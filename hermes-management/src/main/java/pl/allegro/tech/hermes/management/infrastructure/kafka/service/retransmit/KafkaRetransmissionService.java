package pl.allegro.tech.hermes.management.infrastructure.kafka.service.retransmit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.TopicPartition;
import pl.allegro.tech.hermes.api.Topic;
import pl.allegro.tech.hermes.common.broker.BrokerStorage;
import pl.allegro.tech.hermes.common.kafka.KafkaConsumerPool;
import pl.allegro.tech.hermes.common.kafka.KafkaNamesMapper;
import pl.allegro.tech.hermes.common.kafka.KafkaTopic;
import pl.allegro.tech.hermes.common.kafka.offset.PartitionOffset;
import pl.allegro.tech.hermes.common.kafka.offset.SubscriptionOffsetChangeIndicator;
import pl.allegro.tech.hermes.management.domain.message.RetransmissionService;

public class KafkaRetransmissionService implements RetransmissionService {

  private final BrokerStorage brokerStorage;
  private final SubscriptionOffsetChangeIndicator subscriptionOffsetChange;
  private final KafkaConsumerPool consumerPool;
  private final KafkaNamesMapper kafkaNamesMapper;

  public KafkaRetransmissionService(
      BrokerStorage brokerStorage,
      SubscriptionOffsetChangeIndicator subscriptionOffsetChange,
      KafkaConsumerPool consumerPool,
      KafkaNamesMapper kafkaNamesMapper) {

    this.brokerStorage = brokerStorage;
    this.subscriptionOffsetChange = subscriptionOffsetChange;
    this.consumerPool = consumerPool;
    this.kafkaNamesMapper = kafkaNamesMapper;
  }

  @Override
  public void indicateOffsetChange(
      Topic topic,
      String subscription,
      String brokersClusterName,
      List<PartitionOffset> partitionOffsets) {
    for (PartitionOffset partitionOffset : partitionOffsets) {
      subscriptionOffsetChange.setSubscriptionOffset(
          topic.getName(), subscription, brokersClusterName, partitionOffset);
    }
  }

  @Override
  public boolean areOffsetsMoved(Topic topic, String subscriptionName, String brokersClusterName) {
    return kafkaNamesMapper
        .toKafkaTopics(topic)
        .allMatch(
            kafkaTopic -> {
              List<Integer> partitionIds =
                  brokerStorage.readPartitionsIds(kafkaTopic.name().asString());
              return subscriptionOffsetChange.areOffsetsMoved(
                  topic.getName(), subscriptionName, brokersClusterName, kafkaTopic, partitionIds);
            });
  }

  private KafkaConsumer<byte[], byte[]> createKafkaConsumer(KafkaTopic kafkaTopic, int partition) {
    return consumerPool.get(kafkaTopic, partition);
  }

  public List<PartitionOffset> fetchTopicEndOffsets(Topic topic) {
    return fetchTopicOffsetsAt(topic, null);
  }

  public List<PartitionOffset> fetchTopicOffsetsAt(Topic topic, Long timestamp) {
    List<PartitionOffset> partitionOffsetList = new ArrayList<>();
    kafkaNamesMapper
        .toKafkaTopics(topic)
        .forEach(
            k -> {
              List<Integer> partitionsIds = brokerStorage.readPartitionsIds(k.name().asString());
              for (Integer partitionId : partitionsIds) {
                KafkaConsumer<byte[], byte[]> consumer = createKafkaConsumer(k, partitionId);
                long offset = getOffsetForTimestampOrEnd(timestamp, k, partitionId, consumer);
                PartitionOffset partitionOffset =
                    new PartitionOffset(k.name(), offset, partitionId);
                partitionOffsetList.add(partitionOffset);
              }
            });

    return partitionOffsetList;
  }

  private long getOffsetForTimestampOrEnd(
      Long timestamp,
      KafkaTopic kafkaTopic,
      Integer partitionId,
      KafkaConsumer<byte[], byte[]> consumer) {
    long endOffset = getEndingOffset(consumer, kafkaTopic, partitionId);
    return Optional.ofNullable(timestamp)
        .flatMap(ts -> findClosestOffsetJustBeforeTimestamp(consumer, kafkaTopic, partitionId, ts))
        .orElse(endOffset);
  }

  private Optional<Long> findClosestOffsetJustBeforeTimestamp(
      KafkaConsumer<byte[], byte[]> consumer,
      KafkaTopic kafkaTopic,
      int partition,
      long timestamp) {
    TopicPartition topicPartition = new TopicPartition(kafkaTopic.name().asString(), partition);
    return Optional.ofNullable(
            consumer
                .offsetsForTimes(Collections.singletonMap(topicPartition, timestamp))
                .get(topicPartition))
        .map(OffsetAndTimestamp::offset);
  }

  private long getEndingOffset(
      KafkaConsumer<byte[], byte[]> kafkaConsumer, KafkaTopic topicName, int partition) {
    TopicPartition topicPartition = new TopicPartition(topicName.name().asString(), partition);
    Map<TopicPartition, Long> offsets =
        kafkaConsumer.endOffsets(Collections.singleton(topicPartition));
    return Optional.ofNullable(offsets.get(topicPartition))
        .orElseThrow(
            () ->
                new OffsetNotFoundException(
                    String.format("Ending offset for partition %s not found", topicPartition)));
  }
}

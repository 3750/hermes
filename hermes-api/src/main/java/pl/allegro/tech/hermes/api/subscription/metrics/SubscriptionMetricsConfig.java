package pl.allegro.tech.hermes.api.subscription.metrics;

public record SubscriptionMetricsConfig(
        SubscriptionMetricConfig<MessageProcessingDurationMetricOptions> messageProcessingDuration) {
    public static final SubscriptionMetricsConfig DEFAULT = new SubscriptionMetricsConfig(SubscriptionMetricConfig.disabled());
}

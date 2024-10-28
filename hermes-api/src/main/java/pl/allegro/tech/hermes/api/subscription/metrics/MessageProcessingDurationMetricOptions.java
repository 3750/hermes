package pl.allegro.tech.hermes.api.subscription.metrics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.Arrays;

public record MessageProcessingDurationMetricOptions(
    @Size(min = 1, max = 10) long[] thresholdsMilliseconds) {
  @JsonIgnore
  public Duration[] getThresholdsDurations() {
    return Arrays.stream(thresholdsMilliseconds)
        .mapToObj(Duration::ofMillis)
        .toArray(Duration[]::new);
  }

  @Override
  public String toString() {
    return "{thresholdsMilliseconds=" + Arrays.toString(thresholdsMilliseconds) + '}';
  }
}

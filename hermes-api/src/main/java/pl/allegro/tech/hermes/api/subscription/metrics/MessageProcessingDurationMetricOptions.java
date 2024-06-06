package pl.allegro.tech.hermes.api.subscription.metrics;

import java.time.Duration;
import java.util.Arrays;

public record MessageProcessingDurationMetricOptions(long[] thresholdsMilliseconds) {
    public Duration[] getThresholdsDurations() {
        return Arrays.stream(thresholdsMilliseconds).mapToObj(Duration::ofMillis).toArray(Duration[]::new);
    }
}

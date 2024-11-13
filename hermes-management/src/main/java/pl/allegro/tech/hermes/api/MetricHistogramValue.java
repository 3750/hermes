package pl.allegro.tech.hermes.api;

import java.util.Collections;
import java.util.Map;

public record MetricHistogramValue(Map<String, String> buckets) implements MetricValue {

  private static final MetricHistogramValue UNAVAILABLE = new MetricHistogramValue(Map.of());
  private static final MetricHistogramValue DEFAULT_VALUE = new MetricHistogramValue(Map.of());

  public static MetricHistogramValue of() {
    return new MetricHistogramValue(Collections.emptyMap());
  }

  public static MetricHistogramValue of(Map<String, String> buckets) {
    return new MetricHistogramValue(buckets);
  }

  public static MetricHistogramValue of(String le1, String v1) {
    return new MetricHistogramValue(Map.of(le1, v1));
  }

  public static MetricHistogramValue of(String le1, String v1, String le2, String v2) {
    return new MetricHistogramValue(Map.of(le1, v1, le2, v2));
  }

  public static MetricHistogramValue of(
      String le1, String v1, String le2, String v2, String le3, String v3) {
    return new MetricHistogramValue(Map.of(le1, v1, le2, v2, le3, v3));
  }

  public static MetricHistogramValue unavailable() {
    return UNAVAILABLE;
  }

  public static MetricHistogramValue defaultValue() {
    return DEFAULT_VALUE;
  }

  @Override
  public boolean isAvailable() {
    return false;
  }
}

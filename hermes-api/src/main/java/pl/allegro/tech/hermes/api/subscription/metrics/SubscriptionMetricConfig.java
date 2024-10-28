package pl.allegro.tech.hermes.api.subscription.metrics;

import jakarta.validation.Valid;

public record SubscriptionMetricConfig<T>(boolean enabled, @Valid T options) {

  public static <T> SubscriptionMetricConfig<T> disabled() {
    return new SubscriptionMetricConfig<>(false, null);
  }

  @Override
  public String toString() {
    return "{enabled=" + enabled + ", options=" + options + '}';
  }
}

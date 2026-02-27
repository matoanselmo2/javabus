package me.mato.eventbus;

/**
 * Sentinel value used by {@link Listen#event()} to indicate event inference from method parameter.
 */
public final class InferredEvent extends Event {
    private InferredEvent() {
    }
}

package me.mato.eventbus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class RegisteredListener {
    private final Object owner;
    private final Method method;
    private final Class<? extends Event> eventType;
    private final EnumEventPriority priority;

    RegisteredListener(Object owner, Method method, Class<? extends Event> eventType, EnumEventPriority priority) {
        this.owner = owner;
        this.method = method;
        this.eventType = eventType;
        this.priority = priority;
    }

    Object owner() {
        return owner;
    }

    Class<? extends Event> eventType() {
        return eventType;
    }

    EnumEventPriority priority() {
        return priority;
    }

    void invoke(Event event) {
        try {
            if (method.getParameterCount() == 0) {
                method.invoke(owner);
            } else {
                method.invoke(owner, event);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke listener method: " + method, e);
        }
    }
}

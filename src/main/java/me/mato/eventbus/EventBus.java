package me.mato.eventbus;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class EventBus {
    private static final Comparator<RegisteredListener> PRIORITY_ORDER =
            Comparator.comparingInt(listener -> listener.priority().ordinal());

    private final Map<Class<? extends Event>, List<RegisteredListener>> listenersByEvent = new ConcurrentHashMap<>();
    private final Map<Object, List<RegisteredListener>> listenersByOwner = new ConcurrentHashMap<>();

    public synchronized void register(Object listenerOwner) {
        Objects.requireNonNull(listenerOwner, "listenerOwner");
        unregister(listenerOwner);

        List<RegisteredListener> resolved = resolveListeners(listenerOwner);
        if (resolved.isEmpty()) {
            return;
        }

        listenersByOwner.put(listenerOwner, resolved);
        for (RegisteredListener listener : resolved) {
            listenersByEvent.compute(listener.eventType(), (eventType, current) -> {
                List<RegisteredListener> updated = current == null ? new ArrayList<>() : new ArrayList<>(current);
                updated.add(listener);
                updated.sort(PRIORITY_ORDER);
                return List.copyOf(updated);
            });
        }
    }

    public synchronized void unregister(Object listenerOwner) {
        Objects.requireNonNull(listenerOwner, "listenerOwner");
        List<RegisteredListener> existing = listenersByOwner.remove(listenerOwner);
        if (existing == null || existing.isEmpty()) {
            return;
        }

        for (RegisteredListener registered : existing) {
            listenersByEvent.computeIfPresent(registered.eventType(), (eventType, current) -> {
                List<RegisteredListener> updated = new ArrayList<>(current);
                updated.remove(registered);
                if (updated.isEmpty()) {
                    return null;
                }
                return List.copyOf(updated);
            });
        }
    }

    public void post(Event event) {
        Objects.requireNonNull(event, "event");
        for (Class<? extends Event> dispatchType : resolveDispatchTypes(event.getClass())) {
            List<RegisteredListener> listeners = listenersByEvent.get(dispatchType);
            if (listeners == null) {
                continue;
            }
            for (RegisteredListener listener : listeners) {
                listener.invoke(event);
            }
        }
    }

    private List<RegisteredListener> resolveListeners(Object listenerOwner) {
        List<RegisteredListener> resolved = new ArrayList<>();
        for (Method method : getAllMethods(listenerOwner.getClass())) {
            Listen listen = method.getAnnotation(Listen.class);
            if (listen == null) {
                continue;
            }

            method.setAccessible(true);
            Class<? extends Event> eventType = resolveEventType(method, listen);
            resolved.add(new RegisteredListener(listenerOwner, method, eventType, listen.priority()));
        }
        return List.copyOf(resolved);
    }

    private List<Method> getAllMethods(Class<?> type) {
        List<Method> methods = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            Collections.addAll(methods, current.getDeclaredMethods());
            current = current.getSuperclass();
        }
        return methods;
    }

    private Class<? extends Event> resolveEventType(Method method, Listen listen) {
        int parameterCount = method.getParameterCount();
        if (parameterCount > 1) {
            throw new IllegalArgumentException("Listener method must have at most one parameter: " + method);
        }

        Class<? extends Event> annotationEventType = listen.event();
        boolean annotationDeclared = annotationEventType != InferredEvent.class;

        if (parameterCount == 0) {
            if (!annotationDeclared) {
                throw new IllegalArgumentException(
                        "Zero-argument listener methods must declare event type in @Listen: " + method);
            }
            return annotationEventType;
        }

        Class<?> parameterType = method.getParameterTypes()[0];
        if (!Event.class.isAssignableFrom(parameterType)) {
            throw new IllegalArgumentException("Listener parameter must extend Event: " + method);
        }

        @SuppressWarnings("unchecked")
        Class<? extends Event> parameterEventType = (Class<? extends Event>) parameterType;

        if (!annotationDeclared) {
            return parameterEventType;
        }

        if (!parameterEventType.isAssignableFrom(annotationEventType)) {
            throw new IllegalArgumentException(
                    "@Listen event type must match or be more specific than the parameter type: " + method);
        }

        return annotationEventType;
    }

    private Set<Class<? extends Event>> resolveDispatchTypes(Class<? extends Event> eventClass) {
        Set<Class<? extends Event>> resolved = new LinkedHashSet<>();
        Deque<Class<?>> queue = new ArrayDeque<>();
        Set<Class<?>> visited = new HashSet<>();
        queue.add(eventClass);

        while (!queue.isEmpty()) {
            Class<?> type = queue.removeFirst();
            if (!visited.add(type)) {
                continue;
            }

            if (Event.class.isAssignableFrom(type)) {
                @SuppressWarnings("unchecked")
                Class<? extends Event> eventType = (Class<? extends Event>) type;
                resolved.add(eventType);
            }

            Class<?> superClass = type.getSuperclass();
            if (superClass != null && Event.class.isAssignableFrom(superClass)) {
                queue.addLast(superClass);
            }
            for (Class<?> eventInterface : type.getInterfaces()) {
                if (Event.class.isAssignableFrom(eventInterface)) {
                    queue.addLast(eventInterface);
                }
            }
        }

        return resolved;
    }
}

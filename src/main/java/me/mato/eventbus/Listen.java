package me.mato.eventbus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Listen {
    Class<? extends Event> event() default InferredEvent.class;

    EnumEventPriority priority() default EnumEventPriority.NORMAL;
}

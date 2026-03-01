package me.mato.eventbus;

import me.mato.eventbus.events.InputEvent;
import me.mato.eventbus.events.KeyPressEvent;
import me.mato.eventbus.events.PostInitEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventBusTest {
    @Test
    void supportsPriorityZeroArgOneArgInheritanceAndUnregister() {
        EventBus eventBus = new EventBus();
        List<String> calls = new ArrayList<>();
        DemoListener listener = new DemoListener(calls);

        eventBus.register(listener);

        eventBus.post(new PostInitEvent());
        assertEquals(List.of("post-init"), calls);

        eventBus.post(new KeyPressEvent(42));
        assertEquals(List.of(
                "post-init",
                "keypress-highest",
                "input-high",
                "input-low"
        ), calls);

        eventBus.unregister(listener);
        eventBus.post(new PostInitEvent());
        eventBus.post(new KeyPressEvent(100));

        assertEquals(List.of(
                "post-init",
                "keypress-highest",
                "input-high",
                "input-low"
        ), calls);
    }

    static final class DemoListener {
        private final List<String> calls;

        DemoListener(List<String> calls) {
            this.calls = calls;
        }

        @Listen(event = PostInitEvent.class)
        private void onPostInit() {
            calls.add("post-init");
        }

        @Listen(priority = EnumEventPriority.HIGHEST)
        private void onKeyPress(KeyPressEvent event) {
            if (event.key() > 0) {
                calls.add("keypress-highest");
            }
        }

        @Listen(priority = EnumEventPriority.HIGH)
        public void onInputHigh(InputEvent ignored) {
            calls.add("input-high");
        }

        @Listen(priority = EnumEventPriority.LOW)
        public void onInputLow(InputEvent ignored) {
            calls.add("input-low");
        }
    }
}

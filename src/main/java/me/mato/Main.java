package me.mato;

import me.mato.eventbus.EnumEventPriority;
import me.mato.eventbus.EventBus;
import me.mato.eventbus.Listen;
import me.mato.eventbus.events.KeyPressEvent;
import me.mato.eventbus.events.PostInitEvent;

public class Main {
    public static void main(String[] args) {
        EventBus eventBus = new EventBus();
        ExampleListeners listeners = new ExampleListeners();

        eventBus.register(listeners);

        eventBus.post(new PostInitEvent());
        eventBus.post(new KeyPressEvent(65));

        eventBus.unregister(listeners);
        eventBus.post(new PostInitEvent());
    }

    static final class ExampleListeners {
        @Listen(event = PostInitEvent.class, priority = EnumEventPriority.HIGHEST)
        public void onPostInit() {
            System.out.println("PostInit");
        }

        @Listen(priority = EnumEventPriority.HIGH)
        public void onKeyPress(KeyPressEvent event) {
            System.out.println("Key pressed: " + event.key());
        }
    }
}

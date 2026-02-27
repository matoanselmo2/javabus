package me.mato.eventbus.events;

public final class KeyPressEvent extends InputEvent {
    private final int key;

    public KeyPressEvent(int key) {
        this.key = key;
    }

    public int key() {
        return key;
    }
}

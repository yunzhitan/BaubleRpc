package top.yunzhitan.rpc.future;

import top.yunzhitan.rpc.Listener;

import java.util.Arrays;

public class DefaultListeners<V> {
    private Listener<V>[] listeners;
    private int size;

    static <T> DefaultListeners<T> with(Listener<T> first, Listener<T> second) {
        return new DefaultListeners<>(first, second);
    }

    @SuppressWarnings("unchecked")
    private DefaultListeners(Listener<V> first, Listener<V> second) {
        listeners = new Listener[2];
        listeners[0] = first;
        listeners[1] = second;
        size = 2;
    }

    public void add(Listener<V> l) {
        Listener<V>[] listeners = this.listeners;
        final int size = this.size;
        if (size == listeners.length) {
            this.listeners = listeners = Arrays.copyOf(listeners, size << 1);
        }
        listeners[size] = l;
        this.size = size + 1;
    }

    public void remove(Listener<V> l) {
        final Listener<V>[] listeners = this.listeners;
        int size = this.size;
        for (int i = 0; i < size; i++) {
            if (listeners[i] == l) {
                int length = size - i - 1;
                if (length > 0) {
                    System.arraycopy(listeners, i + 1, listeners, i, length);
                }
                listeners[--size] = null;
                this.size = size;
                return;
            }
        }
    }

    public Listener<V>[] listeners() {
        return listeners;
    }

    public int size() {
        return size;
    }

}

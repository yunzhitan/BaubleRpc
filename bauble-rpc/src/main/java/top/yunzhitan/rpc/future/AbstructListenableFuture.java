package top.yunzhitan.rpc.future;


import top.yunzhitan.rpc.Listener;

public abstract class AbstructListenableFuture<V> extends AbstractFuture<V> implements ListenableFuture<V> {

    private Object listeners;

    @Override
    protected void done(int state, Object x) {
        notifyListeners(state, x);
    }

    @Override
    public ListenableFuture<V> addListener(Listener<V> listener) {

        synchronized (this) {
            addListener0(listener);
        }

        if (isDone()) {
            notifyListeners(state(), result());
        }

        return this;
    }

    @Override
    public ListenableFuture<V> addListeners(Listener<V>... listeners) {

        synchronized (this) {
            for (Listener<V> listener : listeners) {
                if (listener == null) {
                    continue;
                }
                addListener0(listener);
            }
        }

        if (isDone()) {
            notifyListeners(state(), result());
        }

        return this;
    }

    @Override
    public ListenableFuture<V> removeListener(Listener<V> listener) {

        synchronized (this) {
            removeListener0(listener);
        }

        return this;
    }

    @Override
    public ListenableFuture<V> removeListeners(Listener<V>... listeners) {

        synchronized (this) {
            for (Listener<V> listener : listeners) {
                if (listener == null) {
                    continue;
                }
                removeListener0(listener);
            }
        }

        return this;
    }

    protected void notifyListeners(int state, Object x) {
        Object listeners;
        synchronized (this) {
            // no competition unless the listener is added too late or the rpc call timeout
            if (this.listeners == null) {
                return;
            }

            listeners = this.listeners;
            this.listeners = null;
        }

        if (listeners instanceof DefaultListeners) {
            Listener<V>[] array = ((DefaultListeners<V>) listeners).listeners();
            int size = ((DefaultListeners<V>) listeners).size();

            for (int i = 0; i < size; i++) {
                notifyListener0(array[i], state, x);
            }
        } else {
            notifyListener0((Listener<V>) listeners, state, x);
        }
    }

    protected abstract void notifyListener0(Listener<V> listener, int state, Object x);

    private void addListener0(Listener<V> listener) {
        if (listeners == null) {
            listeners = listener;
        } else if (listeners instanceof DefaultListeners) {
            ((DefaultListeners<V>) listeners).add(listener);
        } else {
            listeners = DefaultListeners.with((Listener<V>) listeners, listener);
        }
    }

    private void removeListener0(Listener<V> listener) {
        if (listeners instanceof DefaultListeners) {
            ((DefaultListeners<V>) listeners).remove(listener);
        } else if (listeners == listener) {
            listeners = null;
        }
    }
}

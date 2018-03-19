package top.yunzhitan.Util.collection;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentSet<E> extends AbstractSet<E> implements Serializable{

    private static final long serialVersionUID = -348237463248234L;

    private ConcurrentMap<E,Boolean> map;

    public ConcurrentSet() {
        map = new ConcurrentHashMap<>();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean add(E e) {
        return map.putIfAbsent(e,Boolean.TRUE);
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o);
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }
}

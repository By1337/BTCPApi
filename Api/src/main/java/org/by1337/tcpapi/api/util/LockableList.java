package org.by1337.tcpapi.api.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.function.Consumer;

public class LockableList<E> implements Iterable<E> {
    private final List<E> source;
    private final Queue<Action> actions = new LinkedBlockingQueue <>();
    private boolean locked;

    public LockableList(List<E> source) {
        this.source = source;
    }

    public void lock(){
        locked = true;
    }
    @SuppressWarnings("unchecked")
    public void unlock(){
        locked = false;
        Action action;
        while ((action = actions.poll()) != null){
            switch (action.type){
                case ADD -> source.add((E) action.object);
                case CLEAR -> source.clear();
                case REMOVE -> source.remove(action.object);
            }
        }
    }
    public LockableList() {
        source = new ArrayList<>();
    }

    public int size() {
        return source.size();
    }

    public boolean isEmpty() {
        return source.isEmpty();
    }

    public boolean contains(Object o) {
        return source.contains(o);
    }

    @NotNull
    public Iterator<E> iterator() {
        return new Itr();
    }

    @NotNull
    public Object @NotNull [] toArray() {
        return source.toArray();
    }

    @NotNull
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        return source.toArray(a);
    }

    public boolean add(E e) {
        if (locked) {
            return actions.offer(new Action(ActionType.ADD, e));
        }
        return source.add(e);
    }

    public boolean remove(Object o) {
        if (locked) {
            actions.offer(new Action(ActionType.REMOVE, o));
            return true;
        }
        return source.remove(o);
    }


    public void clear() {
        if (locked) {
            actions.offer(new Action(ActionType.CLEAR, null));
        } else {
            source.clear();
        }
    }

    public E get(int index) {
        return source.get(index);
    }

    public int indexOf(Object o) {
        return source.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return source.lastIndexOf(o);
    }

    private class Itr implements Iterator<E> {
        Iterator<E> sourceIter = source.iterator();
        E last;

        Itr() {
        }

        public boolean hasNext() {
            return sourceIter.hasNext();
        }

        @SuppressWarnings("unchecked")
        public E next() {
            last = sourceIter.next();
            return last;
        }

        public void remove() {
            if (locked) {
                actions.offer(new Action(ActionType.REMOVE, last));
            } else {
                sourceIter.remove();
            }
        }

        public void forEachRemaining(Consumer<? super E> action) {
            sourceIter.forEachRemaining(action);
        }
    }

    private enum ActionType {
        ADD,
        REMOVE,
        CLEAR,
    }

    private static class Action {
        final ActionType type;
        final Object object;

        public Action(ActionType type, Object object) {
            this.type = type;
            this.object = object;
        }
    }
}

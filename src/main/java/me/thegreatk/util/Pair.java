package me.thegreatk.util;

public class Pair<E1, E2> {
    private final E1 first;
    private final E2 second;

    public Pair(E1 first, E2 second) {
        this.first = first;
        this.second = second;
    }

    public E1 getFirst() {
        return first;
    }

    public E2 getSecond() {
        return second;
    }

    public Object getCounterpart(Object part) {
        if (part == null) throw new NullPointerException("Attempting to search for null counterpart");

        if (first.equals(part)) return second;
        if (second.equals(part)) return first;

        throw new IllegalArgumentException("Could not find counterpart");
    }

    public static <E1, E2> Pair<E1, E2> of(E1 first, E2 second) {
        return new Pair<>(first, second);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair<?, ?> pair) {
            return pair.getFirst().equals(getFirst()) && pair.getSecond().equals(getSecond());
        }
        return false;
    }
}

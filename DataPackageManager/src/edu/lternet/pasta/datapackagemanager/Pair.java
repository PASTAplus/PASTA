// Generic container class that holds a pair of objects of potentially different types.
package edu.lternet.pasta.datapackagemanager;

public class Pair<T, U> {
    public final T t;
    public final U u;

    public Pair(T t, U u) {
        this.t = t;
        this.u = u;
    }
}

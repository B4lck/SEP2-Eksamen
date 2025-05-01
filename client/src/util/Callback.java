package util;


@FunctionalInterface
public interface Callback<R> {
    void callback(R res);
}
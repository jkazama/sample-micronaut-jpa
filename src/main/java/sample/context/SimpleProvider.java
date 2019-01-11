package sample.context;

import javax.inject.Provider;

/**
 * Provider that simply returns the object it holds.
 */
public class SimpleProvider<T> implements Provider<T> {

    private final T value;
    
    public SimpleProvider(T value) {
        this.value = value;
    }
    
    /** {@inheritDoc} */
    @Override
    public T get() {
        return value;
    }
    
    public static <T> SimpleProvider<T> of(T value) {
        return new SimpleProvider<>(value);
    }
    
}

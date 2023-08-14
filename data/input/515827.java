public class JavaProxyFactory<T> implements IProxyFactory<T> {
    @SuppressWarnings("unchecked")
    public T createProxy(Class<T> toMock, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(toMock.getClassLoader(),
                new Class[] { toMock }, handler);
    }
}

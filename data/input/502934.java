public class ThreadLocalCache<T> {
    private SoftReference<ThreadLocal<T>> storage = new SoftReference<ThreadLocal<T>>(
            null);
    private ThreadLocal<T> getThreadLocal() {
        ThreadLocal<T> tls = storage.get();
        if (tls == null) {
            tls = new ThreadLocal<T>() {
                public T initialValue() {
                    return ThreadLocalCache.this.initialValue();
                }
            };
            storage = new SoftReference<ThreadLocal<T>>(tls);
        }
        return tls;
    }
    protected T initialValue() {
        return null;
    }
    public T get() {
        return getThreadLocal().get();
    }
    public void set(T value) {
        getThreadLocal().set(value);
    }
    public void remove() {
        storage.clear();
    }
    public static ThreadLocalCache<CharsetDecoder> utf8Decoder = new ThreadLocalCache<CharsetDecoder>() {
        protected CharsetDecoder initialValue() {
            return Charset.forName("UTF-8").newDecoder();
        }
    };
    public static ThreadLocalCache<CharsetEncoder> utf8Encoder = new ThreadLocalCache<CharsetEncoder>() {
        protected CharsetEncoder initialValue() {
            return Charset.forName("UTF-8").newEncoder();
        }
    };
    public static ThreadLocalCache<java.nio.ByteBuffer> byteBuffer = new ThreadLocalCache<java.nio.ByteBuffer>() {
        protected java.nio.ByteBuffer initialValue() {
            return java.nio.ByteBuffer.allocate(72); 
        }
    };
    public static ThreadLocalCache<CharBuffer> charBuffer = new ThreadLocalCache<CharBuffer>() {
        protected CharBuffer initialValue() {
            return CharBuffer.allocate(72); 
        }
    };
}

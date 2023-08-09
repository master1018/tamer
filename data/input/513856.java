    abstract class Internal extends KeyStore {
        protected Internal() {
            super(null, null, null);
        }
    }
    @VirtualTestTarget
    static abstract class PKCS12 extends Internal {
        protected abstract void method();
    }
}

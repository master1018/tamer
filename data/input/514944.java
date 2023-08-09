    abstract class Internal extends KeyFactory {
        protected Internal() {
            super(null, null, null);
        }
    }
    @VirtualTestTarget
    static abstract class RSA extends Internal {
        protected abstract void method();
    }
    @VirtualTestTarget
    static abstract class DSA extends Internal {
        protected abstract void method();
    }
    @VirtualTestTarget
    static abstract class DH extends Internal {
        protected abstract void method();
    }
}

    abstract class Internal extends SecureRandom {
        protected Internal() {
            super();
        }
    }
    @VirtualTestTarget
    static abstract class SHAPRNG1 extends Internal {
        protected abstract void method();
    }
}

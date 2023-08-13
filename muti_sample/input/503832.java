    abstract class Internal extends CertPathBuilder {
        protected Internal() {
            super(null, null, null);
        }
    }
    @VirtualTestTarget
    static abstract class PKIX extends Internal {
        protected abstract void method();
    }
}

    abstract class Internal extends CertPathValidator {
        protected Internal() {
            super(null, null, null);
        }
    }
    @VirtualTestTarget
    static abstract class PKIX extends Internal {
        protected abstract void method();
    }
}

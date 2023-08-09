    abstract class Internal extends CertificateFactory {
        protected Internal() {
            super(null, null, null);
        }
    }
    @VirtualTestTarget
    static abstract class X509 extends Internal {
        protected abstract void method();
    }
}

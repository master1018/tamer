    abstract class Internal {
        protected Internal() {
        }
    }
    @VirtualTestTarget
    static abstract class DES extends Internal {
        protected abstract void method();
    }
    @VirtualTestTarget
    static abstract class DESede extends Internal {
        protected abstract void method();
    }
    @VirtualTestTarget
    static abstract class PBEWITHMD5ANDDES extends Internal {
        protected abstract void method();
    }
}

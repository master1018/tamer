    abstract class Internal {
        protected Internal() {
        }
    }
    @VirtualTestTarget
    static abstract class HMACMD5 extends Internal {
        protected abstract void method();
    }
    @VirtualTestTarget
    static abstract class HMACSHA1 extends Internal {
        protected abstract void method();
    }
    @VirtualTestTarget
    static abstract class HMACSHA256 extends Internal {
        protected abstract void method();
    }
    @VirtualTestTarget
    static abstract class HMACSHA384 extends Internal {
        protected abstract void method();
    }
    @VirtualTestTarget
    static abstract class HMACSHA512 extends Internal {
        protected abstract void method();
    }
}

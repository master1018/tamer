final class IOStatus {
    private IOStatus() { }
    static final int EOF = -1;              
    static final int UNAVAILABLE = -2;      
    static final int INTERRUPTED = -3;      
    static final int UNSUPPORTED = -4;      
    static final int THROWN = -5;           
    static final int UNSUPPORTED_CASE = -6; 
    static int normalize(int n) {
        if (n == UNAVAILABLE)
            return 0;
        return n;
    }
    static boolean check(int n) {
        return (n >= UNAVAILABLE);
    }
    static long normalize(long n) {
        if (n == UNAVAILABLE)
            return 0;
        return n;
    }
    static boolean check(long n) {
        return (n >= UNAVAILABLE);
    }
    static boolean checkAll(long n) {
        return ((n > EOF) || (n < UNSUPPORTED_CASE));
    }
}

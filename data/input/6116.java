public class InternalException extends RuntimeException {
     private int errorCode;
     public InternalException() {
         super();
         this.errorCode = 0;
     }
     public InternalException(String s) {
         super(s);
         this.errorCode = 0;
     }
    public InternalException(int errorCode) {
        super();
        this.errorCode = errorCode;
    }
    public InternalException(String s, int errorCode) {
        super(s);
        this.errorCode = errorCode;
    }
    public int errorCode() {
        return errorCode;
    }
}

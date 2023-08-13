public class DatatypeConfigurationException extends Exception {
    private static final long serialVersionUID = -1699373159027047238L;
    private Throwable causeOnJDK13OrBelow;
    private transient boolean isJDK14OrAbove = false;
    public DatatypeConfigurationException() {
        super();
    }
    public DatatypeConfigurationException(String message) {
        super(message);
    }
    public DatatypeConfigurationException(String message, Throwable cause) {
        super(message);
        initCauseByReflection(cause);
    }
    public DatatypeConfigurationException(Throwable cause) {
        super(cause == null ? null : cause.toString());
        initCauseByReflection(cause);
    }
    public void printStackTrace() {
        if (!isJDK14OrAbove && causeOnJDK13OrBelow != null) {
            printStackTrace0(new PrintWriter(System.err, true));
        }
        else {
            super.printStackTrace();
        }
    }
    public void printStackTrace(PrintStream s) {
        if (!isJDK14OrAbove && causeOnJDK13OrBelow != null) {
            printStackTrace0(new PrintWriter(s));
        }
        else {
            super.printStackTrace(s);
        }
    }
    public void printStackTrace(PrintWriter s) {
        if (!isJDK14OrAbove && causeOnJDK13OrBelow != null) {
            printStackTrace0(s);
        }
        else {
            super.printStackTrace(s);
        }
    }
    private void printStackTrace0(PrintWriter s) {
        causeOnJDK13OrBelow.printStackTrace(s);
        s.println("------------------------------------------");
        super.printStackTrace(s);
    }
    private void initCauseByReflection(Throwable cause) {
        causeOnJDK13OrBelow = cause;
        try {
            Method m = this.getClass().getMethod("initCause", new Class[] {Throwable.class});
            m.invoke(this, new Object[] {cause});
            isJDK14OrAbove = true;
        }
        catch (Exception e) {}
    }
    private void readObject(ObjectInputStream in) 
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        try {
            Method m1 = this.getClass().getMethod("getCause", new Class[] {});
            Throwable cause = (Throwable) m1.invoke(this, new Object[] {});
            if (causeOnJDK13OrBelow == null) {
                causeOnJDK13OrBelow = cause;
            }
            else if (cause == null) {
                Method m2 = this.getClass().getMethod("initCause", new Class[] {Throwable.class});
                m2.invoke(this, new Object[] {causeOnJDK13OrBelow});
            }
            isJDK14OrAbove = true;
        }
        catch (Exception e) {}
    }
}

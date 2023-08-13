public class Chaining {
    static void check(Throwable t, String msg, Throwable cause)
        throws Exception
    {
        String tmsg = t.getMessage();
        if (msg == null ? tmsg != null : !msg.equals(tmsg)) {
            throw new RuntimeException("message does not match");
        }
        if (t.getClass().getField("detail").get(t) != cause) {
            throw new RuntimeException("detail field does not match");
        }
        if (t.getCause() != cause) {
            throw new RuntimeException("getCause value does not match");
        }
        try {
            t.initCause(cause);
            throw new RuntimeException("initCause succeeded");
        } catch (IllegalStateException e) {
        }
    }
    static void test(Throwable t, String msg, Throwable cause)
        throws Exception
    {
        check(t, msg, cause);
        Throwable[] pair = new Throwable[]{t, cause};
        pair = (Throwable[]) new MarshalledObject(pair).get();
        check(pair[0], msg, pair[1]);
    }
    public static void main(String[] args) throws Exception {
        Exception t = new RuntimeException();
        String foo = "foo";
        String fooMsg = "foo; nested exception is: \n\t" + t;
        test(new RemoteException(), null, null);
        test(new RemoteException(foo), foo, null);
        test(new RemoteException(foo, t), fooMsg, t);
        test(new ActivationException(), null, null);
        test(new ActivationException(foo), foo, null);
        test(new ActivationException(foo, t), fooMsg, t);
        test(new ServerCloneException(foo), foo, null);
        test(new ServerCloneException(foo, t), fooMsg, t);
    }
}

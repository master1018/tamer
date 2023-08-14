public class Test6277246 {
    public static void main(String[] args) {
        try {
            System.setSecurityManager(new SecurityManager());
            Class container = Class.forName("java.lang.Class");
            Class parameter = Class.forName("java.lang.String");
            Method method = container.getMethod("forName", parameter);
            Object[] arglist = new Object[] {"sun.misc.BASE64Encoder"};
            EventHandler eh = new EventHandler(Test6277246.class, "forName", "", "forName");
            Object object = eh.invoke(null, method, arglist);
            throw new Error((object != null) ? "test failure" : "test error");
        }
        catch (ClassNotFoundException exception) {
            throw new Error("unexpected exception", exception);
        }
        catch (NoSuchMethodException exception) {
            throw new Error("unexpected exception", exception);
        }
        catch (SecurityException exception) {
        }
        catch (RuntimeException exception) {
            throw new Error("unexpected exception", exception);
        }
    }
}

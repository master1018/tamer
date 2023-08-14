public class Test6224433 {
    public static void main(String[] args) {
        try {
            System.setSecurityManager(new SecurityManager());
            Class target = Test6224433.class;
            String method = "forName";
            String[] params = {"sun.misc.BASE64Encoder"};
            if (null != new Expression(target, method, params).getValue())
                throw new Error("failure: bug exists");
            throw new Error("unexpected condition");
        }
        catch (ClassNotFoundException exception) {
            throw new Error("expected class missing", exception);
        }
        catch (SecurityException exception) {
        }
        catch (Exception exception) {
            throw new Error("unexpected condition", exception);
        }
    }
}

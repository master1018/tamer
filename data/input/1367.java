public class FailureDebugOption {
   public static void main (String argv[]) throws Exception {
        try {
            AccessController.checkPermission(
                        new BasicPermission("no such permission"){});
        } catch (NullPointerException npe) {
           throw new Exception("Unexpected NullPointerException for security" +
                        " debug option, -Djava.security.debug=failure");
        } catch (AccessControlException ace) {
        }
   }
}

public class bug6622002 {
     public static void main(String[] args) {
         if (createPrivateValue() == null) {
             throw new RuntimeException("The private value unexpectedly wasn't created");
         }
         if (createPublicValue() == null) {
             throw new RuntimeException("The public value unexpectedly wasn't created");
         }
         System.setSecurityManager(new SecurityManager());
         if (createPrivateValue() != null) {
             throw new RuntimeException("The private value was unexpectedly created");
         }
         if (createPublicValue() == null) {
             throw new RuntimeException("The public value unexpectedly wasn't created");
         }
     }
    private static Object createPrivateValue() {
        return new UIDefaults.ProxyLazyValue(
            "javax.swing.MultiUIDefaults").createValue(null);
    }
    private static Object createPublicValue() {
        return new UIDefaults.ProxyLazyValue(
            "javax.swing.UIDefaults").createValue(null);
    }
}

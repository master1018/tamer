public class CheckNullPermission {
   public static void main (String argv[]) throws Exception {
       ProtectionDomain pd[] = new ProtectionDomain[1];
       try {
           (new AccessControlContext(pd)).checkPermission(null);
           throw new Exception("Expected NullPointerException not thrown");
       } catch (NullPointerException npe) {
       }
   }
}

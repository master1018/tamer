public class AvoidPropertyExpansionExceptions {
    public static void main(String[] args) throws Exception {
      Policy p = Policy.getPolicy();
      PermissionCollection pc = p.getPermissions(
        new CodeSource(null, (java.security.cert.Certificate[]) null));
      Enumeration e = pc.elements();
      if (!e.hasMoreElements()) {
        throw new Exception("Permission incorrectly ignored");
      }
    }
}

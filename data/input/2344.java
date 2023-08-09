public class PrincipalExpansionErrorAction implements
        java.security.PrivilegedExceptionAction {
    public Object run() throws Exception {
        java.io.FileInputStream fis = new java.io.FileInputStream ("/testFile");
        return fis;
    }
}

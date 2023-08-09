public class GSSUtil {
    public static Subject createSubject(GSSName principals,
                                     GSSCredential credentials) {
        return  sun.security.jgss.GSSUtil.getSubject(principals,
                                                     credentials);
    }
}

public class LifeTimeInSeconds {
    public static void main(String[] args) throws Exception {
        new OneKDC(null).writeJAASConf();
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        GSSManager gm = GSSManager.getInstance();
        GSSCredential cred = gm.createCredential(GSSCredential.INITIATE_AND_ACCEPT);
        int time = cred.getRemainingLifetime();
        int time2 = cred.getRemainingInitLifetime(null);
        int elevenhrs = 11*3600;
        if (time > elevenhrs+60 || time < elevenhrs-60) {
            throw new Exception("getRemainingLifetime returns wrong value.");
        }
        if (time2 > elevenhrs+60 || time2 < elevenhrs-60) {
            throw new Exception("getRemainingInitLifetime returns wrong value.");
        }
    }
}

public class BuildPath {
    public static void main(String[] args) throws Exception {
        TrustAnchor anchor =
            new TrustAnchor(CertUtils.getCertFromFile("mgrM2mgrM"), null);
        X509Certificate target = CertUtils.getCertFromFile("mgrM2leadMA");
        X509CertSelector xcs = new X509CertSelector();
        xcs.setSubject("CN=leadMA,CN=mgrM,OU=prjM,OU=divE,OU=Comp,O=sun,C=us");
        xcs.setCertificate(target);
        SunCertPathBuilderParameters params =
            new SunCertPathBuilderParameters(Collections.singleton(anchor),xcs);
        params.setBuildForward(false);
        CertStore cs = CertUtils.createStore(new String[]
            {"mgrM2prjM", "prjM2mgrM", "prjM2divE", "mgrM2leadMA" });
        params.addCertStore(cs);
        CertStore cs2 = CertUtils.createCRLStore
            (new String[] {"mgrMcrl", "prjMcrl"});
        params.addCertStore(cs2);
        PKIXCertPathBuilderResult res = CertUtils.build(params);
    }
}

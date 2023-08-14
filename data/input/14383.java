public class ProfileDeferralInfo extends InputStream {
    public int colorSpaceType, numComponents, profileClass;
    public String filename;
    public ProfileDeferralInfo(String fn, int type, int ncomp, int pclass) {
        super();
        filename = fn;
        colorSpaceType = type;
        numComponents = ncomp;
        profileClass = pclass;
    }
    public int read() throws IOException {
        return 0;
    }
}

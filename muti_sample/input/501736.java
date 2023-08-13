public class ASN1UTCTime extends ASN1Time {
    public static final int UTC_HM = 11;
    public static final int UTC_HMS = 13;
    public static final int UTC_LOCAL_HM = 15;
    public static final int UTC_LOCAL_HMS = 17;
    private static final ASN1UTCTime ASN1 = new ASN1UTCTime();
    public ASN1UTCTime() {
        super(TAG_UTCTIME);
    }
    public static ASN1UTCTime getInstance() {
        return ASN1;
    }
    public Object decode(BerInputStream in) throws IOException {
        in.readUTCTime();
        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }
    public void encodeContent(BerOutputStream out) {
        out.encodeUTCTime();
    }
    private final static String UTC_PATTERN = "yyMMddHHmmss'Z'"; 
    public void setEncodingContent(BerOutputStream out) {
        SimpleDateFormat sdf = new SimpleDateFormat(UTC_PATTERN);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); 
        try {
            out.content = sdf.format(out.content).getBytes("UTF-8"); 
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
        out.length = ((byte[]) out.content).length;
    }
}

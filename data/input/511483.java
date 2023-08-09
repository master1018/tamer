public class ASN1GeneralizedTime extends ASN1Time {
    private static final ASN1GeneralizedTime ASN1 = new ASN1GeneralizedTime();
    public ASN1GeneralizedTime() {
        super(TAG_GENERALIZEDTIME);
    }
    public static ASN1GeneralizedTime getInstance() {
        return ASN1;
    }
    public Object decode(BerInputStream in) throws IOException {
        in.readGeneralizedTime();
        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }
    public void encodeContent(BerOutputStream out) {
        out.encodeGeneralizedTime();
    }
    private final static String GEN_PATTERN = "yyyyMMddHHmmss.SSS"; 
    public void setEncodingContent(BerOutputStream out) {
        SimpleDateFormat sdf = new SimpleDateFormat(GEN_PATTERN);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); 
        String temp = sdf.format(out.content);
        int nullId;
        int currLength;
        while (((nullId = temp.lastIndexOf('0', currLength = temp.length() - 1)) != -1)
                & (nullId == currLength)) {
            temp = temp.substring(0, nullId);
        }
        if (temp.charAt(currLength) == '.') {
            temp = temp.substring(0, currLength);
        }
        try {
            out.content = (temp + "Z").getBytes("UTF-8"); 
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
        out.length = ((byte[]) out.content).length;
    }
}

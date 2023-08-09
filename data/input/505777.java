public class TimeStampResp {
    private final PKIStatusInfo status;
    private final ContentInfo timeStampToken;
    public TimeStampResp(PKIStatusInfo status, ContentInfo timeStampToken) {
        this.status = status;
        this.timeStampToken = timeStampToken;
    }
    public String toString(){
        StringBuilder res = new StringBuilder();
        res.append("-- TimeStampResp:");
        res.append("\nstatus:  ");
        res.append(status);
        res.append("\ntimeStampToken:  ");
        res.append(timeStampToken);
        res.append("\n-- TimeStampResp End\n");
        return res.toString();
    }
    public PKIStatusInfo getStatus() {
        return status;
    }
    public ContentInfo getTimeStampToken() {
        return timeStampToken;
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            PKIStatusInfo.ASN1,     
            ContentInfo.ASN1}) {    
        {
            setOptional(1);
        }
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new TimeStampResp(
                    (PKIStatusInfo) values[0],
                    (ContentInfo) values[1]);
        }
        protected void getValues(Object object, Object[] values) {
            TimeStampResp resp = (TimeStampResp) object;
            values[0] = resp.status;
            values[1] = resp.timeStampToken;
        }    
    };
}

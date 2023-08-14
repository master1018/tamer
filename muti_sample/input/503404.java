public class PKIStatusInfo {
    private final PKIStatus status;
    private final List statusString;
    private final PKIFailureInfo failInfo;
    public PKIStatusInfo(PKIStatus pKIStatus, List statusString,
            PKIFailureInfo failInfo) {
        this.status = pKIStatus;
        this.statusString = statusString;
        this.failInfo = failInfo;
    }
    public String toString(){
        StringBuilder res = new StringBuilder();
        res.append("-- PKIStatusInfo:");
        res.append("\nPKIStatus : ");
        res.append(status);
        res.append("\nstatusString:  ");
        res.append(statusString);
        res.append("\nfailInfo:  ");
        res.append(failInfo);
        res.append("\n-- PKIStatusInfo End\n");
        return res.toString();
    }
    public PKIFailureInfo getFailInfo() {
        return failInfo;
    }
    public PKIStatus getStatus() {
        return status;
    }
    public List getStatusString() {
        return statusString;
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
        ASN1Integer.getInstance(),                      
        new ASN1SequenceOf(ASN1StringType.UTF8STRING),  
        ASN1BitString.getInstance() }) {                
        {
            setOptional(1);
            setOptional(2);
        }
        protected void getValues(Object object, Object[] values) {
            PKIStatusInfo psi = (PKIStatusInfo) object;
            values[0] = BigInteger.valueOf(psi.status.getStatus())
                    .toByteArray();
            values[1] = psi.statusString;
            if (psi.failInfo != null) {
                boolean[] failInfoBoolArray = new boolean[PKIFailureInfo
                        .getMaxValue()];
                failInfoBoolArray[psi.failInfo.getValue()] = true;
                values[2] = new BitString(failInfoBoolArray);
            } else {
                values[2] = null;
            }
        }
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            int failInfoValue = -1;
            if (values[2] != null) {
                boolean[] failInfoBoolArray = ((BitString) values[2]).toBooleanArray();
                for (int i = 0; i < failInfoBoolArray.length; i++) {
                    if (failInfoBoolArray[i]) {
                        failInfoValue = i;
                        break;
                    }
                }
            }
            return new PKIStatusInfo(
                    PKIStatus.getInstance(ASN1Integer.toIntValue(values[0])),
                    (List)values[1],
                    PKIFailureInfo.getInstance(failInfoValue));
        }
    };
}

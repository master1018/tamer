public class SerialNumber {
    private BigInteger  serialNum;
    private void construct(DerValue derVal) throws IOException {
        serialNum = derVal.getBigInteger();
        if (derVal.data.available() != 0) {
            throw new IOException("Excess SerialNumber data");
        }
    }
    public SerialNumber(BigInteger num) {
        serialNum = num;
    }
    public SerialNumber(int num) {
        serialNum = BigInteger.valueOf(num);
    }
    public SerialNumber(DerInputStream in) throws IOException {
        DerValue derVal = in.getDerValue();
        construct(derVal);
    }
    public SerialNumber(DerValue val) throws IOException {
        construct(val);
    }
    public SerialNumber(InputStream in) throws IOException {
        DerValue derVal = new DerValue(in);
        construct(derVal);
    }
    public String toString() {
        return ("SerialNumber: [" + Debug.toHexString(serialNum) + "]");
    }
    public void encode(DerOutputStream out) throws IOException {
        out.putInteger(serialNum);
    }
    public BigInteger getNumber() {
        return serialNum;
    }
}

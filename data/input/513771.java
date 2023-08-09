public class SignalStrength implements Parcelable {
    static final String LOG_TAG = "PHONE";
    private int mGsmSignalStrength; 
    private int mGsmBitErrorRate;   
    private int mCdmaDbm;   
    private int mCdmaEcio;  
    private int mEvdoDbm;   
    private int mEvdoEcio;  
    private int mEvdoSnr;   
    private boolean isGsm; 
    public static SignalStrength newFromBundle(Bundle m) {
        SignalStrength ret;
        ret = new SignalStrength();
        ret.setFromNotifierBundle(m);
        return ret;
    }
    public SignalStrength() {
        mGsmSignalStrength = 99;
        mGsmBitErrorRate = -1;
        mCdmaDbm = -1;
        mCdmaEcio = -1;
        mEvdoDbm = -1;
        mEvdoEcio = -1;
        mEvdoSnr = -1;
        isGsm = true;
    }
    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate,
            int cdmaDbm, int cdmaEcio,
            int evdoDbm, int evdoEcio, int evdoSnr, boolean gsm) {
        mGsmSignalStrength = gsmSignalStrength;
        mGsmBitErrorRate = gsmBitErrorRate;
        mCdmaDbm = cdmaDbm;
        mCdmaEcio = cdmaEcio;
        mEvdoDbm = evdoDbm;
        mEvdoEcio = evdoEcio;
        mEvdoSnr = evdoSnr;
        isGsm = gsm;
    }
    public SignalStrength(SignalStrength s) {
        copyFrom(s);
    }
    protected void copyFrom(SignalStrength s) {
        mGsmSignalStrength = s.mGsmSignalStrength;
        mGsmBitErrorRate = s.mGsmBitErrorRate;
        mCdmaDbm = s.mCdmaDbm;
        mCdmaEcio = s.mCdmaEcio;
        mEvdoDbm = s.mEvdoDbm;
        mEvdoEcio = s.mEvdoEcio;
        mEvdoSnr = s.mEvdoSnr;
        isGsm = s.isGsm;
    }
    public SignalStrength(Parcel in) {
        mGsmSignalStrength = in.readInt();
        mGsmBitErrorRate = in.readInt();
        mCdmaDbm = in.readInt();
        mCdmaEcio = in.readInt();
        mEvdoDbm = in.readInt();
        mEvdoEcio = in.readInt();
        mEvdoSnr = in.readInt();
        isGsm = (in.readInt() != 0);
    }
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mGsmSignalStrength);
        out.writeInt(mGsmBitErrorRate);
        out.writeInt(mCdmaDbm);
        out.writeInt(mCdmaEcio);
        out.writeInt(mEvdoDbm);
        out.writeInt(mEvdoEcio);
        out.writeInt(mEvdoSnr);
        out.writeInt(isGsm ? 1 : 0);
    }
    public int describeContents() {
        return 0;
    }
    public static final Parcelable.Creator<SignalStrength> CREATOR = new Parcelable.Creator() {
        public SignalStrength createFromParcel(Parcel in) {
            return new SignalStrength(in);
        }
        public SignalStrength[] newArray(int size) {
            return new SignalStrength[size];
        }
    };
    public int getGsmSignalStrength() {
        return this.mGsmSignalStrength;
    }
    public int getGsmBitErrorRate() {
        return this.mGsmBitErrorRate;
    }
    public int getCdmaDbm() {
        return this.mCdmaDbm;
    }
    public int getCdmaEcio() {
        return this.mCdmaEcio;
    }
    public int getEvdoDbm() {
        return this.mEvdoDbm;
    }
    public int getEvdoEcio() {
        return this.mEvdoEcio;
    }
    public int getEvdoSnr() {
        return this.mEvdoSnr;
    }
    public boolean isGsm() {
        return this.isGsm;
    }
    @Override
    public int hashCode() {
        return ((mGsmSignalStrength * 0x1234)
                + mGsmBitErrorRate
                + mCdmaDbm + mCdmaEcio
                + mEvdoDbm + mEvdoEcio + mEvdoSnr
                + (isGsm ? 1 : 0));
    }
    @Override
    public boolean equals (Object o) {
        SignalStrength s;
        try {
            s = (SignalStrength) o;
        } catch (ClassCastException ex) {
            return false;
        }
        if (o == null) {
            return false;
        }
        return (mGsmSignalStrength == s.mGsmSignalStrength
                && mGsmBitErrorRate == s.mGsmBitErrorRate
                && mCdmaDbm == s.mCdmaDbm
                && mCdmaEcio == s.mCdmaEcio
                && mEvdoDbm == s.mEvdoDbm
                && mEvdoEcio == s.mEvdoEcio
                && mEvdoSnr == s.mEvdoSnr
                && isGsm == s.isGsm);
    }
    @Override
    public String toString() {
        return ("SignalStrength:"
                + " " + mGsmSignalStrength
                + " " + mGsmBitErrorRate
                + " " + mCdmaDbm
                + " " + mCdmaEcio
                + " " + mEvdoDbm
                + " " + mEvdoEcio
                + " " + mEvdoSnr
                + " " + (isGsm ? "gsm" : "cdma"));
    }
    private static boolean equalsHandlesNulls (Object a, Object b) {
        return (a == null) ? (b == null) : a.equals (b);
    }
    private void setFromNotifierBundle(Bundle m) {
        mGsmSignalStrength = m.getInt("GsmSignalStrength");
        mGsmBitErrorRate = m.getInt("GsmBitErrorRate");
        mCdmaDbm = m.getInt("CdmaDbm");
        mCdmaEcio = m.getInt("CdmaEcio");
        mEvdoDbm = m.getInt("EvdoDbm");
        mEvdoEcio = m.getInt("EvdoEcio");
        mEvdoSnr = m.getInt("EvdoSnr");
        isGsm = m.getBoolean("isGsm");
    }
    public void fillInNotifierBundle(Bundle m) {
        m.putInt("GsmSignalStrength", mGsmSignalStrength);
        m.putInt("GsmBitErrorRate", mGsmBitErrorRate);
        m.putInt("CdmaDbm", mCdmaDbm);
        m.putInt("CdmaEcio", mCdmaEcio);
        m.putInt("EvdoDbm", mEvdoDbm);
        m.putInt("EvdoEcio", mEvdoEcio);
        m.putInt("EvdoSnr", mEvdoSnr);
        m.putBoolean("isGsm", Boolean.valueOf(isGsm));
    }
}

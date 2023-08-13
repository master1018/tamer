public class KerberosTime implements Cloneable {
    private long kerberosTime; 
    private int  microSeconds; 
    private static final long initMilli = System.currentTimeMillis();
    private static final long initMicro = System.nanoTime() / 1000;
    private static long syncTime;
    private static boolean DEBUG = Krb5.DEBUG;
    public static final boolean NOW = true;
    public static final boolean UNADJUSTED_NOW = false;
    public KerberosTime(long time) {
        kerberosTime = time;
    }
    private KerberosTime(long time, int micro) {
        kerberosTime = time;
        microSeconds = micro;
    }
    public Object clone() {
        return new KerberosTime(kerberosTime, microSeconds);
    }
    public KerberosTime(String time) throws Asn1Exception {
        kerberosTime = toKerberosTime(time);
    }
    public KerberosTime(DerValue encoding) throws Asn1Exception, IOException {
        GregorianCalendar calendar = new GregorianCalendar();
        Date temp = encoding.getGeneralizedTime();
        kerberosTime = temp.getTime();
    }
    private static long toKerberosTime(String time) throws Asn1Exception {
        if (time.length() != 15)
            throw new Asn1Exception(Krb5.ASN1_BAD_TIMEFORMAT);
        if (time.charAt(14) != 'Z')
            throw new Asn1Exception(Krb5.ASN1_BAD_TIMEFORMAT);
        int year = Integer.parseInt(time.substring(0, 4));
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear(); 
        calendar.set(year,
                     Integer.parseInt(time.substring(4, 6)) - 1,
                     Integer.parseInt(time.substring(6, 8)),
                     Integer.parseInt(time.substring(8, 10)),
                     Integer.parseInt(time.substring(10, 12)),
                     Integer.parseInt(time.substring(12, 14)));
        return (calendar.getTime().getTime());
    }
    public static String zeroPad(String s, int length) {
        StringBuffer temp = new StringBuffer(s);
        while (temp.length() < length)
            temp.insert(0, '0');
        return temp.toString();
    }
    public KerberosTime(Date time) {
        kerberosTime = time.getTime(); 
    }
    public KerberosTime(boolean initToNow) {
        if (initToNow) {
            setNow();
        }
    }
    public String toGeneralizedTimeString() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.setTimeInMillis(kerberosTime);
        return zeroPad(Integer.toString(calendar.get(Calendar.YEAR)), 4) +
            zeroPad(Integer.toString(calendar.get(Calendar.MONTH) + 1), 2) +
            zeroPad(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)), 2) +
            zeroPad(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)), 2) +
            zeroPad(Integer.toString(calendar.get(Calendar.MINUTE)), 2) +
            zeroPad(Integer.toString(calendar.get(Calendar.SECOND)), 2) + 'Z';
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream out = new DerOutputStream();
        out.putGeneralizedTime(this.toDate());
        return out.toByteArray();
    }
    public long getTime() {
        return kerberosTime;
    }
    public void setTime(Date time) {
        kerberosTime = time.getTime(); 
        microSeconds = 0;
    }
    public void setTime(long time) {
        kerberosTime = time;
        microSeconds = 0;
    }
    public Date toDate() {
        Date temp = new Date(kerberosTime);
        temp.setTime(temp.getTime());
        return temp;
    }
    public void setNow() {
        long microElapsed = System.nanoTime() / 1000 - initMicro;
        setTime(initMilli + microElapsed/1000);
        microSeconds = (int)(microElapsed % 1000);
    }
    public int getMicroSeconds() {
        Long temp_long = new Long((kerberosTime % 1000L) * 1000L);
        return temp_long.intValue() + microSeconds;
    }
    public void setMicroSeconds(int usec) {
        microSeconds = usec % 1000;
        Integer temp_int = new Integer(usec);
        long temp_long = temp_int.longValue() / 1000L;
        kerberosTime = kerberosTime - (kerberosTime % 1000L) + temp_long;
    }
    public void setMicroSeconds(Integer usec) {
        if (usec != null) {
            microSeconds = usec.intValue() % 1000;
            long temp_long = usec.longValue() / 1000L;
            kerberosTime = kerberosTime - (kerberosTime % 1000L) + temp_long;
        }
    }
    public boolean inClockSkew(int clockSkew) {
        KerberosTime now = new KerberosTime(KerberosTime.NOW);
        if (java.lang.Math.abs(kerberosTime - now.kerberosTime) >
            clockSkew * 1000L)
            return false;
        return true;
    }
    public boolean inClockSkew() {
        return inClockSkew(getDefaultSkew());
    }
    public boolean inClockSkew(int clockSkew, KerberosTime now) {
        if (java.lang.Math.abs(kerberosTime - now.kerberosTime) >
            clockSkew * 1000L)
            return false;
        return true;
    }
    public boolean inClockSkew(KerberosTime time) {
        return inClockSkew(getDefaultSkew(), time);
    }
    public boolean greaterThanWRTClockSkew(KerberosTime time, int clockSkew) {
        if ((kerberosTime - time.kerberosTime) > clockSkew * 1000L)
            return true;
        return false;
    }
    public boolean greaterThanWRTClockSkew(KerberosTime time) {
        return greaterThanWRTClockSkew(time, getDefaultSkew());
    }
    public boolean greaterThan(KerberosTime time) {
        return kerberosTime > time.kerberosTime ||
            kerberosTime == time.kerberosTime &&
                    microSeconds > time.microSeconds;
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KerberosTime)) {
            return false;
        }
        return kerberosTime == ((KerberosTime)obj).kerberosTime &&
                microSeconds == ((KerberosTime)obj).microSeconds;
    }
    public int hashCode() {
        int result = 37 * 17 + (int)(kerberosTime ^ (kerberosTime >>> 32));
        return result * 17 + microSeconds;
    }
    public boolean isZero() {
        return kerberosTime == 0 && microSeconds == 0;
    }
    public int getSeconds() {
        Long temp_long = new Long(kerberosTime / 1000L);
        return temp_long.intValue();
    }
    public void setSeconds(int sec) {
        Integer temp_int = new Integer(sec);
        kerberosTime = temp_int.longValue() * 1000L;
    }
    public static KerberosTime parse(DerInputStream data, byte explicitTag, boolean optional) throws Asn1Exception, IOException {
        if ((optional) && (((byte)data.peekByte() & (byte)0x1F)!= explicitTag))
            return null;
        DerValue der = data.getDerValue();
        if (explicitTag != (der.getTag() & (byte)0x1F))  {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        else {
            DerValue subDer = der.getData().getDerValue();
            return new KerberosTime(subDer);
        }
    }
    public static int getDefaultSkew() {
        int tdiff = Krb5.DEFAULT_ALLOWABLE_CLOCKSKEW;
        try {
            Config c = Config.getInstance();
            if ((tdiff = c.getDefaultIntValue("clockskew",
                                              "libdefaults")) == Integer.MIN_VALUE) {   
                tdiff = Krb5.DEFAULT_ALLOWABLE_CLOCKSKEW;
            }
        } catch (KrbException e) {
            if (DEBUG) {
                System.out.println("Exception in getting clockskew from " +
                                   "Configuration " +
                                   "using default value " +
                                   e.getMessage());
            }
        }
        return tdiff;
    }
    public String toString() {
        return toGeneralizedTimeString();
    }
}

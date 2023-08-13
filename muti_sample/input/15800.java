public class AuthTime {
    long kerberosTime;
    int cusec;
    public AuthTime(long time, int c) {
        kerberosTime = time;
        cusec = c;
    }
    public boolean equals(Object o) {
        if (o instanceof AuthTime) {
            if ((((AuthTime)o).kerberosTime == kerberosTime)
                && (((AuthTime)o).cusec == cusec)) {
                return true;
            }
        }
        return false;
    }
    public int hashCode() {
        int result = 17;
        result = 37 * result + (int)(kerberosTime ^ (kerberosTime >>> 32));
        result = 37 * result + cusec;
        return result;
    }
}

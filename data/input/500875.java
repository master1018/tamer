public class Support_TimeZone extends TimeZone {
    private static final long serialVersionUID = 1L;
    int rawOffset;
    boolean useDaylightTime;
    public Support_TimeZone(int rawOffset, boolean useDaylightTime) {
        this.rawOffset = rawOffset;
        this.useDaylightTime = useDaylightTime;
    }
    @Override
    public int getRawOffset() {
        return rawOffset;
    }
    @Override
    public boolean inDaylightTime(java.util.Date p1) {
        if (!useDaylightTime) {
            return false;
        }
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(p1);
        int month = cal.get(Calendar.MONTH);
        if (month > 4 && month < 10) {
            return true;
        }
        return false;
    }
    @Override
    public boolean useDaylightTime() {
        return useDaylightTime;
    }
    @Override
    public int getOffset(int p1, int p2, int p3, int p4, int p5, int p6) {
        return 0;
    }
    @Override
    public void setRawOffset(int p1) {
        rawOffset = p1;
    }
}

public class Timestamp extends Date {
    private static final long serialVersionUID = 2745179027874758501L;
    private int nanos;
    private static final String TIME_FORMAT_REGEX = "[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.*"; 
    @SuppressWarnings("deprecation")
    @Deprecated
    public Timestamp(int theYear, int theMonth, int theDate, int theHour,
            int theMinute, int theSecond, int theNano)
            throws IllegalArgumentException {
        super(theYear, theMonth, theDate, theHour, theMinute, theSecond);
        if (theNano < 0 || theNano > 999999999) {
            throw new IllegalArgumentException();
        }
        nanos = theNano;
    }
    public Timestamp(long theTime) {
        super(theTime);
        setTimeImpl(theTime);
    }
    public boolean after(Timestamp theTimestamp) {
        long thisTime = this.getTime();
        long compareTime = theTimestamp.getTime();
        if (thisTime > compareTime) {
            return true;
        }
        else if (thisTime < compareTime) {
            return false;
        }
        else if (this.getNanos() > theTimestamp.getNanos()) {
            return true;
        } else {
            return false;
        }
    }
    public boolean before(Timestamp theTimestamp) {
        long thisTime = this.getTime();
        long compareTime = theTimestamp.getTime();
        if (thisTime < compareTime) {
            return true;
        }
        else if (thisTime > compareTime) {
            return false;
        }
        else if (this.getNanos() < theTimestamp.getNanos()) {
            return true;
        } else {
            return false;
        }
    }
    @Override
    public int compareTo(Date theObject) throws ClassCastException {
        return this.compareTo((Timestamp) theObject);
    }
    public int compareTo(Timestamp theTimestamp) {
        int result = super.compareTo(theTimestamp);
        if (result == 0) {
            int thisNano = this.getNanos();
            int thatNano = theTimestamp.getNanos();
            if (thisNano > thatNano) {
                return 1;
            } else if (thisNano == thatNano) {
                return 0;
            } else {
                return -1;
            }
        }
        return result;
    }
    @Override
    public boolean equals(Object theObject) {
        if (theObject instanceof Timestamp) {
            return equals((Timestamp) theObject);
        }
        return false;
    }
    public boolean equals(Timestamp theTimestamp) {
        if (theTimestamp == null) {
            return false;
        }
        return (this.getTime() == theTimestamp.getTime())
                && (this.getNanos() == theTimestamp.getNanos());
    }
    public int getNanos() {
        return nanos;
    }
    @Override
    public long getTime() {
        long theTime = super.getTime();
        theTime = theTime + (nanos / 1000000);
        return theTime;
    }
    public void setNanos(int n) throws IllegalArgumentException {
        if ((n < 0) || (n > 999999999)) {
            throw new IllegalArgumentException(Messages.getString("sql.0")); 
        }
        nanos = n;
    }
    @Override
    public void setTime(long theTime) {
        setTimeImpl(theTime);
    }
    private void setTimeImpl(long theTime) {
        int milliseconds = (int) (theTime % 1000);
        theTime = theTime - milliseconds;
        if (milliseconds < 0) {
            theTime = theTime - 1000;
            milliseconds = 1000 + milliseconds;
        }
        super.setTime(theTime);
        setNanos(milliseconds * 1000000);
    }
    @SuppressWarnings("deprecation")
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(29);
        format((getYear() + 1900), 4, sb);
        sb.append('-');
        format((getMonth() + 1), 2, sb);
        sb.append('-');
        format(getDate(), 2, sb);
        sb.append(' ');
        format(getHours(), 2, sb);
        sb.append(':');
        format(getMinutes(), 2, sb);
        sb.append(':');
        format(getSeconds(), 2, sb);
        sb.append('.');
        if (nanos == 0) {
            sb.append('0');
        } else {
            format(nanos, 9, sb);
            while (sb.charAt(sb.length() - 1) == '0') {
                sb.setLength(sb.length() - 1);
            }
        }
        return sb.toString();
    }
    private static final String PADDING = "000000000";  
    private void format(int date, int digits, StringBuilder sb) { 
        String str = String.valueOf(date);
        if (digits - str.length() > 0) {
            sb.append(PADDING.substring(0, digits - str.length()));
        }
        sb.append(str); 
    }
    public static Timestamp valueOf(String s) throws IllegalArgumentException {
        if (s == null) {
            throw new IllegalArgumentException(Messages.getString("sql.3")); 
        }
        s = s.trim();
        if (!Pattern.matches(TIME_FORMAT_REGEX, s)) {
            throw new IllegalArgumentException(Messages.getString("sql.2")); 
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
        ParsePosition pp = new ParsePosition(0);
        Date theDate;
        try {
            theDate = df.parse(s, pp);
        } catch (Exception e) {
            throw new IllegalArgumentException(Messages.getString("sql.2")); 
        }
        if (theDate == null) {
            throw new IllegalArgumentException(Messages.getString("sql.2")); 
        }
        int position = pp.getIndex();
        int remaining = s.length() - position;
        int theNanos;
        if (remaining == 0) {
            theNanos = 0;
        } else {
            if ((s.length() - position) < ".n".length()) { 
                throw new IllegalArgumentException(Messages.getString("sql.2")); 
            }
            if ((s.length() - position) > ".nnnnnnnnn".length()) { 
                throw new IllegalArgumentException(Messages.getString("sql.2")); 
            }
            if (s.charAt(position) != '.') {
                throw new NumberFormatException(Messages.getString(
                        "sql.4", s.charAt(position))); 
            }
            int nanoLength = s.length() - position - 1;
            String theNanoString = s.substring(position + 1, position + 1
                    + nanoLength);
            theNanoString = theNanoString + "000000000"; 
            theNanoString = theNanoString.substring(0, 9);
            try {
                theNanos = Integer.parseInt(theNanoString);
            } catch (Exception e) {
                throw new IllegalArgumentException(Messages.getString("sql.2")); 
            }
        }
        if (theNanos < 0 || theNanos > 999999999) {
            throw new IllegalArgumentException(Messages.getString("sql.2")); 
        }
        Timestamp theTimestamp = new Timestamp(theDate.getTime());
        theTimestamp.setNanos(theNanos);
        return theTimestamp;
    }
}

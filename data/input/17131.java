public class Date extends java.util.Date {
    public Date(int year, int month, int day) {
        super(year, month, day);
    }
    public Date(long date) {
        super(date);
    }
    public void setTime(long date) {
        super.setTime(date);
    }
    public static Date valueOf(String s) {
        final int YEAR_LENGTH = 4;
        final int MONTH_LENGTH = 2;
        final int DAY_LENGTH = 2;
        final int MAX_MONTH = 12;
        final int MAX_DAY = 31;
        int firstDash;
        int secondDash;
        Date d = null;
        if (s == null) {
            throw new java.lang.IllegalArgumentException();
        }
        firstDash = s.indexOf('-');
        secondDash = s.indexOf('-', firstDash + 1);
        if ((firstDash > 0) && (secondDash > 0) && (secondDash < s.length() - 1)) {
            String yyyy = s.substring(0, firstDash);
            String mm = s.substring(firstDash + 1, secondDash);
            String dd = s.substring(secondDash + 1);
            if (yyyy.length() == YEAR_LENGTH &&
                    (mm.length() >= 1 && mm.length() <= MONTH_LENGTH) &&
                    (dd.length() >= 1 && dd.length() <= DAY_LENGTH)) {
                int year = Integer.parseInt(yyyy);
                int month = Integer.parseInt(mm);
                int day = Integer.parseInt(dd);
                if ((month >= 1 && month <= MAX_MONTH) && (day >= 1 && day <= MAX_DAY)) {
                    d = new Date(year - 1900, month - 1, day);
                }
            }
        }
        if (d == null) {
            throw new java.lang.IllegalArgumentException();
        }
        return d;
    }
    public String toString () {
        int year = super.getYear() + 1900;
        int month = super.getMonth() + 1;
        int day = super.getDate();
        char buf[] = "2000-00-00".toCharArray();
        buf[0] = Character.forDigit(year/1000,10);
        buf[1] = Character.forDigit((year/100)%10,10);
        buf[2] = Character.forDigit((year/10)%10,10);
        buf[3] = Character.forDigit(year%10,10);
        buf[5] = Character.forDigit(month/10,10);
        buf[6] = Character.forDigit(month%10,10);
        buf[8] = Character.forDigit(day/10,10);
        buf[9] = Character.forDigit(day%10,10);
        return new String(buf);
    }
    public int getHours() {
        throw new java.lang.IllegalArgumentException();
    }
    public int getMinutes() {
        throw new java.lang.IllegalArgumentException();
    }
    public int getSeconds() {
        throw new java.lang.IllegalArgumentException();
    }
    public void setHours(int i) {
        throw new java.lang.IllegalArgumentException();
    }
    public void setMinutes(int i) {
        throw new java.lang.IllegalArgumentException();
    }
    public void setSeconds(int i) {
        throw new java.lang.IllegalArgumentException();
    }
    static final long serialVersionUID = 1511598038487230103L;
}

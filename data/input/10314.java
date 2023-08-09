public class CK_DATE implements Cloneable {
    public char[] year;    
    public char[] month;   
    public char[] day;     
    public CK_DATE(char[] year, char[] month, char[] day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }
    public Object clone() {
        CK_DATE copy = null;
        try {
            copy = (CK_DATE) super.clone();
        } catch (CloneNotSupportedException cnse) {
            throw (RuntimeException)
                (new RuntimeException("Clone error").initCause(cnse));
        }
        copy.year = this.year.clone();
        copy.month = this.month.clone();
        copy.day =  this.day.clone();
        return copy;
    }
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(new String(day));
        buffer.append('.');
        buffer.append(new String(month));
        buffer.append('.');
        buffer.append(new String(year));
        buffer.append(" (DD.MM.YYYY)");
        return buffer.toString();
    }
}

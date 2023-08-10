public class GregorianCalendar extends java.util.GregorianCalendar implements SCO {
    protected transient Object owner;
    protected transient StateManager ownerSM;
    protected transient String fieldName;
    public GregorianCalendar(StateManager ownerSM, String fieldName) {
        super();
        this.ownerSM = ownerSM;
        this.owner = ownerSM.getObject();
        this.fieldName = fieldName;
    }
    public void initialise() {
    }
    public void initialise(Object o, boolean forInsert, boolean forUpdate) {
        java.util.Calendar cal = (java.util.Calendar) o;
        super.setTimeInMillis(cal.getTime().getTime());
        super.setTimeZone(cal.getTimeZone());
    }
    public Object getValue() {
        java.util.GregorianCalendar cal = new java.util.GregorianCalendar(getTimeZone());
        cal.setTime(getTime());
        return cal;
    }
    public void unsetOwner() {
        ownerSM = null;
    }
    public Object getOwner() {
        return (ownerSM != null ? ownerSM.getObject() : null);
    }
    public String getFieldName() {
        return this.fieldName;
    }
    public void makeDirty() {
        if (owner != null) {
            ((PersistenceCapable) owner).jdoMakeDirty(fieldName);
        }
    }
    public Object detachCopy(FetchPlanState state) {
        java.util.GregorianCalendar cal = new java.util.GregorianCalendar(getTimeZone());
        cal.setTime(getTime());
        return cal;
    }
    public void attachCopy(Object value) {
        long oldValue = getTimeInMillis();
        initialise(value, false, true);
        long newValue = ((java.util.Calendar) value).getTime().getTime();
        if (oldValue != newValue) {
            makeDirty();
        }
    }
    public Object clone() {
        Object obj = super.clone();
        ((GregorianCalendar) obj).unsetOwner();
        return obj;
    }
    protected Object writeReplace() throws ObjectStreamException {
        java.util.GregorianCalendar cal = new java.util.GregorianCalendar(this.getTimeZone());
        cal.setTime(this.getTime());
        return cal;
    }
    public void add(int field, int amount) {
        super.add(field, amount);
        makeDirty();
    }
    public void roll(int field, boolean up) {
        super.roll(field, up);
        makeDirty();
    }
    public void roll(int field, int amount) {
        super.roll(field, amount);
        makeDirty();
    }
    public void set(int field, int value) {
        super.set(field, value);
        makeDirty();
    }
    public void setGregorianChange(Date date) {
        super.setGregorianChange(date);
        makeDirty();
    }
    public void setFirstDayOfWeek(int value) {
        super.setFirstDayOfWeek(value);
        makeDirty();
    }
    public void setLenient(boolean lenient) {
        super.setLenient(lenient);
        makeDirty();
    }
    public void setMinimalDaysInFirstWeek(int value) {
        super.setMinimalDaysInFirstWeek(value);
        makeDirty();
    }
    public void setTimeInMillis(long millis) {
        super.setTimeInMillis(millis);
        makeDirty();
    }
    public void setTimeZone(TimeZone value) {
        super.setTimeZone(value);
        makeDirty();
    }
}

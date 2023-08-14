public final class Era {
    private final String name;
    private final String abbr;
    private final long since;
    private final CalendarDate sinceDate;
    private final boolean localTime;
    public Era(String name, String abbr, long since, boolean localTime) {
        this.name = name;
        this.abbr = abbr;
        this.since = since;
        this.localTime = localTime;
        Gregorian gcal = CalendarSystem.getGregorianCalendar();
        BaseCalendar.Date d = (BaseCalendar.Date) gcal.newCalendarDate(null);
        gcal.getCalendarDate(since, d);
        sinceDate = new ImmutableGregorianDate(d);
    }
    public String getName() {
        return name;
    }
    public String getDisplayName(Locale locale) {
        return name;
    }
    public String getAbbreviation() {
        return abbr;
    }
    public String getDiaplayAbbreviation(Locale locale) {
        return abbr;
    }
    public long getSince(TimeZone zone) {
        if (zone == null || !localTime) {
            return since;
        }
        int offset = zone.getOffset(since);
        return since - offset;
    }
    public CalendarDate getSinceDate() {
        return sinceDate;
    }
    public boolean isLocalTime() {
        return localTime;
    }
    public boolean equals(Object o) {
        if (!(o instanceof Era)) {
            return false;
        }
        Era that = (Era) o;
        return name.equals(that.name)
            && abbr.equals(that.abbr)
            && since == that.since
            && localTime == that.localTime;
    }
    private int hash = 0;
    public int hashCode() {
        if (hash == 0) {
            hash = name.hashCode() ^ abbr.hashCode() ^ (int)since ^ (int)(since >> 32)
                ^ (localTime ? 1 : 0);
        }
        return hash;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(getName()).append(" (");
        sb.append(getAbbreviation()).append(')');
        sb.append(" since ").append(getSinceDate());
        if (localTime) {
            sb.setLength(sb.length() - 1); 
            sb.append(" local time");
        }
        sb.append(']');
        return sb.toString();
    }
}

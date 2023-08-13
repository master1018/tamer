public final class FileTime
    implements Comparable<FileTime>
{
    private final long value;
    private final TimeUnit unit;
    private String valueAsString;
    private DaysAndNanos daysAndNanos;
    private DaysAndNanos asDaysAndNanos() {
        if (daysAndNanos == null)
            daysAndNanos = new DaysAndNanos(value, unit);
        return daysAndNanos;
    }
    private FileTime(long value, TimeUnit unit) {
        if (unit == null)
            throw new NullPointerException();
        this.value = value;
        this.unit = unit;
    }
    public static FileTime from(long value, TimeUnit unit) {
        return new FileTime(value, unit);
    }
    public static FileTime fromMillis(long value) {
        return new FileTime(value, TimeUnit.MILLISECONDS);
    }
    public long to(TimeUnit unit) {
        return unit.convert(this.value, this.unit);
    }
    public long toMillis() {
        return unit.toMillis(value);
    }
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof FileTime) ? compareTo((FileTime)obj) == 0 : false;
    }
    @Override
    public int hashCode() {
        return asDaysAndNanos().hashCode();
    }
    @Override
    public int compareTo(FileTime other) {
        if (unit == other.unit) {
            return (value < other.value) ? -1 : (value == other.value ? 0 : 1);
        } else {
            return asDaysAndNanos().compareTo(other.asDaysAndNanos());
        }
    }
    @Override
    public String toString() {
        String v = valueAsString;
        if (v == null) {
            long ms = toMillis();
            String fractionAsString = "";
            if (unit.compareTo(TimeUnit.SECONDS) < 0) {
                long fraction = asDaysAndNanos().fractionOfSecondInNanos();
                if (fraction != 0L) {
                    if (fraction < 0L) {
                        final long MAX_FRACTION_PLUS_1 = 1000L * 1000L * 1000L;
                        fraction += MAX_FRACTION_PLUS_1;
                        if (ms != Long.MIN_VALUE) ms--;
                    }
                    String s = Long.toString(fraction);
                    int len = s.length();
                    int width = 9 - len;
                    StringBuilder sb = new StringBuilder(".");
                    while (width-- > 0) {
                        sb.append('0');
                    }
                    if (s.charAt(len-1) == '0') {
                        len--;
                        while (s.charAt(len-1) == '0')
                            len--;
                        sb.append(s.substring(0, len));
                    } else {
                        sb.append(s);
                    }
                    fractionAsString = sb.toString();
                }
            }
            GregorianCalendar cal =
                new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.ROOT);
            if (value < 0L)
                cal.setGregorianChange(new Date(Long.MIN_VALUE));
            cal.setTimeInMillis(ms);
            String sign = (cal.get(Calendar.ERA) == GregorianCalendar.BC) ? "-" : "";
            v = new Formatter(Locale.ROOT)
                .format("%s%tFT%tR:%tS%sZ", sign, cal, cal, cal, fractionAsString)
                .toString();
            valueAsString = v;
        }
        return v;
    }
    private static class DaysAndNanos implements Comparable<DaysAndNanos> {
        private static final long C0 = 1L;
        private static final long C1 = C0 * 24L;
        private static final long C2 = C1 * 60L;
        private static final long C3 = C2 * 60L;
        private static final long C4 = C3 * 1000L;
        private static final long C5 = C4 * 1000L;
        private static final long C6 = C5 * 1000L;
        private final long days;
        private final long excessNanos;
        DaysAndNanos(long value, TimeUnit unit) {
            long scale;
            switch (unit) {
                case DAYS         : scale = C0; break;
                case HOURS        : scale = C1; break;
                case MINUTES      : scale = C2; break;
                case SECONDS      : scale = C3; break;
                case MILLISECONDS : scale = C4; break;
                case MICROSECONDS : scale = C5; break;
                case NANOSECONDS  : scale = C6; break;
                default : throw new AssertionError("Unit not handled");
            }
            this.days = unit.toDays(value);
            this.excessNanos = unit.toNanos(value - (this.days * scale));
        }
        long fractionOfSecondInNanos() {
            return excessNanos % (1000L * 1000L * 1000L);
        }
        @Override
        public boolean equals(Object obj) {
            return (obj instanceof DaysAndNanos) ?
                compareTo((DaysAndNanos)obj) == 0 : false;
        }
        @Override
        public int hashCode() {
            return (int)(days ^ (days >>> 32) ^
                         excessNanos ^ (excessNanos >>> 32));
        }
        @Override
        public int compareTo(DaysAndNanos other) {
            if (this.days != other.days)
                return (this.days < other.days) ? -1 : 1;
            return (this.excessNanos < other.excessNanos) ? -1 :
                   (this.excessNanos == other.excessNanos) ? 0 : 1;
        }
    }
}

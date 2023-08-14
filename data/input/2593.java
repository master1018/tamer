public class Scale {
    private static int nextOrdinal = 0;
    private static HashMap<String, Scale> map = new HashMap<String, Scale>();
    private final String name;
    private final int ordinal = nextOrdinal++;
    private final double factor;
    private Scale(String name, double factor) {
        this.name = name;
        this.factor = factor;
        assert !map.containsKey(name);
        map.put(name, this);
    }
    public static final Scale RAW = new Scale("raw", 1);
    public static final Scale PERCENT = new Scale("percent", 1/100);
    public static final Scale KILO = new Scale("K", 1024);
    public static final Scale MEGA = new Scale("M", 1024*1024);
    public static final Scale GIGA = new Scale("G", 1024*1024*1024);
    public static final Scale TERA = new Scale("T", 1024*1024*1024*1024);
    public static final Scale PETA = new Scale("P", 1024*1024*1024*1024*1024);
    public static final Scale PICO = new Scale("p", 10.0E-12);
    public static final Scale NANO = new Scale("n", 10.0E-9);
    public static final Scale MICRO = new Scale("u", 10.0E-6);
    public static final Scale MILLI = new Scale("m", 10.0E-3);
    public static final Scale PSEC = new Scale("ps", 10.0E-12);
    public static final Scale NSEC = new Scale("ns", 10.0E-9);
    public static final Scale USEC = new Scale("us", 10.0E-6);
    public static final Scale MSEC = new Scale("ms", 10.0E-3);
    public static final Scale SEC = new Scale("s", 1);
    public static final Scale SEC2 = new Scale("sec", 1);
    public static final Scale MINUTES = new Scale("min", 1/60.0);
    public static final Scale HOUR = new Scale("h", 1/(60.0*60.0));
    public static final Scale HOUR2 = new Scale("hour", 1/(60.0*60.0));
    public double getFactor() {
        return factor;
    }
    public String toString() {
        return name;
    }
    public static Scale toScale(String s) {
        return map.get(s);
    }
    protected static Set keySet() {
        return map.keySet();
    }
    protected double scale(double value) {
        return value/factor;
    }
}

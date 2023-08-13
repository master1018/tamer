public abstract class AbstractCounter implements Counter {
    String name;
    Units units;
    Variability variability;
    int flags;
    int vectorLength;
    class Flags {
        static final int SUPPORTED = 0x1;
    }
    protected AbstractCounter(String name, Units units,
                              Variability variability, int flags,
                              int vectorLength) {
        this.name = name;
        this.units = units;
        this.variability = variability;
        this.flags = flags;
        this.vectorLength = vectorLength;
    }
    protected AbstractCounter(String name, Units units,
                              Variability variability, int flags) {
        this(name, units, variability, flags, 0);
    }
    public String getName() {
        return name;
    }
    public Units getUnits() {
        return units;
    }
    public Variability getVariability() {
        return variability;
    }
    public boolean isVector() {
        return vectorLength > 0;
    }
    public int getVectorLength() {
        return vectorLength;
    }
    public boolean isInternal() {
        return (flags & Flags.SUPPORTED) == 0;
    }
    public int getFlags() {
        return flags;
    }
    public abstract Object getValue();
    public String toString() {
        String result = getName() + ": " + getValue() + " " + getUnits();
        if (isInternal()) {
            return result + " [INTERNAL]";
        } else {
            return result;
        }
    }
    private static final long serialVersionUID = 6992337162326171013L;
}

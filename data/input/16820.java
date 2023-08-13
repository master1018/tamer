public abstract class AbstractMonitor implements Monitor  {
    protected String name;
    protected Units units;
    protected Variability variability;
    protected int vectorLength;
    protected boolean supported;
    protected AbstractMonitor(String name, Units units, Variability variability,
                              boolean supported, int vectorLength) {
        this.name = name;
        this.units = units;
        this.variability = variability;
        this.vectorLength = vectorLength;
        this.supported = supported;
    }
    protected AbstractMonitor(String name, Units units, Variability variability,
                              boolean supported) {
        this(name, units, variability, supported, 0);
    }
    public String getName() {
        return name;
    }
    public String getBaseName() {
        int baseIndex = name.lastIndexOf(".")+1;
        return name.substring(baseIndex);
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
    public boolean isSupported() {
        return supported;
    }
    public abstract Object getValue();
}

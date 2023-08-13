public final class GeneralPath extends Path2D.Float {
    public GeneralPath() {
        super(WIND_NON_ZERO, INIT_SIZE);
    }
    public GeneralPath(int rule) {
        super(rule, INIT_SIZE);
    }
    public GeneralPath(int rule, int initialCapacity) {
        super(rule, initialCapacity);
    }
    public GeneralPath(Shape s) {
        super(s, null);
    }
    GeneralPath(int windingRule,
                byte[] pointTypes,
                int numTypes,
                float[] pointCoords,
                int numCoords)
    {
        this.windingRule = windingRule;
        this.pointTypes = pointTypes;
        this.numTypes = numTypes;
        this.floatCoords = pointCoords;
        this.numCoords = numCoords;
    }
    private static final long serialVersionUID = -8327096662768731142L;
}

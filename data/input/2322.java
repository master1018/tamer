public abstract class GraphicsConfigTemplate implements Serializable {
    private static final long serialVersionUID = -8061369279557787079L;
    public GraphicsConfigTemplate() {
    }
    public static final int REQUIRED    = 1;
    public static final int PREFERRED   = 2;
    public static final int UNNECESSARY = 3;
    public abstract GraphicsConfiguration
      getBestConfiguration(GraphicsConfiguration[] gc);
    public abstract boolean
      isGraphicsConfigSupported(GraphicsConfiguration gc);
}

public class MethodInfo implements java.io.Serializable {
    private String name;
    private long type;
    private int compileSize;
    MethodInfo(String name, long type, int compileSize) {
        this.name = name;
        this.type = type;
        this.compileSize = compileSize;
    }
    public String getName() {
        return name;
    }
    public long getType() {
        return type;
    }
    public int getCompileSize() {
        return compileSize;
    }
    public String toString() {
        return getName() + " type = " + getType() +
            " compileSize = " + getCompileSize();
    }
    private static final long serialVersionUID = 6992337162326171013L;
}

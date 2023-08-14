public class InputBytecode {
    private int bci;
    private String name;
    private InputMethod inlined;
    public InputBytecode(int bci, String name) {
        this.bci = bci;
        this.name = name;
    }
    public InputMethod getInlined() {
        return inlined;
    }
    public void setInlined(InputMethod inlined) {
        this.inlined = inlined;
    }
    public int getBci() {
        return bci;
    }
    public String getName() {
        return name;
    }
}

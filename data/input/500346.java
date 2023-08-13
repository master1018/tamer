public class CompilationUnit {
    private String name;
    private String source;
    public CompilationUnit(String name, String source) {
        this.name = name;
        this.source = source;
    }
    public String getName() {
        return name;
    }
    public String getSource() {
        return source;
    }
}

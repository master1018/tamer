public class ClassManager {
    private SearchPath classPath;
    public ClassManager(Environment env) {
        this.classPath = new SearchPath("");
    }
    public ClassManager(SearchPath classPath) {
        this.classPath = classPath;
    }
    public void setClassPath(SearchPath sp) {
        classPath = sp;
    }
    public SearchPath getClassPath() {
        return classPath;
    }
}

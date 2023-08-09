public abstract class JavacTask implements CompilationTask {
    public abstract Iterable<? extends CompilationUnitTree> parse()
        throws IOException;
    public abstract Iterable<? extends Element> analyze() throws IOException;
    public abstract Iterable<? extends JavaFileObject> generate() throws IOException;
    public abstract void setTaskListener(TaskListener taskListener);
    public abstract TypeMirror getTypeMirror(Iterable<? extends Tree> path);
    public abstract Elements getElements();
    public abstract Types getTypes();
}

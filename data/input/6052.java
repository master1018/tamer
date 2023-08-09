public final class TaskEvent
{
    public enum Kind {
        PARSE,
        ENTER,
        ANALYZE,
        GENERATE,
        ANNOTATION_PROCESSING,
        ANNOTATION_PROCESSING_ROUND
    };
    public TaskEvent(Kind kind) {
        this(kind, null, null, null);
    }
    public TaskEvent(Kind kind, JavaFileObject sourceFile) {
        this(kind, sourceFile, null, null);
    }
    public TaskEvent(Kind kind, CompilationUnitTree unit) {
        this(kind, unit.getSourceFile(), unit, null);
    }
    public TaskEvent(Kind kind, CompilationUnitTree unit, TypeElement clazz) {
        this(kind, unit.getSourceFile(), unit, clazz);
    }
    private TaskEvent(Kind kind, JavaFileObject file, CompilationUnitTree unit, TypeElement clazz) {
        this.kind = kind;
        this.file = file;
        this.unit = unit;
        this.clazz = clazz;
    }
    public Kind getKind() {
        return kind;
    }
    public JavaFileObject getSourceFile() {
        return file;
    }
    public CompilationUnitTree getCompilationUnit() {
        return unit;
    }
    public TypeElement getTypeElement() {
        return clazz;
    }
    public String toString() {
        return "TaskEvent["
            + kind + ","
            + file + ","
            + clazz + "]";
    }
    private Kind kind;
    private JavaFileObject file;
    private CompilationUnitTree unit;
    private TypeElement clazz;
}

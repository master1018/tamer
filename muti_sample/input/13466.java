public class JavacMessager implements Messager {
    Log log;
    JavacProcessingEnvironment processingEnv;
    int errorCount = 0;
    int warningCount = 0;
    JavacMessager(Context context, JavacProcessingEnvironment processingEnv) {
        log = Log.instance(context);
        this.processingEnv = processingEnv;
    }
    public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
        printMessage(kind, msg, null, null, null);
    }
    public void printMessage(Diagnostic.Kind kind, CharSequence msg,
                      Element e) {
        printMessage(kind, msg, e, null, null);
    }
    public void printMessage(Diagnostic.Kind kind, CharSequence msg,
                      Element e, AnnotationMirror a) {
        printMessage(kind, msg, e, a, null);
    }
    public void printMessage(Diagnostic.Kind kind, CharSequence msg,
                      Element e, AnnotationMirror a, AnnotationValue v) {
        JavaFileObject oldSource = null;
        JavaFileObject newSource = null;
        JCDiagnostic.DiagnosticPosition pos = null;
        JavacElements elemUtils = processingEnv.getElementUtils();
        Pair<JCTree, JCCompilationUnit> treeTop = elemUtils.getTreeAndTopLevel(e, a, v);
        if (treeTop != null) {
            newSource = treeTop.snd.sourcefile;
            if (newSource != null) {
                oldSource = log.useSource(newSource);
                pos = treeTop.fst.pos();
            }
        }
        try {
            switch (kind) {
            case ERROR:
                errorCount++;
                boolean prev = log.multipleErrors;
                log.multipleErrors = true;
                try {
                    log.error(pos, "proc.messager", msg.toString());
                } finally {
                    log.multipleErrors = prev;
                }
                break;
            case WARNING:
                warningCount++;
                log.warning(pos, "proc.messager", msg.toString());
                break;
            case MANDATORY_WARNING:
                warningCount++;
                log.mandatoryWarning(pos, "proc.messager", msg.toString());
                break;
            default:
                log.note(pos, "proc.messager", msg.toString());
                break;
            }
        } finally {
            if (oldSource != null)
                log.useSource(oldSource);
        }
    }
    public void printError(String msg) {
        printMessage(Diagnostic.Kind.ERROR, msg);
    }
    public void printWarning(String msg) {
        printMessage(Diagnostic.Kind.WARNING, msg);
    }
    public void printNotice(String msg) {
        printMessage(Diagnostic.Kind.NOTE, msg);
    }
    public boolean errorRaised() {
        return errorCount > 0;
    }
    public int errorCount() {
        return errorCount;
    }
    public int warningCount() {
        return warningCount;
    }
    public void newRound(Context context) {
        log = Log.instance(context);
        errorCount = 0;
    }
    public String toString() {
        return "javac Messager";
    }
}

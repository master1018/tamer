public class MessagerImpl implements Messager {
    private final Bark bark;
    private static final Context.Key<MessagerImpl> messagerKey =
            new Context.Key<MessagerImpl>();
    public static MessagerImpl instance(Context context) {
        MessagerImpl instance = context.get(messagerKey);
        if (instance == null) {
            instance = new MessagerImpl(context);
        }
        return instance;
    }
    private MessagerImpl(Context context) {
        context.put(messagerKey, this);
        bark = Bark.instance(context);
    }
    public void printError(String msg) {
        bark.aptError("Messager", msg);
    }
    public void printError(SourcePosition pos, String msg) {
        if (pos instanceof SourcePositionImpl) {
            SourcePositionImpl posImpl = (SourcePositionImpl) pos;
            JavaFileObject prev = bark.useSource(posImpl.getSource());
            bark.aptError(posImpl.getJavacPosition(), "Messager", msg);
            bark.useSource(prev);
        } else
            printError(msg);
    }
    public void printWarning(String msg) {
        bark.aptWarning("Messager", msg);
    }
    public void printWarning(SourcePosition pos, String msg) {
        if (pos instanceof SourcePositionImpl) {
            SourcePositionImpl posImpl = (SourcePositionImpl) pos;
            JavaFileObject prev = bark.useSource(posImpl.getSource());
            bark.aptWarning(posImpl.getJavacPosition(), "Messager", msg);
            bark.useSource(prev);
        } else
            printWarning(msg);
    }
    public void printNotice(String msg) {
        bark.aptNote("Messager", msg);
    }
    public void printNotice(SourcePosition pos, String msg) {
        if (pos instanceof SourcePositionImpl) {
            SourcePositionImpl posImpl = (SourcePositionImpl) pos;
            JavaFileObject prev = bark.useSource(posImpl.getSource());
            bark.aptNote(posImpl.getJavacPosition(), "Messager", msg);
            bark.useSource(prev);
        } else
            printNotice(msg);
    }
}

public class Log extends AbstractLog {
    public static final Context.Key<Log> logKey
        = new Context.Key<Log>();
    public static final Context.Key<PrintWriter> outKey =
        new Context.Key<PrintWriter>();
    public final PrintWriter errWriter;
    public final PrintWriter warnWriter;
    public final PrintWriter noticeWriter;
    public final int MaxErrors;
    public final int MaxWarnings;
    public boolean promptOnError;
    public boolean emitWarnings;
    public boolean suppressNotes;
    public boolean dumpOnError;
    public boolean multipleErrors;
    protected DiagnosticListener<? super JavaFileObject> diagListener;
    private DiagnosticFormatter<JCDiagnostic> diagFormatter;
    public Set<String> expectDiagKeys;
    private JavacMessages messages;
    public boolean deferDiagnostics;
    public Queue<JCDiagnostic> deferredDiagnostics = new ListBuffer<JCDiagnostic>();
    @Deprecated
    protected Log(Context context, PrintWriter errWriter, PrintWriter warnWriter, PrintWriter noticeWriter) {
        super(JCDiagnostic.Factory.instance(context));
        context.put(logKey, this);
        this.errWriter = errWriter;
        this.warnWriter = warnWriter;
        this.noticeWriter = noticeWriter;
        Options options = Options.instance(context);
        this.dumpOnError = options.isSet(DOE);
        this.promptOnError = options.isSet(PROMPT);
        this.emitWarnings = options.isUnset(XLINT_CUSTOM, "none");
        this.suppressNotes = options.isSet("suppressNotes");
        this.MaxErrors = getIntOption(options, XMAXERRS, getDefaultMaxErrors());
        this.MaxWarnings = getIntOption(options, XMAXWARNS, getDefaultMaxWarnings());
        boolean rawDiagnostics = options.isSet("rawDiagnostics");
        messages = JavacMessages.instance(context);
        this.diagFormatter = rawDiagnostics ? new RawDiagnosticFormatter(options) :
                                              new BasicDiagnosticFormatter(options, messages);
        @SuppressWarnings("unchecked") 
        DiagnosticListener<? super JavaFileObject> dl =
            context.get(DiagnosticListener.class);
        this.diagListener = dl;
        String ek = options.get("expectKeys");
        if (ek != null)
            expectDiagKeys = new HashSet<String>(Arrays.asList(ek.split(", *")));
    }
        private int getIntOption(Options options, OptionName optionName, int defaultValue) {
            String s = options.get(optionName);
            try {
                if (s != null) {
                    int n = Integer.parseInt(s);
                    return (n <= 0 ? Integer.MAX_VALUE : n);
                }
            } catch (NumberFormatException e) {
            }
            return defaultValue;
        }
        protected int getDefaultMaxErrors() {
            return 100;
        }
        protected int getDefaultMaxWarnings() {
            return 100;
        }
    static final PrintWriter defaultWriter(Context context) {
        PrintWriter result = context.get(outKey);
        if (result == null)
            context.put(outKey, result = new PrintWriter(System.err));
        return result;
    }
    protected Log(Context context) {
        this(context, defaultWriter(context));
    }
    protected Log(Context context, PrintWriter defaultWriter) {
        this(context, defaultWriter, defaultWriter, defaultWriter);
    }
    public static Log instance(Context context) {
        Log instance = context.get(logKey);
        if (instance == null)
            instance = new Log(context);
        return instance;
    }
    public int nerrors = 0;
    public int nwarnings = 0;
    private Set<Pair<JavaFileObject, Integer>> recorded = new HashSet<Pair<JavaFileObject,Integer>>();
    public boolean hasDiagnosticListener() {
        return diagListener != null;
    }
    public void setEndPosTable(JavaFileObject name, Map<JCTree, Integer> table) {
        name.getClass(); 
        getSource(name).setEndPosTable(table);
    }
    public JavaFileObject currentSourceFile() {
        return source == null ? null : source.getFile();
    }
    public DiagnosticFormatter<JCDiagnostic> getDiagnosticFormatter() {
        return diagFormatter;
    }
    public void setDiagnosticFormatter(DiagnosticFormatter<JCDiagnostic> diagFormatter) {
        this.diagFormatter = diagFormatter;
    }
    public void flush() {
        errWriter.flush();
        warnWriter.flush();
        noticeWriter.flush();
    }
    protected boolean shouldReport(JavaFileObject file, int pos) {
        if (multipleErrors || file == null)
            return true;
        Pair<JavaFileObject,Integer> coords = new Pair<JavaFileObject,Integer>(file, pos);
        boolean shouldReport = !recorded.contains(coords);
        if (shouldReport)
            recorded.add(coords);
        return shouldReport;
    }
    public void prompt() {
        if (promptOnError) {
            System.err.println(localize("resume.abort"));
            char ch;
            try {
                while (true) {
                    switch (System.in.read()) {
                    case 'a': case 'A':
                        System.exit(-1);
                        return;
                    case 'r': case 'R':
                        return;
                    case 'x': case 'X':
                        throw new AssertionError("user abort");
                    default:
                    }
                }
            } catch (IOException e) {}
        }
    }
    private void printErrLine(int pos, PrintWriter writer) {
        String line = (source == null ? null : source.getLine(pos));
        if (line == null)
            return;
        int col = source.getColumnNumber(pos, false);
        printLines(writer, line);
        for (int i = 0; i < col - 1; i++) {
            writer.print((line.charAt(i) == '\t') ? "\t" : " ");
        }
        writer.println("^");
        writer.flush();
    }
    public static void printLines(PrintWriter writer, String msg) {
        int nl;
        while ((nl = msg.indexOf('\n')) != -1) {
            writer.println(msg.substring(0, nl));
            msg = msg.substring(nl+1);
        }
        if (msg.length() != 0) writer.println(msg);
    }
    public void printErrLines(String key, Object... args) {
        printLines(errWriter, localize(key, args));
    }
    public void printNoteLines(String key, Object... args) {
        printLines(noticeWriter, localize(key, args));
    }
    public void printVerbose(String key, Object... args) {
        printLines(noticeWriter, localize("verbose." + key, args));
    }
    protected void directError(String key, Object... args) {
        printErrLines(key, args);
        errWriter.flush();
    }
    public void strictWarning(DiagnosticPosition pos, String key, Object ... args) {
        writeDiagnostic(diags.warning(source, pos, key, args));
        nwarnings++;
    }
    public void reportDeferredDiagnostics() {
        reportDeferredDiagnostics(EnumSet.allOf(JCDiagnostic.Kind.class));
    }
    public void reportDeferredDiagnostics(Set<JCDiagnostic.Kind> kinds) {
        deferDiagnostics = false;
        JCDiagnostic d;
        while ((d = deferredDiagnostics.poll()) != null) {
            if (kinds.contains(d.getKind()))
                report(d);
        }
    }
    public void report(JCDiagnostic diagnostic) {
        if (deferDiagnostics) {
            deferredDiagnostics.add(diagnostic);
            return;
        }
        if (expectDiagKeys != null)
            expectDiagKeys.remove(diagnostic.getCode());
        switch (diagnostic.getType()) {
        case FRAGMENT:
            throw new IllegalArgumentException();
        case NOTE:
            if ((emitWarnings || diagnostic.isMandatory()) && !suppressNotes) {
                writeDiagnostic(diagnostic);
            }
            break;
        case WARNING:
            if (emitWarnings || diagnostic.isMandatory()) {
                if (nwarnings < MaxWarnings) {
                    writeDiagnostic(diagnostic);
                    nwarnings++;
                }
            }
            break;
        case ERROR:
            if (nerrors < MaxErrors
                && shouldReport(diagnostic.getSource(), diagnostic.getIntPosition())) {
                writeDiagnostic(diagnostic);
                nerrors++;
            }
            break;
        }
    }
    protected void writeDiagnostic(JCDiagnostic diag) {
        if (diagListener != null) {
            diagListener.report(diag);
            return;
        }
        PrintWriter writer = getWriterForDiagnosticType(diag.getType());
        printLines(writer, diagFormatter.format(diag, messages.getCurrentLocale()));
        if (promptOnError) {
            switch (diag.getType()) {
            case ERROR:
            case WARNING:
                prompt();
            }
        }
        if (dumpOnError)
            new RuntimeException().printStackTrace(writer);
        writer.flush();
    }
    @Deprecated
    protected PrintWriter getWriterForDiagnosticType(DiagnosticType dt) {
        switch (dt) {
        case FRAGMENT:
            throw new IllegalArgumentException();
        case NOTE:
            return noticeWriter;
        case WARNING:
            return warnWriter;
        case ERROR:
            return errWriter;
        default:
            throw new Error();
        }
    }
    public static String getLocalizedString(String key, Object ... args) {
        return JavacMessages.getDefaultLocalizedString("compiler.misc." + key, args);
    }
    public String localize(String key, Object... args) {
        return messages.getLocalizedString("compiler.misc." + key, args);
    }
    private void printRawError(int pos, String msg) {
        if (source == null || pos == Position.NOPOS) {
            printLines(errWriter, "error: " + msg);
        } else {
            int line = source.getLineNumber(pos);
            JavaFileObject file = source.getFile();
            if (file != null)
                printLines(errWriter,
                           file.getName() + ":" +
                           line + ": " + msg);
            printErrLine(pos, errWriter);
        }
        errWriter.flush();
    }
    public void rawError(int pos, String msg) {
        if (nerrors < MaxErrors && shouldReport(currentSourceFile(), pos)) {
            printRawError(pos, msg);
            prompt();
            nerrors++;
        }
        errWriter.flush();
    }
    public void rawWarning(int pos, String msg) {
        if (nwarnings < MaxWarnings && emitWarnings) {
            printRawError(pos, "warning: " + msg);
        }
        prompt();
        nwarnings++;
        errWriter.flush();
    }
    public static String format(String fmt, Object... args) {
        return String.format((java.util.Locale)null, fmt, args);
    }
}

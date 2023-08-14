public abstract class AbstractDiagnosticFormatter implements DiagnosticFormatter<JCDiagnostic> {
    protected JavacMessages messages;
    private SimpleConfiguration config;
    protected int depth = 0;
    private List<Type> allCaptured = List.nil();
    protected AbstractDiagnosticFormatter(JavacMessages messages, SimpleConfiguration config) {
        this.messages = messages;
        this.config = config;
    }
    public String formatKind(JCDiagnostic d, Locale l) {
        switch (d.getType()) {
            case FRAGMENT: return "";
            case NOTE:     return localize(l, "compiler.note.note");
            case WARNING:  return localize(l, "compiler.warn.warning");
            case ERROR:    return localize(l, "compiler.err.error");
            default:
                throw new AssertionError("Unknown diagnostic type: " + d.getType());
        }
    }
    @Override
    public String format(JCDiagnostic d, Locale locale) {
        allCaptured = List.nil();
        return formatDiagnostic(d, locale);
    }
    protected abstract String formatDiagnostic(JCDiagnostic d, Locale locale);
    public String formatPosition(JCDiagnostic d, PositionKind pk,Locale l) {
        Assert.check(d.getPosition() != Position.NOPOS);
        return String.valueOf(getPosition(d, pk));
    }
    private long getPosition(JCDiagnostic d, PositionKind pk) {
        switch (pk) {
            case START: return d.getIntStartPosition();
            case END: return d.getIntEndPosition();
            case LINE: return d.getLineNumber();
            case COLUMN: return d.getColumnNumber();
            case OFFSET: return d.getIntPosition();
            default:
                throw new AssertionError("Unknown diagnostic position: " + pk);
        }
    }
    public String formatSource(JCDiagnostic d, boolean fullname, Locale l) {
        JavaFileObject fo = d.getSource();
        if (fo == null)
            throw new IllegalArgumentException(); 
        if (fullname)
            return fo.getName();
        else if (fo instanceof BaseFileObject)
            return ((BaseFileObject) fo).getShortName();
        else
            return BaseFileObject.getSimpleName(fo);
    }
    protected Collection<String> formatArguments(JCDiagnostic d, Locale l) {
        ListBuffer<String> buf = new ListBuffer<String>();
        for (Object o : d.getArgs()) {
           buf.append(formatArgument(d, o, l));
        }
        return buf.toList();
    }
    protected String formatArgument(JCDiagnostic d, Object arg, Locale l) {
        if (arg instanceof JCDiagnostic) {
            String s = null;
            depth++;
            try {
                s = formatMessage((JCDiagnostic)arg, l);
            }
            finally {
                depth--;
            }
            return s;
        }
        else if (arg instanceof Iterable<?>) {
            return formatIterable(d, (Iterable<?>)arg, l);
        }
        else if (arg instanceof Type) {
            return printer.visit((Type)arg, l);
        }
        else if (arg instanceof Symbol) {
            return printer.visit((Symbol)arg, l);
        }
        else if (arg instanceof JavaFileObject) {
            return ((JavaFileObject)arg).getName();
        }
        else if (arg instanceof Formattable) {
            return ((Formattable)arg).toString(l, messages);
        }
        else {
            return String.valueOf(arg);
        }
    }
    protected String formatIterable(JCDiagnostic d, Iterable<?> it, Locale l) {
        StringBuilder sbuf = new StringBuilder();
        String sep = "";
        for (Object o : it) {
            sbuf.append(sep);
            sbuf.append(formatArgument(d, o, l));
            sep = ",";
        }
        return sbuf.toString();
    }
    protected List<String> formatSubdiagnostics(JCDiagnostic d, Locale l) {
        List<String> subdiagnostics = List.nil();
        int maxDepth = config.getMultilineLimit(MultilineLimit.DEPTH);
        if (maxDepth == -1 || depth < maxDepth) {
            depth++;
            try {
                int maxCount = config.getMultilineLimit(MultilineLimit.LENGTH);
                int count = 0;
                for (JCDiagnostic d2 : d.getSubdiagnostics()) {
                    if (maxCount == -1 || count < maxCount) {
                        subdiagnostics = subdiagnostics.append(formatSubdiagnostic(d, d2, l));
                        count++;
                    }
                    else
                        break;
                }
            }
            finally {
                depth--;
            }
        }
        return subdiagnostics;
    }
    protected String formatSubdiagnostic(JCDiagnostic parent, JCDiagnostic sub, Locale l) {
        return formatMessage(sub, l);
    }
    protected String formatSourceLine(JCDiagnostic d, int nSpaces) {
        StringBuilder buf = new StringBuilder();
        DiagnosticSource source = d.getDiagnosticSource();
        int pos = d.getIntPosition();
        if (d.getIntPosition() == Position.NOPOS)
            throw new AssertionError();
        String line = (source == null ? null : source.getLine(pos));
        if (line == null)
            return "";
        buf.append(indent(line, nSpaces));
        int col = source.getColumnNumber(pos, false);
        if (config.isCaretEnabled()) {
            buf.append("\n");
            for (int i = 0; i < col - 1; i++)  {
                buf.append((line.charAt(i) == '\t') ? "\t" : " ");
            }
            buf.append(indent("^", nSpaces));
        }
        return buf.toString();
    }
    protected String formatLintCategory(JCDiagnostic d, Locale l) {
        LintCategory lc = d.getLintCategory();
        if (lc == null)
            return "";
        return localize(l, "compiler.warn.lintOption", lc.option);
    }
    protected String localize(Locale l, String key, Object... args) {
        return messages.getLocalizedString(l, key, args);
    }
    public boolean displaySource(JCDiagnostic d) {
        return config.getVisible().contains(DiagnosticPart.SOURCE) &&
                d.getType() != FRAGMENT &&
                d.getIntPosition() != Position.NOPOS;
    }
    public boolean isRaw() {
        return false;
    }
    protected String indentString(int nSpaces) {
        String spaces = "                        ";
        if (nSpaces <= spaces.length())
            return spaces.substring(0, nSpaces);
        else {
            StringBuilder buf = new StringBuilder();
            for (int i = 0 ; i < nSpaces ; i++)
                buf.append(" ");
            return buf.toString();
        }
    }
    protected String indent(String s, int nSpaces) {
        String indent = indentString(nSpaces);
        StringBuilder buf = new StringBuilder();
        String nl = "";
        for (String line : s.split("\n")) {
            buf.append(nl);
            buf.append(indent + line);
            nl = "\n";
        }
        return buf.toString();
    }
    public SimpleConfiguration getConfiguration() {
        return config;
    }
    static public class SimpleConfiguration implements Configuration {
        protected Map<MultilineLimit, Integer> multilineLimits;
        protected EnumSet<DiagnosticPart> visibleParts;
        protected boolean caretEnabled;
        public SimpleConfiguration(Set<DiagnosticPart> parts) {
            multilineLimits = new HashMap<MultilineLimit, Integer>();
            setVisible(parts);
            setMultilineLimit(MultilineLimit.DEPTH, -1);
            setMultilineLimit(MultilineLimit.LENGTH, -1);
            setCaretEnabled(true);
        }
        @SuppressWarnings("fallthrough")
        public SimpleConfiguration(Options options, Set<DiagnosticPart> parts) {
            this(parts);
            String showSource = null;
            if ((showSource = options.get("showSource")) != null) {
                if (showSource.equals("true"))
                    setVisiblePart(DiagnosticPart.SOURCE, true);
                else if (showSource.equals("false"))
                    setVisiblePart(DiagnosticPart.SOURCE, false);
            }
            String diagOpts = options.get("diags");
            if (diagOpts != null) {
                Collection<String> args = Arrays.asList(diagOpts.split(","));
                if (args.contains("short")) {
                    setVisiblePart(DiagnosticPart.DETAILS, false);
                    setVisiblePart(DiagnosticPart.SUBDIAGNOSTICS, false);
                }
                if (args.contains("source"))
                    setVisiblePart(DiagnosticPart.SOURCE, true);
                if (args.contains("-source"))
                    setVisiblePart(DiagnosticPart.SOURCE, false);
            }
            String multiPolicy = null;
            if ((multiPolicy = options.get("multilinePolicy")) != null) {
                if (multiPolicy.equals("disabled"))
                    setVisiblePart(DiagnosticPart.SUBDIAGNOSTICS, false);
                else if (multiPolicy.startsWith("limit:")) {
                    String limitString = multiPolicy.substring("limit:".length());
                    String[] limits = limitString.split(":");
                    try {
                        switch (limits.length) {
                            case 2: {
                                if (!limits[1].equals("*"))
                                    setMultilineLimit(MultilineLimit.DEPTH, Integer.parseInt(limits[1]));
                            }
                            case 1: {
                                if (!limits[0].equals("*"))
                                    setMultilineLimit(MultilineLimit.LENGTH, Integer.parseInt(limits[0]));
                            }
                        }
                    }
                    catch(NumberFormatException ex) {
                        setMultilineLimit(MultilineLimit.DEPTH, -1);
                        setMultilineLimit(MultilineLimit.LENGTH, -1);
                    }
                }
            }
            String showCaret = null;
            if (((showCaret = options.get("showCaret")) != null) &&
                showCaret.equals("false"))
                    setCaretEnabled(false);
            else
                setCaretEnabled(true);
        }
        public int getMultilineLimit(MultilineLimit limit) {
            return multilineLimits.get(limit);
        }
        public EnumSet<DiagnosticPart> getVisible() {
            return EnumSet.copyOf(visibleParts);
        }
        public void setMultilineLimit(MultilineLimit limit, int value) {
            multilineLimits.put(limit, value < -1 ? -1 : value);
        }
        public void setVisible(Set<DiagnosticPart> diagParts) {
            visibleParts = EnumSet.copyOf(diagParts);
        }
        public void setVisiblePart(DiagnosticPart diagParts, boolean enabled) {
            if (enabled)
                visibleParts.add(diagParts);
            else
                visibleParts.remove(diagParts);
        }
        public void setCaretEnabled(boolean caretEnabled) {
            this.caretEnabled = caretEnabled;
        }
        public boolean isCaretEnabled() {
            return caretEnabled;
        }
    }
    public Printer getPrinter() {
        return printer;
    }
    public void setPrinter(Printer printer) {
        this.printer = printer;
    }
    protected Printer printer = new Printer() {
        @Override
        protected String localize(Locale locale, String key, Object... args) {
            return AbstractDiagnosticFormatter.this.localize(locale, key, args);
        }
        @Override
        protected String capturedVarId(CapturedType t, Locale locale) {
            return "" + (allCaptured.indexOf(t) + 1);
        }
        @Override
        public String visitCapturedType(CapturedType t, Locale locale) {
            if (!allCaptured.contains(t)) {
                allCaptured = allCaptured.append(t);
            }
            return super.visitCapturedType(t, locale);
        }
    };
}

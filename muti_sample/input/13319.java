public class BasicDiagnosticFormatter extends AbstractDiagnosticFormatter {
    public BasicDiagnosticFormatter(Options options, JavacMessages msgs) {
        super(msgs, new BasicConfiguration(options));
    }
    public BasicDiagnosticFormatter(JavacMessages msgs) {
        super(msgs, new BasicConfiguration());
    }
    public String formatDiagnostic(JCDiagnostic d, Locale l) {
        if (l == null)
            l = messages.getCurrentLocale();
        String format = selectFormat(d);
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < format.length(); i++) {
            char c = format.charAt(i);
            boolean meta = false;
            if (c == '%' && i < format.length() - 1) {
                meta = true;
                c = format.charAt(++i);
            }
            buf.append(meta ? formatMeta(c, d, l) : String.valueOf(c));
        }
        if (depth == 0)
            return addSourceLineIfNeeded(d, buf.toString());
        else
            return buf.toString();
    }
    public String formatMessage(JCDiagnostic d, Locale l) {
        int currentIndentation = 0;
        StringBuilder buf = new StringBuilder();
        Collection<String> args = formatArguments(d, l);
        String msg = localize(l, d.getCode(), args.toArray());
        String[] lines = msg.split("\n");
        if (getConfiguration().getVisible().contains(DiagnosticPart.SUMMARY)) {
            currentIndentation += getConfiguration().getIndentation(DiagnosticPart.SUMMARY);
            buf.append(indent(lines[0], currentIndentation)); 
        }
        if (lines.length > 1 && getConfiguration().getVisible().contains(DiagnosticPart.DETAILS)) {
            currentIndentation += getConfiguration().getIndentation(DiagnosticPart.DETAILS);
            for (int i = 1;i < lines.length; i++) {
                buf.append("\n" + indent(lines[i], currentIndentation));
            }
        }
        if (d.isMultiline() && getConfiguration().getVisible().contains(DiagnosticPart.SUBDIAGNOSTICS)) {
            currentIndentation += getConfiguration().getIndentation(DiagnosticPart.SUBDIAGNOSTICS);
                for (String sub : formatSubdiagnostics(d, l)) {
                    buf.append("\n" + indent(sub, currentIndentation));
            }
        }
        return buf.toString();
    }
    protected String addSourceLineIfNeeded(JCDiagnostic d, String msg) {
        if (!displaySource(d))
            return msg;
        else {
            BasicConfiguration conf = getConfiguration();
            int indentSource = conf.getIndentation(DiagnosticPart.SOURCE);
            String sourceLine = "\n" + formatSourceLine(d, indentSource);
            boolean singleLine = msg.indexOf("\n") == -1;
            if (singleLine || getConfiguration().getSourcePosition() == SourcePosition.BOTTOM)
                return msg + sourceLine;
            else
                return msg.replaceFirst("\n", Matcher.quoteReplacement(sourceLine) + "\n");
        }
    }
    protected String formatMeta(char c, JCDiagnostic d, Locale l) {
        switch (c) {
            case 'b':
                return formatSource(d, false, l);
            case 'e':
                return formatPosition(d, END, l);
            case 'f':
                return formatSource(d, true, l);
            case 'l':
                return formatPosition(d, LINE, l);
            case 'c':
                return formatPosition(d, COLUMN, l);
            case 'o':
                return formatPosition(d, OFFSET, l);
            case 'p':
                return formatKind(d, l);
            case 's':
                return formatPosition(d, START, l);
            case 't': {
                boolean usePrefix;
                switch (d.getType()) {
                case FRAGMENT:
                    usePrefix = false;
                    break;
                case ERROR:
                    usePrefix = (d.getIntPosition() == Position.NOPOS);
                    break;
                default:
                    usePrefix = true;
                }
                if (usePrefix)
                    return formatKind(d, l);
                else
                    return "";
            }
            case 'm':
                return formatMessage(d, l);
            case 'L':
                return formatLintCategory(d, l);
            case '_':
                return " ";
            case '%':
                return "%";
            default:
                return String.valueOf(c);
        }
    }
    private String selectFormat(JCDiagnostic d) {
        DiagnosticSource source = d.getDiagnosticSource();
        String format = getConfiguration().getFormat(BasicFormatKind.DEFAULT_NO_POS_FORMAT);
        if (source != null && source != DiagnosticSource.NO_SOURCE) {
            if (d.getIntPosition() != Position.NOPOS) {
                format = getConfiguration().getFormat(BasicFormatKind.DEFAULT_POS_FORMAT);
            } else if (source.getFile() != null &&
                       source.getFile().getKind() == JavaFileObject.Kind.CLASS) {
                format = getConfiguration().getFormat(BasicFormatKind.DEFAULT_CLASS_FORMAT);
            }
        }
        return format;
    }
    @Override
    public BasicConfiguration getConfiguration() {
        return (BasicConfiguration)super.getConfiguration();
    }
    static public class BasicConfiguration extends SimpleConfiguration {
        protected Map<DiagnosticPart, Integer> indentationLevels;
        protected Map<BasicFormatKind, String> availableFormats;
        protected SourcePosition sourcePosition;
        @SuppressWarnings("fallthrough")
        public BasicConfiguration(Options options) {
            super(options, EnumSet.of(DiagnosticPart.SUMMARY,
                            DiagnosticPart.DETAILS,
                            DiagnosticPart.SUBDIAGNOSTICS,
                            DiagnosticPart.SOURCE));
            initFormat();
            initIndentation();
            if (options.isSet("oldDiags"))
                initOldFormat();
            String fmt = options.get("diagsFormat");
            if (fmt != null) {
                if (fmt.equals("OLD"))
                    initOldFormat();
                else
                    initFormats(fmt);
            }
            String srcPos = null;
            if ((((srcPos = options.get("sourcePosition")) != null)) &&
                    srcPos.equals("bottom"))
                    setSourcePosition(SourcePosition.BOTTOM);
            else
                setSourcePosition(SourcePosition.AFTER_SUMMARY);
            String indent = options.get("diagsIndentation");
            if (indent != null) {
                String[] levels = indent.split("\\|");
                try {
                    switch (levels.length) {
                        case 5:
                            setIndentation(DiagnosticPart.JLS,
                                    Integer.parseInt(levels[4]));
                        case 4:
                            setIndentation(DiagnosticPart.SUBDIAGNOSTICS,
                                    Integer.parseInt(levels[3]));
                        case 3:
                            setIndentation(DiagnosticPart.SOURCE,
                                    Integer.parseInt(levels[2]));
                        case 2:
                            setIndentation(DiagnosticPart.DETAILS,
                                    Integer.parseInt(levels[1]));
                        default:
                            setIndentation(DiagnosticPart.SUMMARY,
                                    Integer.parseInt(levels[0]));
                    }
                }
                catch (NumberFormatException ex) {
                    initIndentation();
                }
            }
        }
        public BasicConfiguration() {
            super(EnumSet.of(DiagnosticPart.SUMMARY,
                  DiagnosticPart.DETAILS,
                  DiagnosticPart.SUBDIAGNOSTICS,
                  DiagnosticPart.SOURCE));
            initFormat();
            initIndentation();
        }
        private void initFormat() {
            initFormats("%f:%l:%_%p%L%m", "%p%L%m", "%f:%_%p%L%m");
        }
        private void initOldFormat() {
            initFormats("%f:%l:%_%t%L%m", "%p%L%m", "%f:%_%t%L%m");
        }
        private void initFormats(String pos, String nopos, String clazz) {
            availableFormats = new EnumMap<BasicFormatKind, String>(BasicFormatKind.class);
            setFormat(BasicFormatKind.DEFAULT_POS_FORMAT,    pos);
            setFormat(BasicFormatKind.DEFAULT_NO_POS_FORMAT, nopos);
            setFormat(BasicFormatKind.DEFAULT_CLASS_FORMAT,  clazz);
        }
        @SuppressWarnings("fallthrough")
        private void initFormats(String fmt) {
            String[] formats = fmt.split("\\|");
            switch (formats.length) {
                case 3:
                    setFormat(BasicFormatKind.DEFAULT_CLASS_FORMAT, formats[2]);
                case 2:
                    setFormat(BasicFormatKind.DEFAULT_NO_POS_FORMAT, formats[1]);
                default:
                    setFormat(BasicFormatKind.DEFAULT_POS_FORMAT, formats[0]);
            }
        }
        private void initIndentation() {
            indentationLevels = new HashMap<DiagnosticPart, Integer>();
            setIndentation(DiagnosticPart.SUMMARY, 0);
            setIndentation(DiagnosticPart.DETAILS, DetailsInc);
            setIndentation(DiagnosticPart.SUBDIAGNOSTICS, DiagInc);
            setIndentation(DiagnosticPart.SOURCE, 0);
        }
        public int getIndentation(DiagnosticPart diagPart) {
            return indentationLevels.get(diagPart);
        }
        public void setIndentation(DiagnosticPart diagPart, int nSpaces) {
            indentationLevels.put(diagPart, nSpaces);
        }
        public void setSourcePosition(SourcePosition sourcePos) {
            sourcePosition = sourcePos;
        }
        public SourcePosition getSourcePosition() {
            return sourcePosition;
        }
        public enum SourcePosition {
            BOTTOM,
            AFTER_SUMMARY;
        }
        public void setFormat(BasicFormatKind kind, String s) {
            availableFormats.put(kind, s);
        }
        public String getFormat(BasicFormatKind kind) {
            return availableFormats.get(kind);
        }
        public enum BasicFormatKind {
            DEFAULT_POS_FORMAT,
            DEFAULT_NO_POS_FORMAT,
            DEFAULT_CLASS_FORMAT;
        }
    }
}

public class SourceModel extends AbstractListModel {
    private File path;
    boolean isActuallySource = true;
    private List<ReferenceType> classes = new ArrayList<ReferenceType>();
    private Environment env;
    private List<Line> sourceLines = null;
    public static class Line {
        public String text;
        public boolean hasBreakpoint = false;
        public ReferenceType refType = null;
        Line(String text) {
            this.text = text;
        }
        public boolean isExecutable() {
            return refType != null;
        }
        public boolean hasBreakpoint() {
            return hasBreakpoint;
        }
    };
    public static final Line prototypeCellValue = new Line(
                                        "abcdefghijklmnopqrstuvwxyz" +
                                        "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                                        "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                                        "1234567890~!@#$%^&*()_+{}|" +
                                        ":<>?`-=[];',.XXXXXXXXXXXX/\\\"");
    SourceModel(Environment env, File path) {
        this.env = env;
        this.path = path;
    }
    public SourceModel(String message) {
        this.path = null;
        setMessage(message);
    }
    private void setMessage(String message) {
        isActuallySource = false;
        sourceLines = new ArrayList<Line>();
        sourceLines.add(new Line(message));
    }
    @Override
    public Object getElementAt(int index) {
        if (sourceLines == null) {
            initialize();
        }
        return sourceLines.get(index);
    }
    @Override
    public int getSize() {
        if (sourceLines == null) {
            initialize();
        }
        return sourceLines.size();
    }
    public File fileName() {
        return path;
    }
    public BufferedReader sourceReader() throws IOException {
        return new BufferedReader(new FileReader(path));
    }
    public Line line(int lineNo) {
        if (sourceLines == null) {
            initialize();
        }
        int index = lineNo - 1; 
        if (index >= sourceLines.size() || index < 0) {
            return null;
        } else {
            return sourceLines.get(index);
        }
    }
    public String sourceLine(int lineNo) {
        Line line = line(lineNo);
        if (line == null) {
            return null;
        } else {
            return line.text;
        }
    }
    void addClass(ReferenceType refType) {
        if (classes.indexOf(refType) == -1) {
            classes.add(refType);
            if (sourceLines != null) {
                markClassLines(refType);
            }
        }
    }
    public List<ReferenceType> referenceTypes() {
        return Collections.unmodifiableList(classes);
    }
    private void initialize() {
        try {
            rawInit();
        } catch (IOException exc) {
            setMessage("[Error reading source code]");
        }
    }
    public void showBreakpoint(int ln, boolean hasBreakpoint) {
        line(ln).hasBreakpoint = hasBreakpoint;
        fireContentsChanged(this, ln, ln);
    }
    public void showExecutable(int ln, ReferenceType refType) {
        line(ln).refType = refType;
        fireContentsChanged(this, ln, ln);
    }
    private void markClassLines(ReferenceType refType) {
        for (Method meth : refType.methods()) {
            try {
                for (Location loc : meth.allLineLocations()) {
                    showExecutable(loc.lineNumber(), refType);
                }
            } catch (AbsentInformationException exc) {
            }
        }
        for (BreakpointRequest bp :
                 env.getExecutionManager().eventRequestManager().breakpointRequests()) {
            if (bp.location() != null) {
                Location loc = bp.location();
                if (loc.declaringType().equals(refType)) {
                    showBreakpoint(loc.lineNumber(),true);
                }
            }
        }
    }
    private void rawInit() throws IOException {
        sourceLines = new ArrayList<Line>();
        BufferedReader reader = sourceReader();
        try {
            String line = reader.readLine();
            while (line != null) {
                sourceLines.add(new Line(expandTabs(line)));
                line = reader.readLine();
            }
        } finally {
            reader.close();
        }
        for (ReferenceType refType : classes) {
            markClassLines(refType);
        }
    }
    private String expandTabs(String s) {
        int col = 0;
        int len = s.length();
        StringBuffer sb = new StringBuffer(132);
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            sb.append(c);
            if (c == '\t') {
                int pad = (8 - (col % 8));
                for (int j = 0; j < pad; j++) {
                    sb.append(' ');
                }
                col += pad;
            } else {
                col++;
            }
        }
        return sb.toString();
    }
}

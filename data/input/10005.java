class SDE {
    private static final int INIT_SIZE_FILE = 3;
    private static final int INIT_SIZE_LINE = 100;
    private static final int INIT_SIZE_STRATUM = 3;
    static final String BASE_STRATUM_NAME = "Java";
    static final String NullString = null;
    private class FileTableRecord {
        int fileId;
        String sourceName;
        String sourcePath; 
        boolean isConverted = false;
        String getSourcePath(ReferenceTypeImpl refType) {
            if (!isConverted) {
                if (sourcePath == null) {
                    sourcePath = refType.baseSourceDir() + sourceName;
                } else {
                    StringBuffer buf = new StringBuffer();
                    for (int i = 0; i < sourcePath.length(); ++i) {
                        char ch = sourcePath.charAt(i);
                        if (ch == '/') {
                            buf.append(File.separatorChar);
                        } else {
                            buf.append(ch);
                        }
                    }
                    sourcePath = buf.toString();
                }
                isConverted = true;
            }
            return sourcePath;
        }
    }
    private class LineTableRecord {
        int jplsStart;
        int jplsEnd;
        int jplsLineInc;
        int njplsStart;
        int njplsEnd;
        int fileId;
    }
    private class StratumTableRecord {
        String id;
        int fileIndex;
        int lineIndex;
    }
    class Stratum {
        private final int sti; 
        private Stratum(int sti) {
            this.sti = sti;
        }
        String id() {
            return stratumTable[sti].id;
        }
        boolean isJava() {
            return sti == baseStratumIndex;
        }
        List sourceNames(ReferenceTypeImpl refType) {
            int i;
            int fileIndexStart = stratumTable[sti].fileIndex;
            int fileIndexEnd = stratumTable[sti+1].fileIndex;
            List result = new ArrayList(fileIndexEnd - fileIndexStart);
            for (i = fileIndexStart; i < fileIndexEnd; ++i) {
                result.add(fileTable[i].sourceName);
            }
            return result;
        }
        List sourcePaths(ReferenceTypeImpl refType) {
            int i;
            int fileIndexStart = stratumTable[sti].fileIndex;
            int fileIndexEnd = stratumTable[sti+1].fileIndex;
            List result = new ArrayList(fileIndexEnd - fileIndexStart);
            for (i = fileIndexStart; i < fileIndexEnd; ++i) {
                result.add(fileTable[i].getSourcePath(refType));
            }
            return result;
        }
        LineStratum lineStratum(ReferenceTypeImpl refType,
                                int jplsLine) {
            int lti = stiLineTableIndex(sti, jplsLine);
            if (lti < 0) {
                return null;
            } else {
                return new LineStratum(sti, lti, refType,
                                       jplsLine);
            }
        }
    }
    class LineStratum {
        private final int sti; 
        private final int lti; 
        private final ReferenceTypeImpl refType;
        private final int jplsLine;
        private String sourceName = null;
        private String sourcePath = null;
        private LineStratum(int sti, int lti,
                            ReferenceTypeImpl refType,
                            int jplsLine) {
            this.sti = sti;
            this.lti = lti;
            this.refType = refType;
            this.jplsLine = jplsLine;
        }
        public boolean equals(Object obj) {
            if ((obj != null) && (obj instanceof LineStratum)) {
                LineStratum other = (LineStratum)obj;
                return (lti == other.lti) &&
                       (sti == other.sti) &&
                       (lineNumber() == other.lineNumber()) &&
                       (refType.equals(other.refType));
            } else {
                return false;
            }
        }
        int lineNumber() {
            return stiLineNumber(sti, lti, jplsLine);
        }
        void getSourceInfo() {
            if (sourceName != null) {
                return;
            }
            int fti = stiFileTableIndex(sti, lti);
            if (fti == -1) {
                throw new InternalError(
              "Bad SourceDebugExtension, no matching source id " +
              lineTable[lti].fileId + " jplsLine: " + jplsLine);
            }
            FileTableRecord ftr = fileTable[fti];
            sourceName = ftr.sourceName;
            sourcePath = ftr.getSourcePath(refType);
        }
        String sourceName() {
            getSourceInfo();
            return sourceName;
        }
        String sourcePath() {
            getSourceInfo();
            return sourcePath;
        }
    }
    private FileTableRecord[] fileTable = null;
    private LineTableRecord[] lineTable = null;
    private StratumTableRecord[] stratumTable = null;
    private int fileIndex = 0;
    private int lineIndex = 0;
    private int stratumIndex = 0;
    private int currentFileId = 0;
    private int defaultStratumIndex = -1;
    private int baseStratumIndex = -2; 
    private int sdePos = 0;
    final String sourceDebugExtension;
    String jplsFilename = null;
    String defaultStratumId = null;
    boolean isValid = false;
    SDE(String sourceDebugExtension) {
        this.sourceDebugExtension = sourceDebugExtension;
        decode();
    }
    SDE() {
        this.sourceDebugExtension = null;
        createProxyForAbsentSDE();
    }
    char sdePeek() {
        if (sdePos >= sourceDebugExtension.length()) {
            syntax();
        }
        return sourceDebugExtension.charAt(sdePos);
    }
    char sdeRead() {
        if (sdePos >= sourceDebugExtension.length()) {
            syntax();
        }
        return sourceDebugExtension.charAt(sdePos++);
    }
    void sdeAdvance() {
        sdePos++;
    }
    void syntax() {
        throw new InternalError("bad SourceDebugExtension syntax - position " +
                                sdePos);
    }
    void syntax(String msg) {
        throw new InternalError("bad SourceDebugExtension syntax: " + msg);
    }
    void assureLineTableSize() {
        int len = lineTable == null? 0 : lineTable.length;
        if (lineIndex >= len) {
            int i;
            int newLen = len == 0? INIT_SIZE_LINE : len * 2;
            LineTableRecord[] newTable = new LineTableRecord[newLen];
            for (i = 0; i < len; ++i) {
                newTable[i] = lineTable[i];
            }
            for (; i < newLen; ++i) {
                newTable[i] = new LineTableRecord();
            }
            lineTable = newTable;
        }
    }
    void assureFileTableSize() {
        int len = fileTable == null? 0 : fileTable.length;
        if (fileIndex >= len) {
            int i;
            int newLen = len == 0? INIT_SIZE_FILE : len * 2;
            FileTableRecord[] newTable = new FileTableRecord[newLen];
            for (i = 0; i < len; ++i) {
                newTable[i] = fileTable[i];
            }
            for (; i < newLen; ++i) {
                newTable[i] = new FileTableRecord();
            }
            fileTable = newTable;
        }
    }
    void assureStratumTableSize() {
        int len = stratumTable == null? 0 : stratumTable.length;
        if (stratumIndex >= len) {
            int i;
            int newLen = len == 0? INIT_SIZE_STRATUM : len * 2;
            StratumTableRecord[] newTable = new StratumTableRecord[newLen];
            for (i = 0; i < len; ++i) {
                newTable[i] = stratumTable[i];
            }
            for (; i < newLen; ++i) {
                newTable[i] = new StratumTableRecord();
            }
            stratumTable = newTable;
        }
    }
    String readLine() {
        StringBuffer sb = new StringBuffer();
        char ch;
        ignoreWhite();
        while (((ch = sdeRead()) != '\n') && (ch != '\r')) {
            sb.append((char)ch);
        }
        if ((ch == '\r') && (sdePeek() == '\n')) {
            sdeRead();
        }
        ignoreWhite(); 
        return sb.toString();
    }
    private int defaultStratumTableIndex() {
        if ((defaultStratumIndex == -1) && (defaultStratumId != null)) {
            defaultStratumIndex =
                stratumTableIndex(defaultStratumId);
        }
        return defaultStratumIndex;
    }
    int stratumTableIndex(String stratumId) {
        int i;
        if (stratumId == null) {
            return defaultStratumTableIndex();
        }
        for (i = 0; i < (stratumIndex-1); ++i) {
            if (stratumTable[i].id.equals(stratumId)) {
                return i;
            }
        }
        return defaultStratumTableIndex();
    }
    Stratum stratum(String stratumID) {
        int sti = stratumTableIndex(stratumID);
        return new Stratum(sti);
    }
    List availableStrata() {
        List strata = new ArrayList();
        for (int i = 0; i < (stratumIndex-1); ++i) {
            StratumTableRecord rec = stratumTable[i];
            strata.add(rec.id);
        }
        return strata;
    }
    void ignoreWhite() {
        char ch;
        while (((ch = sdePeek()) == ' ') || (ch == '\t')) {
            sdeAdvance();
        }
    }
    void ignoreLine() {
        char ch;
        while (((ch = sdeRead()) != '\n') && (ch != '\r')) {
        }
        if ((ch == '\r') && (sdePeek() == '\n')) {
            sdeAdvance();
        }
        ignoreWhite(); 
    }
    int readNumber() {
        int value = 0;
        char ch;
        ignoreWhite();
        while (((ch = sdePeek()) >= '0') && (ch <= '9')) {
            sdeAdvance();
            value = (value * 10) + ch - '0';
        }
        ignoreWhite();
        return value;
    }
    void storeFile(int fileId, String sourceName, String sourcePath) {
        assureFileTableSize();
        fileTable[fileIndex].fileId = fileId;
        fileTable[fileIndex].sourceName = sourceName;
        fileTable[fileIndex].sourcePath = sourcePath;
        ++fileIndex;
    }
    void fileLine() {
        int hasAbsolute = 0; 
        int fileId;
        String sourceName;
        String sourcePath = null;
        if (sdePeek() == '+') {
            sdeAdvance();
            hasAbsolute = 1;
        }
        fileId = readNumber();
        sourceName = readLine();
        if (hasAbsolute == 1) {
            sourcePath = readLine();
        }
        storeFile(fileId, sourceName, sourcePath);
    }
    void storeLine(int jplsStart, int jplsEnd, int jplsLineInc,
                  int njplsStart, int njplsEnd, int fileId) {
        assureLineTableSize();
        lineTable[lineIndex].jplsStart = jplsStart;
        lineTable[lineIndex].jplsEnd = jplsEnd;
        lineTable[lineIndex].jplsLineInc = jplsLineInc;
        lineTable[lineIndex].njplsStart = njplsStart;
        lineTable[lineIndex].njplsEnd = njplsEnd;
        lineTable[lineIndex].fileId = fileId;
        ++lineIndex;
    }
    void lineLine() {
        int lineCount = 1;
        int lineIncrement = 1;
        int njplsStart;
        int jplsStart;
        njplsStart = readNumber();
        if (sdePeek() == '#') {
            sdeAdvance();
            currentFileId = readNumber();
        }
        if (sdePeek() == ',') {
            sdeAdvance();
            lineCount = readNumber();
        }
        if (sdeRead() != ':') {
            syntax();
        }
        jplsStart = readNumber();
        if (sdePeek() == ',') {
            sdeAdvance();
            lineIncrement = readNumber();
        }
        ignoreLine(); 
        storeLine(jplsStart,
                  jplsStart + (lineCount * lineIncrement) -1,
                  lineIncrement,
                  njplsStart,
                  njplsStart + lineCount -1,
                  currentFileId);
    }
    void storeStratum(String stratumId) {
        if (stratumIndex > 0) {
            if ((stratumTable[stratumIndex-1].fileIndex
                                            == fileIndex) &&
                (stratumTable[stratumIndex-1].lineIndex
                                            == lineIndex)) {
                --stratumIndex;
            }
        }
        assureStratumTableSize();
        stratumTable[stratumIndex].id = stratumId;
        stratumTable[stratumIndex].fileIndex = fileIndex;
        stratumTable[stratumIndex].lineIndex = lineIndex;
        ++stratumIndex;
        currentFileId = 0;
    }
    void stratumSection() {
        storeStratum(readLine());
    }
    void fileSection() {
        ignoreLine();
        while (sdePeek() != '*') {
            fileLine();
        }
    }
    void lineSection() {
        ignoreLine();
        while (sdePeek() != '*') {
            lineLine();
        }
    }
    void ignoreSection() {
        ignoreLine();
        while (sdePeek() != '*') {
            ignoreLine();
        }
    }
    void createJavaStratum() {
        baseStratumIndex = stratumIndex;
        storeStratum(BASE_STRATUM_NAME);
        storeFile(1, jplsFilename, NullString);
        storeLine(1, 65536, 1, 1, 65536, 1);
        storeStratum("Aux"); 
    }
    void decode() {
        if ((sourceDebugExtension.length() < 4) ||
            (sdeRead() != 'S') ||
            (sdeRead() != 'M') ||
            (sdeRead() != 'A') ||
            (sdeRead() != 'P')) {
            return; 
        }
        ignoreLine(); 
        jplsFilename = readLine();
        defaultStratumId = readLine();
        createJavaStratum();
        while (true) {
            if (sdeRead() != '*') {
                syntax();
            }
            switch (sdeRead()) {
                case 'S':
                    stratumSection();
                    break;
                case 'F':
                    fileSection();
                    break;
                case 'L':
                    lineSection();
                    break;
                case 'E':
                    storeStratum("*terminator*");
                    isValid = true;
                    return;
                default:
                    ignoreSection();
            }
        }
    }
    void createProxyForAbsentSDE() {
        jplsFilename = null;
        defaultStratumId = BASE_STRATUM_NAME;
        defaultStratumIndex = stratumIndex;
        createJavaStratum();
        storeStratum("*terminator*");
    }
    private int stiLineTableIndex(int sti, int jplsLine) {
        int i;
        int lineIndexStart;
        int lineIndexEnd;
        lineIndexStart = stratumTable[sti].lineIndex;
        lineIndexEnd = stratumTable[sti+1].lineIndex;
        for (i = lineIndexStart; i < lineIndexEnd; ++i) {
            if ((jplsLine >= lineTable[i].jplsStart) &&
                            (jplsLine <= lineTable[i].jplsEnd)) {
                return i;
            }
        }
        return -1;
    }
    private int stiLineNumber(int sti, int lti, int jplsLine) {
        return lineTable[lti].njplsStart +
                (((jplsLine - lineTable[lti].jplsStart) /
                                   lineTable[lti].jplsLineInc));
    }
    private int fileTableIndex(int sti, int fileId) {
        int i;
        int fileIndexStart = stratumTable[sti].fileIndex;
        int fileIndexEnd = stratumTable[sti+1].fileIndex;
        for (i = fileIndexStart; i < fileIndexEnd; ++i) {
            if (fileTable[i].fileId == fileId) {
                return i;
            }
        }
        return -1;
    }
    private int stiFileTableIndex(int sti, int lti) {
        return fileTableIndex(sti, lineTable[lti].fileId);
    }
    boolean isValid() {
        return isValid;
    }
}

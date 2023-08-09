public class StreamTokenizer {
    public double nval;
    public String sval;
    public static final int TT_EOF = -1;
    public static final int TT_EOL = '\n';
    public static final int TT_NUMBER = -2;
    public static final int TT_WORD = -3;
    private static final int TT_UNKNOWN = -4;
    public int ttype = TT_UNKNOWN;
    private byte tokenTypes[] = new byte[256];
    private static final byte TOKEN_COMMENT = 1;
    private static final byte TOKEN_QUOTE = 2;
    private static final byte TOKEN_WHITE = 4;
    private static final byte TOKEN_WORD = 8;
    private static final byte TOKEN_DIGIT = 16;
    private int lineNumber = 1;
    private boolean forceLowercase;
    private boolean isEOLSignificant;
    private boolean slashStarComments;
    private boolean slashSlashComments;
    private boolean pushBackToken;
    private boolean lastCr;
    private InputStream inStream;
    private Reader inReader;
    private int peekChar = -2;
    private StreamTokenizer() {
        wordChars('A', 'Z');
        wordChars('a', 'z');
        wordChars(160, 255);
        whitespaceChars(0, 32);
        commentChar('/');
        quoteChar('"');
        quoteChar('\'');
        parseNumbers();
    }
    @Deprecated
    public StreamTokenizer(InputStream is) {
        this();
        if (is == null) {
            throw new NullPointerException();
        }
        inStream = is;
    }
    public StreamTokenizer(Reader r) {
        this();
        if (r == null) {
            throw new NullPointerException();
        }
        inReader = r;
    }
    public void commentChar(int ch) {
        if (0 <= ch && ch < tokenTypes.length) {
            tokenTypes[ch] = TOKEN_COMMENT;
        }
    }
    public void eolIsSignificant(boolean flag) {
        isEOLSignificant = flag;
    }
    public int lineno() {
        return lineNumber;
    }
    public void lowerCaseMode(boolean flag) {
        forceLowercase = flag;
    }
    public int nextToken() throws IOException {
        if (pushBackToken) {
            pushBackToken = false;
            if (ttype != TT_UNKNOWN) {
                return ttype;
            }
        }
        sval = null; 
        int currentChar = peekChar == -2 ? read() : peekChar;
        if (lastCr && currentChar == '\n') {
            lastCr = false;
            currentChar = read();
        }
        if (currentChar == -1) {
            return (ttype = TT_EOF);
        }
        byte currentType = currentChar > 255 ? TOKEN_WORD
                : tokenTypes[currentChar];
        while ((currentType & TOKEN_WHITE) != 0) {
            if (currentChar == '\r') {
                lineNumber++;
                if (isEOLSignificant) {
                    lastCr = true;
                    peekChar = -2;
                    return (ttype = TT_EOL);
                }
                if ((currentChar = read()) == '\n') {
                    currentChar = read();
                }
            } else if (currentChar == '\n') {
                lineNumber++;
                if (isEOLSignificant) {
                    peekChar = -2;
                    return (ttype = TT_EOL);
                }
                currentChar = read();
            } else {
                currentChar = read();
            }
            if (currentChar == -1) {
                return (ttype = TT_EOF);
            }
            currentType = currentChar > 255 ? TOKEN_WORD
                    : tokenTypes[currentChar];
        }
        if ((currentType & TOKEN_DIGIT) != 0) {
            StringBuilder digits = new StringBuilder(20);
            boolean haveDecimal = false, checkJustNegative = currentChar == '-';
            while (true) {
                if (currentChar == '.') {
                    haveDecimal = true;
                }
                digits.append((char) currentChar);
                currentChar = read();
                if ((currentChar < '0' || currentChar > '9')
                        && (haveDecimal || currentChar != '.')) {
                    break;
                }
            }
            peekChar = currentChar;
            if (checkJustNegative && digits.length() == 1) {
                return (ttype = '-');
            }
            try {
                nval = Double.valueOf(digits.toString()).doubleValue();
            } catch (NumberFormatException e) {
                nval = 0;
            }
            return (ttype = TT_NUMBER);
        }
        if ((currentType & TOKEN_WORD) != 0) {
            StringBuilder word = new StringBuilder(20);
            while (true) {
                word.append((char) currentChar);
                currentChar = read();
                if (currentChar == -1
                        || (currentChar < 256 && (tokenTypes[currentChar] & (TOKEN_WORD | TOKEN_DIGIT)) == 0)) {
                    break;
                }
            }
            peekChar = currentChar;
            sval = forceLowercase ? word.toString().toLowerCase() : word
                    .toString();
            return (ttype = TT_WORD);
        }
        if (currentType == TOKEN_QUOTE) {
            int matchQuote = currentChar;
            StringBuilder quoteString = new StringBuilder();
            int peekOne = read();
            while (peekOne >= 0 && peekOne != matchQuote && peekOne != '\r'
                    && peekOne != '\n') {
                boolean readPeek = true;
                if (peekOne == '\\') {
                    int c1 = read();
                    if (c1 <= '7' && c1 >= '0') {
                        int digitValue = c1 - '0';
                        c1 = read();
                        if (c1 > '7' || c1 < '0') {
                            readPeek = false;
                        } else {
                            digitValue = digitValue * 8 + (c1 - '0');
                            c1 = read();
                            if (digitValue > 037 || c1 > '7' || c1 < '0') {
                                readPeek = false;
                            } else {
                                digitValue = digitValue * 8 + (c1 - '0');
                            }
                        }
                        if (!readPeek) {
                            quoteString.append((char) digitValue);
                            peekOne = c1;
                        } else {
                            peekOne = digitValue;
                        }
                    } else {
                        switch (c1) {
                            case 'a':
                                peekOne = 0x7;
                                break;
                            case 'b':
                                peekOne = 0x8;
                                break;
                            case 'f':
                                peekOne = 0xc;
                                break;
                            case 'n':
                                peekOne = 0xA;
                                break;
                            case 'r':
                                peekOne = 0xD;
                                break;
                            case 't':
                                peekOne = 0x9;
                                break;
                            case 'v':
                                peekOne = 0xB;
                                break;
                            default:
                                peekOne = c1;
                        }
                    }
                }
                if (readPeek) {
                    quoteString.append((char) peekOne);
                    peekOne = read();
                }
            }
            if (peekOne == matchQuote) {
                peekOne = read();
            }
            peekChar = peekOne;
            ttype = matchQuote;
            sval = quoteString.toString();
            return ttype;
        }
        if (currentChar == '/' && (slashSlashComments || slashStarComments)) {
            if ((currentChar = read()) == '*' && slashStarComments) {
                int peekOne = read();
                while (true) {
                    currentChar = peekOne;
                    peekOne = read();
                    if (currentChar == -1) {
                        peekChar = -1;
                        return (ttype = TT_EOF);
                    }
                    if (currentChar == '\r') {
                        if (peekOne == '\n') {
                            peekOne = read();
                        }
                        lineNumber++;
                    } else if (currentChar == '\n') {
                        lineNumber++;
                    } else if (currentChar == '*' && peekOne == '/') {
                        peekChar = read();
                        return nextToken();
                    }
                }
            } else if (currentChar == '/' && slashSlashComments) {
                while ((currentChar = read()) >= 0 && currentChar != '\r'
                        && currentChar != '\n') {
                }
                peekChar = currentChar;
                return nextToken();
            } else if (currentType != TOKEN_COMMENT) {
                peekChar = currentChar;
                return (ttype = '/');
            }
        }
        if (currentType == TOKEN_COMMENT) {
            while ((currentChar = read()) >= 0 && currentChar != '\r'
                    && currentChar != '\n') {
            }
            peekChar = currentChar;
            return nextToken();
        }
        peekChar = read();
        return (ttype = currentChar);
    }
    public void ordinaryChar(int ch) {
        if (0 <= ch && ch < tokenTypes.length) {
            tokenTypes[ch] = 0;
        }
    }
    public void ordinaryChars(int low, int hi) {
        if (low < 0) {
            low = 0;
        }
        if (hi > tokenTypes.length) {
            hi = tokenTypes.length - 1;
        }
        for (int i = low; i <= hi; i++) {
            tokenTypes[i] = 0;
        }
    }
    public void parseNumbers() {
        for (int i = '0'; i <= '9'; i++) {
            tokenTypes[i] |= TOKEN_DIGIT;
        }
        tokenTypes['.'] |= TOKEN_DIGIT;
        tokenTypes['-'] |= TOKEN_DIGIT;
    }
    public void pushBack() {
        pushBackToken = true;
    }
    public void quoteChar(int ch) {
        if (0 <= ch && ch < tokenTypes.length) {
            tokenTypes[ch] = TOKEN_QUOTE;
        }
    }
    private int read() throws IOException {
        if (inStream == null) {
            return inReader.read();
        }
        return inStream.read();
    }
    public void resetSyntax() {
        for (int i = 0; i < 256; i++) {
            tokenTypes[i] = 0;
        }
    }
    public void slashSlashComments(boolean flag) {
        slashSlashComments = flag;
    }
    public void slashStarComments(boolean flag) {
        slashStarComments = flag;
    }
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Token["); 
        switch (ttype) {
            case TT_EOF:
                result.append("EOF"); 
                break;
            case TT_EOL:
                result.append("EOL"); 
                break;
            case TT_NUMBER:
                result.append("n="); 
                result.append(nval);
                break;
            case TT_WORD:
                result.append(sval);
                break;
            default:
                if (ttype == TT_UNKNOWN || tokenTypes[ttype] == TOKEN_QUOTE) {
                    result.append(sval);
                } else {
                    result.append('\'');
                    result.append((char) ttype);
                    result.append('\'');
                }
        }
        result.append("], line "); 
        result.append(lineNumber);
        return result.toString();
    }
    public void whitespaceChars(int low, int hi) {
        if (low < 0) {
            low = 0;
        }
        if (hi > tokenTypes.length) {
            hi = tokenTypes.length - 1;
        }
        for (int i = low; i <= hi; i++) {
            tokenTypes[i] = TOKEN_WHITE;
        }
    }
    public void wordChars(int low, int hi) {
        if (low < 0) {
            low = 0;
        }
        if (hi > tokenTypes.length) {
            hi = tokenTypes.length - 1;
        }
        for (int i = low; i <= hi; i++) {
            tokenTypes[i] |= TOKEN_WORD;
        }
    }
}

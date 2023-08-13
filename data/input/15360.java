public class DocCommentScanner extends Scanner {
    protected DocCommentScanner(ScannerFactory fac, CharBuffer buffer) {
        super(fac, buffer);
    }
    protected DocCommentScanner(ScannerFactory fac, char[] input, int inputLength) {
        super(fac, input, inputLength);
    }
    private int pos;
    private char[] buf;
    private int bp;
    private int buflen;
    private char ch;
    private int col;
    private int unicodeConversionBp = 0;
    private char[] docCommentBuffer = new char[1024];
    private int docCommentCount;
    private String docComment = null;
    private void expandCommentBuffer() {
        char[] newBuffer = new char[docCommentBuffer.length * 2];
        System.arraycopy(docCommentBuffer, 0, newBuffer,
                         0, docCommentBuffer.length);
        docCommentBuffer = newBuffer;
    }
    private int digit(int base) {
        char c = ch;
        int result = Character.digit(c, base);
        if (result >= 0 && c > 0x7f) {
            ch = "0123456789abcdef".charAt(result);
        }
        return result;
    }
    private void convertUnicode() {
        if (ch == '\\' && unicodeConversionBp != bp) {
            bp++; ch = buf[bp]; col++;
            if (ch == 'u') {
                do {
                    bp++; ch = buf[bp]; col++;
                } while (ch == 'u');
                int limit = bp + 3;
                if (limit < buflen) {
                    int d = digit(16);
                    int code = d;
                    while (bp < limit && d >= 0) {
                        bp++; ch = buf[bp]; col++;
                        d = digit(16);
                        code = (code << 4) + d;
                    }
                    if (d >= 0) {
                        ch = (char)code;
                        unicodeConversionBp = bp;
                        return;
                    }
                }
            } else {
                bp--;
                ch = '\\';
                col--;
            }
        }
    }
    private void scanChar() {
        bp++;
        ch = buf[bp];
        switch (ch) {
        case '\r': 
            col = 0;
            break;
        case '\n': 
            if (bp == 0 || buf[bp-1] != '\r') {
                col = 0;
            }
            break;
        case '\t': 
            col = (col / TabInc * TabInc) + TabInc;
            break;
        case '\\': 
            col++;
            convertUnicode();
            break;
        default:
            col++;
            break;
        }
    }
    private void scanDocCommentChar() {
        scanChar();
        if (ch == '\\') {
            if (buf[bp+1] == '\\' && unicodeConversionBp != bp) {
                if (docCommentCount == docCommentBuffer.length)
                    expandCommentBuffer();
                docCommentBuffer[docCommentCount++] = ch;
                bp++; col++;
            } else {
                convertUnicode();
            }
        }
    }
    public void nextToken() {
        docComment = null;
        super.nextToken();
    }
    public String docComment() {
        return docComment;
    }
    @SuppressWarnings("fallthrough")
    protected void processComment(CommentStyle style) {
        if (style != CommentStyle.JAVADOC) {
            return;
        }
        pos = pos();
        buf = getRawCharacters(pos, endPos());
        buflen = buf.length;
        bp = 0;
        col = 0;
        docCommentCount = 0;
        boolean firstLine = true;
        scanDocCommentChar();
        scanDocCommentChar();
        while (bp < buflen && ch == '*') {
            scanDocCommentChar();
        }
        if (bp < buflen && ch == '/') {
            docComment = "";
            return;
        }
        if (bp < buflen) {
            if (ch == LF) {
                scanDocCommentChar();
                firstLine = false;
            } else if (ch == CR) {
                scanDocCommentChar();
                if (ch == LF) {
                    scanDocCommentChar();
                    firstLine = false;
                }
            }
        }
    outerLoop:
        while (bp < buflen) {
        wsLoop:
            while (bp < buflen) {
                switch(ch) {
                case ' ':
                    scanDocCommentChar();
                    break;
                case '\t':
                    col = ((col - 1) / TabInc * TabInc) + TabInc;
                    scanDocCommentChar();
                    break;
                case FF:
                    col = 0;
                    scanDocCommentChar();
                    break;
                default:
                    break wsLoop;
                }
            }
            if (ch == '*') {
                do {
                    scanDocCommentChar();
                } while (ch == '*');
                if (ch == '/') {
                    break outerLoop;
                }
            } else if (! firstLine) {
                for (int i = 1; i < col; i++) {
                    if (docCommentCount == docCommentBuffer.length)
                        expandCommentBuffer();
                    docCommentBuffer[docCommentCount++] = ' ';
                }
            }
        textLoop:
            while (bp < buflen) {
                switch (ch) {
                case '*':
                    scanDocCommentChar();
                    if (ch == '/') {
                        break outerLoop;
                    }
                    if (docCommentCount == docCommentBuffer.length)
                        expandCommentBuffer();
                    docCommentBuffer[docCommentCount++] = '*';
                    break;
                case ' ':
                case '\t':
                    if (docCommentCount == docCommentBuffer.length)
                        expandCommentBuffer();
                    docCommentBuffer[docCommentCount++] = ch;
                    scanDocCommentChar();
                    break;
                case FF:
                    scanDocCommentChar();
                    break textLoop; 
                case CR: 
                    scanDocCommentChar();
                    if (ch != LF) {
                        if (docCommentCount == docCommentBuffer.length)
                            expandCommentBuffer();
                        docCommentBuffer[docCommentCount++] = (char)LF;
                        break textLoop;
                    }
                case LF: 
                    if (docCommentCount == docCommentBuffer.length)
                        expandCommentBuffer();
                    docCommentBuffer[docCommentCount++] = ch;
                    scanDocCommentChar();
                    break textLoop;
                default:
                    if (docCommentCount == docCommentBuffer.length)
                        expandCommentBuffer();
                    docCommentBuffer[docCommentCount++] = ch;
                    scanDocCommentChar();
                }
            } 
            firstLine = false;
        } 
        if (docCommentCount > 0) {
            int i = docCommentCount - 1;
        trailLoop:
            while (i > -1) {
                switch (docCommentBuffer[i]) {
                case '*':
                    i--;
                    break;
                default:
                    break trailLoop;
                }
            }
            docCommentCount = i + 1;
            docComment = new String(docCommentBuffer, 0 , docCommentCount);
        } else {
            docComment = "";
        }
    }
    public Position.LineMap getLineMap() {
        char[] buf = getRawCharacters();
        return Position.makeLineMap(buf, buf.length, true);
    }
}

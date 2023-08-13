class A2NFilter extends FilterReader {
    private char[] trailChars = null;
    public A2NFilter(Reader in) {
        super(in);
    }
    public int read(char[] buf, int off, int len) throws IOException {
        int numChars = 0;        
        int retChars = 0;        
        char[] cBuf = new char[len];
        int cOffset = 0;         
        boolean eof = false;
        if (trailChars != null) {
            for (int i = 0; i < trailChars.length; i++)
                cBuf[i] = trailChars[i];
            numChars = trailChars.length;
            trailChars = null;
        }
        int n = in.read(cBuf, numChars, len - numChars);
        if (n < 0) {
            eof = true;
            if (numChars == 0)
                return -1;              
        } else {
            numChars += n;
        }
        for (int i = 0; i < numChars;) {
            char c = cBuf[i++];
            if (c != '\\' || (eof && numChars <= 5)) {
                buf[retChars++] = c;
                continue;
            }
            int remaining = numChars - i;
            if (remaining < 5) {
                trailChars = new char[1 + remaining];
                trailChars[0] = c;
                for (int j = 0; j < remaining; j++)
                    trailChars[1 + j] = cBuf[i + j];
                break;
            }
            c = cBuf[i++];
            if (c != 'u') {
                buf[retChars++] = '\\';
                buf[retChars++] = c;
                continue;
            }
            char rc = 0;
            boolean isUE = true;
            try {
                rc = (char)Integer.parseInt(new String(cBuf, i, 4), 16);
            } catch (NumberFormatException x) {
                isUE = false;
            }
            if (isUE && Main.canConvert(rc)) {
                buf[retChars++] = rc;
                i += 4; 
            } else {
                buf[retChars++] = '\\';
                buf[retChars++] = 'u';
                continue;
            }
        }
        return retChars;
    }
    public int read() throws IOException {
        char[] buf = new char[1];
        if (read(buf, 0, 1) == -1)
            return -1;
        else
            return (int)buf[0];
    }
}

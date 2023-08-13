public class Password {
    public static char[] readPassword(InputStream in) throws IOException {
        return readPassword(in, false);
    }
    public static char[] readPassword(InputStream in, boolean isEchoOn)
            throws IOException {
        char[] consoleEntered = null;
        byte[] consoleBytes = null;
        try {
            Console con = null;
            if (!isEchoOn && in == System.in && ((con = System.console()) != null)) {
                consoleEntered = con.readPassword();
                if (consoleEntered != null && consoleEntered.length == 0) {
                    return null;
                }
                consoleBytes = convertToBytes(consoleEntered);
                in = new ByteArrayInputStream(consoleBytes);
            }
            char[] lineBuffer;
            char[] buf;
            int i;
            buf = lineBuffer = new char[128];
            int room = buf.length;
            int offset = 0;
            int c;
            boolean done = false;
            while (!done) {
                switch (c = in.read()) {
                  case -1:
                  case '\n':
                      done = true;
                      break;
                  case '\r':
                    int c2 = in.read();
                    if ((c2 != '\n') && (c2 != -1)) {
                        if (!(in instanceof PushbackInputStream)) {
                            in = new PushbackInputStream(in);
                        }
                        ((PushbackInputStream)in).unread(c2);
                    } else {
                        done = true;
                        break;
                    }
                  default:
                    if (--room < 0) {
                        buf = new char[offset + 128];
                        room = buf.length - offset - 1;
                        System.arraycopy(lineBuffer, 0, buf, 0, offset);
                        Arrays.fill(lineBuffer, ' ');
                        lineBuffer = buf;
                    }
                    buf[offset++] = (char) c;
                    break;
                }
            }
            if (offset == 0) {
                return null;
            }
            char[] ret = new char[offset];
            System.arraycopy(buf, 0, ret, 0, offset);
            Arrays.fill(buf, ' ');
            return ret;
        } finally {
            if (consoleEntered != null) {
                Arrays.fill(consoleEntered, ' ');
            }
            if (consoleBytes != null) {
                Arrays.fill(consoleBytes, (byte)0);
            }
        }
    }
    private static byte[] convertToBytes(char[] pass) {
        if (enc == null) {
            synchronized (Password.class) {
                enc = sun.misc.SharedSecrets.getJavaIOAccess()
                        .charset()
                        .newEncoder()
                        .onMalformedInput(CodingErrorAction.REPLACE)
                        .onUnmappableCharacter(CodingErrorAction.REPLACE);
            }
        }
        byte[] ba = new byte[(int)(enc.maxBytesPerChar() * pass.length)];
        ByteBuffer bb = ByteBuffer.wrap(ba);
        synchronized (enc) {
            enc.reset().encode(CharBuffer.wrap(pass), bb, true);
        }
        if (bb.position() < ba.length) {
            ba[bb.position()] = '\n';
        }
        return ba;
    }
    private static volatile CharsetEncoder enc;
}

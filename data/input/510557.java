public class Wire {
    private final Log log;
    public Wire(Log log) {
        this.log = log;
    }
    private void wire(String header, InputStream instream)
      throws IOException {
        StringBuilder buffer = new StringBuilder();
        int ch;
        while ((ch = instream.read()) != -1) {
            if (ch == 13) {
                buffer.append("[\\r]");
            } else if (ch == 10) {
                    buffer.append("[\\n]\"");
                    buffer.insert(0, "\"");
                    buffer.insert(0, header);
                    log.debug(buffer.toString());
                    buffer.setLength(0);
            } else if ((ch < 32) || (ch > 127)) {
                buffer.append("[0x");
                buffer.append(Integer.toHexString(ch));
                buffer.append("]");
            } else {
                buffer.append((char) ch);
            }
        } 
        if (buffer.length() > 0) {
            buffer.append('\"');
            buffer.insert(0, '\"');
            buffer.insert(0, header);
            log.debug(buffer.toString());
        }
    }
    public boolean enabled() {
        return log.isDebugEnabled();
    }    
    public void output(InputStream outstream)
      throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output may not be null"); 
        }
        wire(">> ", outstream);
    }
    public void input(InputStream instream)
      throws IOException {
        if (instream == null) {
            throw new IllegalArgumentException("Input may not be null"); 
        }
        wire("<< ", instream);
    }
    public void output(byte[] b, int off, int len)
      throws IOException {
        if (b == null) {
            throw new IllegalArgumentException("Output may not be null"); 
        }
        wire(">> ", new ByteArrayInputStream(b, off, len));
    }
    public void input(byte[] b, int off, int len)
      throws IOException {
        if (b == null) {
            throw new IllegalArgumentException("Input may not be null"); 
        }
        wire("<< ", new ByteArrayInputStream(b, off, len));
    }
    public void output(byte[] b)
      throws IOException {
        if (b == null) {
            throw new IllegalArgumentException("Output may not be null"); 
        }
        wire(">> ", new ByteArrayInputStream(b));
    }
    public void input(byte[] b)
      throws IOException {
        if (b == null) {
            throw new IllegalArgumentException("Input may not be null"); 
        }
        wire("<< ", new ByteArrayInputStream(b));
    }
    public void output(int b)
      throws IOException {
        output(new byte[] {(byte) b});
    }
    public void input(int b)
      throws IOException {
        input(new byte[] {(byte) b});
    }
    public void output(final String s)
      throws IOException {
        if (s == null) {
            throw new IllegalArgumentException("Output may not be null"); 
        }
        output(s.getBytes());
    }
    public void input(final String s)
      throws IOException {
        if (s == null) {
            throw new IllegalArgumentException("Input may not be null"); 
        }
        input(s.getBytes());
    }
}

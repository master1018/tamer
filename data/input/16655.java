class DTDInputStream extends FilterReader implements DTDConstants {
    public DTD dtd;
    public Stack stack = new Stack();
    public char str[] = new char[64];
    public int replace = 0;
    public int ln = 1;
    public int ch;
    public DTDInputStream(InputStream in, DTD dtd) throws IOException {
        super(new InputStreamReader(in));
        this.dtd = dtd;
        this.ch = in.read();
    }
    public void error(String msg) {
        System.out.println("line " + ln + ": dtd input error: " + msg);
    }
    public void push(int ch) throws IOException {
        char data[] = {(char)ch};
        push(new CharArrayReader(data));
    }
    public void push(char data[]) throws IOException {
        if (data.length > 0) {
            push(new CharArrayReader(data));
        }
    }
    void push(Reader in) throws IOException {
        stack.push(new Integer(ln));
        stack.push(new Integer(ch));
        stack.push(this.in);
        this.in = in;
        ch = in.read();
    }
    public int read() throws IOException {
        switch (ch) {
          case '%': {
            ch = in.read();
            if (replace > 0) {
                return '%';
            }
            int pos = 0;
            while (((ch >= 'a') && (ch <= 'z')) || ((ch >= 'A') && (ch <= 'Z')) ||
                   ((ch >= '0') && (ch <= '9')) || (ch == '.') || (ch == '-')) {
                str[pos++] = (char)ch;
                ch = in.read();
            }
            if (pos == 0) {
                return '%';
            }
            String nm = new String(str, 0, pos);
            Entity ent = dtd.getEntity(nm);
            if (ent == null) {
                error("undefined entity reference: " + nm);
                return read();
            }
            switch (ch) {
              case '\r':
                ln++;
              case ';':
                ch = in.read();
                break;
              case '\n':
                ln++;
                if ((ch = in.read()) == '\r') {
                    ch = in.read();
                }
                break;
            }
            try {
                push(getEntityInputReader(ent));
            } catch (Exception e) {
                error("entity data not found: " + ent + ", " + ent.getString());
            }
            return read();
          }
          case '\n':
            ln++;
            if ((ch = in.read()) == '\r') {
                ch = in.read();
            }
            return '\n';
          case '\r':
            ln++;
            ch = in.read();
            return '\n';
          case -1:
            if (stack.size() > 0) {
                in = (Reader)stack.pop();
                ch = ((Integer)stack.pop()).intValue();
                ln = ((Integer)stack.pop()).intValue();
                return read();
            }
            return -1;
          default:
            int c = ch;
            ch = in.read();
            return c;
        }
    }
    private Reader getEntityInputReader(Entity ent) throws IOException {
        if ((ent.type & Entity.PUBLIC) != 0) {
            String path = DTDBuilder.mapping.get(ent.getString());
            return new InputStreamReader(new FileInputStream(path));
        }
        if ((ent.type & Entity.SYSTEM) != 0) {
            String path = DTDBuilder.mapping.baseStr +  ent.getString();
            return new InputStreamReader(new FileInputStream(path));
        }
        return new CharArrayReader(ent.data);
    }
}

abstract class QueryHandler {
    protected String urlStart;
    protected String query;
    protected PrintWriter out;
    protected Snapshot snapshot;
    abstract void run();
    void setUrlStart(String s) {
        urlStart = s;
    }
    void setQuery(String s) {
        query = s;
    }
    void setOutput(PrintWriter o) {
        this.out = o;
    }
    void setSnapshot(Snapshot ss) {
        this.snapshot = ss;
    }
    protected String encodeForURL(String s) {
        try {
            s = URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return s;
    }
    protected void startHtml(String title) {
        out.print("<html><title>");
        print(title);
        out.println("</title>");
        out.println("<body bgcolor=\"#ffffff\"><center><h1>");
        print(title);
        out.println("</h1></center>");
    }
    protected void endHtml() {
        out.println("</body></html>");
    }
    protected void error(String msg) {
        out.println(msg);
    }
    protected void printAnchorStart() {
        out.print("<a href=\"");
        out.print(urlStart);
    }
    protected void printThingAnchorTag(long id) {
        printAnchorStart();
        out.print("object/");
        printHex(id);
        out.print("\">");
    }
    protected void printObject(JavaObject obj) {
        printThing(obj);
    }
    protected void printThing(JavaThing thing) {
        if (thing == null) {
            out.print("null");
            return;
        }
        if (thing instanceof JavaHeapObject) {
            JavaHeapObject ho = (JavaHeapObject) thing;
            long id = ho.getId();
            if (id != -1L) {
                if (ho.isNew())
                out.println("<strong>");
                printThingAnchorTag(id);
            }
            print(thing.toString());
            if (id != -1) {
                if (ho.isNew())
                    out.println("[new]</strong>");
                out.print(" (" + ho.getSize() + " bytes)");
                out.println("</a>");
            }
        } else {
            print(thing.toString());
        }
    }
    protected void printRoot(Root root) {
        StackTrace st = root.getStackTrace();
        boolean traceAvailable = (st != null) && (st.getFrames().length != 0);
        if (traceAvailable) {
            printAnchorStart();
            out.print("rootStack/");
            printHex(root.getIndex());
            out.print("\">");
        }
        print(root.getDescription());
        if (traceAvailable) {
            out.print("</a>");
        }
    }
    protected void printClass(JavaClass clazz) {
        if (clazz == null) {
            out.println("null");
            return;
        }
        String name = clazz.getName();
        printAnchorStart();
        out.print("class/");
        print(encodeForURL(clazz));
        out.print("\">");
        print(clazz.toString());
        out.println("</a>");
    }
    protected String encodeForURL(JavaClass clazz) {
        if (clazz.getId() == -1) {
            return encodeForURL(clazz.getName());
        } else {
            return clazz.getIdString();
        }
    }
    protected void printField(JavaField field) {
        print(field.getName() + " (" + field.getSignature() + ")");
    }
    protected void printStatic(JavaStatic member) {
        JavaField f = member.getField();
        printField(f);
        out.print(" : ");
        if (f.hasId()) {
            JavaThing t = member.getValue();
            printThing(t);
        } else {
            print(member.getValue().toString());
        }
    }
    protected void printStackTrace(StackTrace trace) {
        StackFrame[] frames = trace.getFrames();
        for (int i = 0; i < frames.length; i++) {
            StackFrame f = frames[i];
            String clazz = f.getClassName();
            out.print("<font color=purple>");
            print(clazz);
            out.print("</font>");
            print("." + f.getMethodName() + "(" + f.getMethodSignature() + ")");
            out.print(" <bold>:</bold> ");
            print(f.getSourceFileName() + " line " + f.getLineNumber());
            out.println("<br>");
        }
    }
    protected void printHex(long addr) {
        if (snapshot.getIdentifierSize() == 4) {
            out.print(Misc.toHex((int)addr));
        } else {
            out.print(Misc.toHex(addr));
        }
    }
    protected long parseHex(String value) {
        return Misc.parseHex(value);
    }
    protected void print(String str) {
        out.print(Misc.encodeHtml(str));
    }
}

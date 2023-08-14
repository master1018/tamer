public class Hasher {
    static final PrintStream out = System.out;
    static final PrintStream err = System.err;
    boolean verbose = false;
    List keys = new ArrayList();        
    List values = new ArrayList();      
    String pkg = null;                  
    String cln = null;                  
    String vtype = "String";            
    int maxBits = 11;                   
    int maxDepth = 3;                   
    boolean inner = false;              
    boolean empty = false;              
    void usage() {
        err.println("usage: java Hasher [options] [[pkgName.]ClassName]");
        err.println("options:");
        err.println("    -e           generate empty table (ignores value exprs)");
        err.println("    -i           generate a static inner class");
        err.println("    -md depth    max chain depth (default 3)");
        err.println("    -mb bits     max index bits (lg of table size, default 10)");
        err.println("    -t type      value type (default String)");
        err.println("    -v           verbose");
        err.println("Key/value-expression pairs are read from standard input");
        err.println("If class name is given then source is written to standard output");
        System.exit(1);
    }
    Hasher(String[] args) {
        List as = Arrays.asList(args);
        for (Iterator i = as.iterator(); i.hasNext();) {
            String a = (String)i.next();
            if (a.equals("-e")) {
                empty = true;
            } else if (a.equals("-i")) {
                inner = true;
            } else if (a.equals("-v")) {
                verbose = true;
            } else if (a.equals("-md")) {
                if (!i.hasNext())
                    usage();
                maxDepth = Integer.parseInt((String)i.next());
            } else if (a.equals("-mb")) {
                if (!i.hasNext())
                    usage();
                maxBits = Integer.parseInt((String)i.next());
            } else if (a.equals("-t")) {
                if (!i.hasNext())
                    usage();
                vtype = (String)i.next();
            } else if (a.startsWith("-")) {
                usage();
            } else {
                int j = a.lastIndexOf('.');
                if (j >= 0) {
                    pkg = a.substring(0, j);
                    cln = a.substring(j + 1);
                } else {
                    cln = a;
                }
            }
        }
        if (verbose)
            err.println("pkg=" + pkg + ", cln=" + cln);
    }
    Hasher load() throws IOException {
        BufferedReader br
            = new BufferedReader(new InputStreamReader(System.in));
        for (String ln; (ln = br.readLine()) != null;) {
            String[] ws = ln.split(",?\\s+", 2);
            String w = ws[0].replaceAll("\"", "");
            if (keys.contains(w))
                throw new RuntimeException("Duplicate word in input: " + w);
            keys.add(w);
            if (ws.length < 2)
                throw new RuntimeException("Missing expression for key " + w);
            values.add(ws[1]);
        }
        return this;
    }
    Object[] ht;                        
    int nb;                             
    int md;                             
    int mask;                           
    int shift;                          
    int hash(String w) {
        return (w.hashCode() >> shift) & mask;
    }
    void build(int nb, int s) {
        this.nb = nb;
        this.shift = s;
        int n = 1 << nb;
        this.mask = n - 1;
        ht = new Object[n];
        int nw = keys.size();
        for (int i = 0; i < nw; i++) {
            String w = (String)keys.get(i);
            String v = (String)values.get(i);
            int h = hash(w);
            if (ht[h] == null)
                ht[h] = new Object[] { w, v };
            else
                ht[h] = new Object[] { w, v, ht[h] };
        }
        this.md = 0;
        for (int i = 0; i < n; i++) {
            int d = 1;
            for (Object[] a = (Object[])ht[i];
                 a != null && a.length > 2;
                 a = (Object[])a[2], d++);
            this.md = Math.max(md, d);
        }
    }
    Hasher build() {
        for (int nb = 2; nb < maxBits; nb++) {
            for (int s = 0; s < (32 - nb); s++) {
                build(nb, s);
                if (verbose)
                    err.println("nb=" + nb + " s=" + s + " md=" + md);
                if (md <= maxDepth) {
                    out.flush();
                    if (cln != null)
                        err.print(cln + ": ");
                    err.println("Table size " + (1 << nb) + " (" + nb + " bits)"
                                + ", shift " + shift
                                + ", max chain depth " + md);
                    return this;
                }
            }
        }
        throw new RuntimeException("Cannot find a suitable size"
                                   + " within given constraints");
    }
    String get(String k) {
        int h = hash(k);
        Object[] a = (Object[])ht[h];
        for (;;) {
            if (a[0].equals(k))
                return (String)a[1];
            if (a.length < 3)
                return null;
            a = (Object[])a[2];
        }
    }
    Hasher test() {
        if (verbose)
            err.println();
        for (int i = 0, n = keys.size(); i < n; i++) {
            String w = (String)keys.get(i);
            String v = get(w);
            if (verbose)
                err.println(hash(w) + "\t" + w);
            if (!v.equals(values.get(i)))
                throw new Error("Incorrect value: " + w + " --> "
                                + v + ", should be " + values.get(i));
        }
        return this;
    }
    String ind = "";                    
    void genEntry(Object[] a, int depth, PrintWriter pw) {
        Object v = empty ? null : a[1];
        pw.print("new Object[] { \"" + a[0] + "\", " + v);
        if (a.length < 3) {
            pw.print(" }");
            return;
        }
        pw.println(",");
        pw.print(ind + "                     ");
        for (int i = 0; i < depth; i++)
            pw.print("    ");
        genEntry((Object[])a[2], depth + 1, pw);
        pw.print(" }");
    }
    Hasher generate() throws IOException {
        if (cln == null)
            return this;
        PrintWriter pw
            = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
        if (inner)
            ind = "    ";
        if (!inner && pkg != null) {
            pw.println();
            pw.println("package " + pkg + ";");
            pw.println();
        }
        if (inner) {
            pw.println(ind + "private static final class " + cln);
        } else {
            pw.println();
            pw.println("public final class " + cln);
        }
        pw.println(ind + "    extends sun.util.PreHashedMap<" + vtype +">");
        pw.println(ind + "{");
        pw.println();
        pw.println(ind + "    private static final int ROWS = "
                   + ht.length + ";");
        pw.println(ind + "    private static final int SIZE = "
                   + keys.size() + ";");
        pw.println(ind + "    private static final int SHIFT = "
                   + shift + ";");
        pw.println(ind + "    private static final int MASK = 0x"
                   + Integer.toHexString(mask) + ";");
        pw.println();
        pw.println(ind + "    " + (inner ? "private " : "public ")
                   + cln + "() {");
        pw.println(ind + "        super(ROWS, SIZE, SHIFT, MASK);");
        pw.println(ind + "    }");
        pw.println();
        pw.println(ind + "    protected void init(Object[] ht) {");
        for (int i = 0; i < ht.length; i++) {
            if (ht[i] == null)
                continue;
            Object[] a = (Object[])ht[i];
            pw.print(ind + "        ht[" + i + "] = ");
            genEntry(a, 0, pw);
            pw.println(";");
        }
        pw.println(ind + "    }");
        pw.println();
        pw.println(ind + "}");
        if (inner)
            pw.println();
        pw.close();
        return this;
    }
    public static void main(String[] args) throws IOException {
        new Hasher(args)
            .load()
            .build()
            .test()
            .generate();
    }
}

class NewResourcesNames {
    static int MAXLEN = 127;
    static String[] resources = {
        "sun/security/tools/JarSignerResources.java",
        "sun/security/util/Resources.java",
        "sun/security/util/AuthResources.java",
    };
    public static void main(String[] args) throws Exception {
        Map<String,String> allnames = loadResources();
        Set<String> allfound = new HashSet<String>();
        for (String arg: args) {
            allfound.addAll(rewriteFile(arg, "ResourcesMgr.getString(\""));
            allfound.addAll(rewriteFile(arg, "rb.getString(\""));
        }
        allfound.addAll(keyToolEnums());
        allfound.addAll(rewriteFile("sun/security/provider/PolicyFile.java",
                "ResourcesMgr.getString(POLICY+\""));
        if (!allnames.keySet().containsAll(allfound)) {
            err("FATAL: Undefined names");
            for (String name: allfound) {
                if (!allnames.keySet().contains(name)) {
                    err("   " + name);
                }
            }
        }
        if (!allfound.containsAll(allnames.keySet())) {
            System.err.println("WARNING: Unused names");
            for (String name: allnames.keySet()) {
                if (!allfound.contains(name)) {
                    System.err.println(allnames.get(name));
                    System.err.println("  " + normalize(name));
                    System.err.println("  [" + name + "]");
                }
            }
        }
    }
    private static Map<String,String> loadResources() throws Exception {
        Map<String,String> allnames = new HashMap<String,String>();
        for (String f: resources) {
            String clazz =
                    f.replace('/', '.').substring(0, f.length()-5);
            Set<String> expected = loadClass(clazz);
            Set<String> found = rewriteFile(f, "{\"");
            if (!expected.equals(found)) {
                throw new Exception("Expected and found do not match");
            }
            for (String name: found) {
                allnames.put(name, f);
            }
        }
        return allnames;
    }
    private static Set<String> keyToolEnums() throws Exception {
        Set<String> names = new HashSet<String>();
        String file = "sun/security/tools/KeyTool.java";
        System.err.println("Working on " + file);
        File origFile = new File(file);
        File tmpFile = new File(file + ".tmp");
        origFile.renameTo(tmpFile);
        tmpFile.deleteOnExit();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(tmpFile)));
        PrintWriter out = new PrintWriter(new FileOutputStream(origFile));
        int stage = 0;  
        int match = 0;
        while (true) {
            String s = br.readLine();
            if (s == null) {
                break;
            }
            if (s.indexOf("enum Command") >= 0) stage = 1;
            else if (s.indexOf("enum Option") >= 0) stage = 2;
            else if (s.indexOf("private static final String JKS") >= 0) stage = 3;
            if (stage == 1 || stage == 2) {
                if (s.indexOf("(\"") >= 0) {
                    match++;
                    int p1, p2;
                    if (stage == 1) {
                        p1 = s.indexOf("\"");
                        p2 = s.indexOf("\"", p1+1);
                    } else {
                        p2 = s.lastIndexOf("\"");
                        p1 = s.lastIndexOf("\"", p2-1);
                    }
                    String name = s.substring(p1+1, p2);
                    names.add(name);
                    out.println(s.substring(0, p1+1) +
                            normalize(name) +
                            s.substring(p2));
                } else {
                    out.println(s);
                }
            } else {
                out.println(s);
            }
        }
        br.close();
        out.close();
        System.err.println("    GOOD  match is " + match);
        return names;
    }
    private static Set<String> loadClass(String clazz) throws Exception {
        ListResourceBundle lrb =
                (ListResourceBundle)Class.forName(clazz).newInstance();
        Set<String> keys = lrb.keySet();
        Map<String,String> newold = new HashMap<String,String>();
        boolean dup = false;
        for (String k: keys) {
            String key = normalize(k);
            if (newold.containsKey(key)) {
                err("Dup found for " + key + ":");
                err("["+newold.get(key)+"]");
                err("["+k+"]");
                dup = true;
            }
            newold.put(key, k);
        }
        if (dup) throw new Exception();
        return keys;
    }
    private static Set<String> rewriteFile(String file, String pattern)
            throws Exception {
        System.err.println("Working on " + file);
        Set<String> names = new HashSet<String>();
        int plen = pattern.length();
        int match = 0;
        int pmatch = 0;
        int pheadlen = plen - 2;
        String phead = pattern.substring(0, plen-2);
        StringBuilder history = new StringBuilder();
        int hlen = 0;
        File origFile = new File(file);
        File tmpFile = new File(file + ".tmp");
        origFile.renameTo(tmpFile);
        tmpFile.deleteOnExit();
        FileInputStream fis = new FileInputStream(tmpFile);
        FileOutputStream fos = new FileOutputStream(origFile);
        while (true) {
            int ch = fis.read();
            if (ch < 0) break;
            if (!Character.isWhitespace(ch)) {
                history.append((char)ch);
                hlen++;
                if (pheadlen > 0 && hlen >= pheadlen &&
                        history.substring(hlen-pheadlen, hlen).equals(phead)) {
                    pmatch++;
                }
            }
            if (hlen >= plen &&
                    history.substring(hlen-plen, hlen).equals(pattern)) {
                match++;
                history = new StringBuilder();
                hlen = 0;
                fos.write(ch);
                StringBuilder sb = new StringBuilder();
                StringBuilder tail = new StringBuilder();
                boolean in = true;  
                while (true) {
                    int n = fis.read();
                    if (in) {
                        if (n == '\\') {
                            int second = fis.read();
                            switch (second) {
                                case 'n': sb.append('\n'); break;
                                case 'r': sb.append('\r'); break;
                                case 't': sb.append('\t'); break;
                                case '"': sb.append('"'); break;
                                default: throw new Exception(String.format(
                                        "I don't know this escape: %s%c",
                                        sb.toString(), second));
                            }
                        } else if (n == '"') {
                            in = false;
                            tail = new StringBuilder();
                            tail.append('"');
                        } else {
                            sb.append((char)n);
                        }
                    } else {
                        tail.append((char)n);
                        if (n == '"') { 
                            in = true;
                        } else if (n == ',' || n == ')') {  
                            break;
                        } else if (Character.isWhitespace(n) || n == '+') {
                        } else {
                            throw new Exception("Not a correct concat");
                        }
                    }
                }
                String s = sb.toString();
                names.add(s);
                fos.write(normalize(s).getBytes());
                fos.write(tail.toString().getBytes());
            } else {
                fos.write(ch);
            }
        }
        if (pheadlen > 0 && pmatch != match) {
            err("    BAD!! pmatch != match: " + pmatch + " != " + match);
        } else {
            System.err.println("    GOOD  match is " + match);
        }
        fis.close();
        fos.close();
        return names;
    }
    private static String normalize(String s) throws Exception {
        boolean needDot = false;
        int n = 0;
        for (char c: s.toCharArray()) {
            if (c == ' ') n++;
            else n = -10000;
        }
        if (n == 1) return "SPACE";
        else if (n > 1) return "" + n + "SPACE";
        StringBuilder sb = new StringBuilder();
        int dotpos = -1;
        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c) || Character.isDigit(c) ||
                    c == '{' || c == '}') {
                if (needDot) {
                    if (sb.length() <= MAXLEN) dotpos = sb.length();
                    sb.append(".");
                }
                sb.append(c);
                needDot = false;
            } else {
                needDot = true;
            }
        }
        if (sb.length() == 0) {
            if (s.contains("*") && s.contains("\n")) {
                return "STARNN";
            }
            for (char c: s.toCharArray()) {
                switch (c) {
                    case '*': return "STAR";
                    case ',': return "COMMA";
                    case '.': return "PERIOD";
                    case '\n': return "NEWLINE";
                    case '(': return "LPARAM";
                    case ')': return "RPARAM";
                    case ':': return "COLON";
                    case '\'': case '"': return "QUOTE";
                }
            }
            throw new Exception("Unnamed char: [" + s + "]");
        }
        if (needDot) sb.append('.');
        String res = sb.toString();
        if (res.length() > MAXLEN) {
            if (dotpos < 0) throw new Exception("No dot all over? " + s);
            return res.substring(0, dotpos);
        } else {
            return res;
        }
    }
    private static void err(String string) {
        System.out.println("\u001b[1;37;41m" + string + "\u001b[m");
    }
}

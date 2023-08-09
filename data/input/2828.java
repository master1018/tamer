public class Spp {
    public static void main(String args[]) throws Exception {
        Map<String, String> vars = new HashMap<String, String>();
        Set<String> keys = new HashSet<String>();
        boolean be = false;
        for (String arg:args) {
            if (arg.startsWith("-D")) {
                int i = arg.indexOf('=');
                vars.put(arg.substring(2, i),arg.substring(i+1));
            } else if (arg.startsWith("-K")) {
                keys.add(arg.substring(2));
            } else if ("-be".equals(arg)) {
                be = true;
            } else {
                System.err.println("Usage: java build.tools.spp.Spp [-be] [-Kkey] -Dvar=value ... <in >out");
                System.exit(-1);
            }
        }
        StringBuffer out = new StringBuffer();
        new Spp().spp(new Scanner(System.in),
                      out, "",
                      keys, vars, be,
                      false);
        System.out.print(out.toString());
    }
    static final String LNSEP = System.getProperty("line.separator");
    static final String KEY = "([a-zA-Z0-9]+)";
    static final String VAR = "([a-zA-Z0-9_\\-]+)";
    static final String TEXT = "([a-zA-Z0-9&;,.<>/#() \\$]+)"; 
    static final int GN_NOT = 1;
    static final int GN_KEY = 2;
    static final int GN_YES = 3;
    static final int GN_NO  = 5;
    static final int GN_VAR = 6;
    Matcher ifkey = Pattern.compile("^#if\\[(!)?" + KEY + "\\]").matcher("");
    Matcher elsekey = Pattern.compile("^#else\\[(!)?" + KEY + "\\]").matcher("");
    Matcher endkey = Pattern.compile("^#end\\[(!)?" + KEY + "\\]").matcher("");
    Matcher  vardef = Pattern.compile("\\{#if\\[(!)?" + KEY + "\\]\\?" + TEXT + "(:"+ TEXT + ")?\\}|\\$" + VAR + "\\$").matcher("");
    Matcher  vardef2 = Pattern.compile("\\$" + VAR + "\\$").matcher("");
    void append(StringBuffer buf, String ln,
                Set<String> keys, Map<String, String> vars) {
        vardef.reset(ln);
        while (vardef.find()) {
            String repl = "";
            if (vardef.group(GN_VAR) != null)
                repl = vars.get(vardef.group(GN_VAR));
            else {
                boolean test = keys.contains(vardef.group(GN_KEY));
                if (vardef.group(GN_NOT) != null)
                    test = !test;
                repl = test?vardef.group(GN_YES):vardef.group(GN_NO);
                if (repl == null)
                    repl = "";
                else {  
                    while (vardef2.reset(repl).find()) {
                        repl = vardef2.replaceFirst(vars.get(vardef2.group(1)));
                    }
                }
            }
            vardef.appendReplacement(buf, repl);
        }
        vardef.appendTail(buf);
    }
    boolean spp(Scanner in, StringBuffer buf, String key,
                Set<String> keys, Map<String, String> vars,
                boolean be, boolean skip) {
        while (in.hasNextLine()) {
            String ln = in.nextLine();
            if (be) {
                if (ln.startsWith("#begin")) {
                    buf.setLength(0);      
                    continue;
                }
                if (ln.equals("#end")) {
                    while (in.hasNextLine())
                        in.nextLine();
                    return true;           
                }
            }
            if (ifkey.reset(ln).find()) {
                String k = ifkey.group(GN_KEY);
                boolean test = keys.contains(k);
                if (ifkey.group(GN_NOT) != null)
                    test = !test;
                buf.append(LNSEP);
                if (!spp(in, buf, k, keys, vars, be, skip || !test)) {
                    spp(in, buf, k, keys, vars, be, skip || test);
                }
                continue;
            }
            if (elsekey.reset(ln).find()) {
                if (!key.equals(elsekey.group(GN_KEY))) {
                    throw new Error("Mis-matched #if-else-end at line <" + ln + ">");
                }
                buf.append(LNSEP);
                return false;
            }
            if (endkey.reset(ln).find()) {
                if (!key.equals(endkey.group(GN_KEY))) {
                    throw new Error("Mis-matched #if-else-end at line <" + ln + ">");
                }
                buf.append(LNSEP);
                return true;
            }
            if (ln.startsWith("#warn")) {
                ln = "
            } else if (ln.trim().startsWith("
                ln = "";
            }
            if (!skip) {
                append(buf, ln, keys, vars);
            }
            buf.append(LNSEP);
        }
        return true;
    }
}

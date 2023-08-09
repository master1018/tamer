public class Debug {
    private String prefix;
    private static String args;
    static {
        args = java.security.AccessController.doPrivileged
                (new sun.security.action.GetPropertyAction
                ("java.security.debug"));
        String args2 = java.security.AccessController.doPrivileged
                (new sun.security.action.GetPropertyAction
                ("java.security.auth.debug"));
        if (args == null) {
            args = args2;
        } else {
            if (args2 != null)
               args = args + "," + args2;
        }
        if (args != null) {
            args = marshal(args);
            if (args.equals("help")) {
                Help();
            }
        }
    }
    public static void Help()
    {
        System.err.println();
        System.err.println("all           turn on all debugging");
        System.err.println("access        print all checkPermission results");
        System.err.println("combiner      SubjectDomainCombiner debugging");
        System.err.println("gssloginconfig");
        System.err.println("configfile    JAAS ConfigFile loading");
        System.err.println("configparser  JAAS ConfigFile parsing");
        System.err.println("              GSS LoginConfigImpl debugging");
        System.err.println("jar           jar verification");
        System.err.println("logincontext  login context results");
        System.err.println("policy        loading and granting");
        System.err.println("provider      security provider debugging");
        System.err.println("scl           permissions SecureClassLoader assigns");
        System.err.println();
        System.err.println("The following can be used with access:");
        System.err.println();
        System.err.println("stack         include stack trace");
        System.err.println("domain        dump all domains in context");
        System.err.println("failure       before throwing exception, dump stack");
        System.err.println("              and domain that didn't have permission");
        System.err.println();
        System.err.println("The following can be used with stack and domain:");
        System.err.println();
        System.err.println("permission=<classname>");
        System.err.println("              only dump output if specified permission");
        System.err.println("              is being checked");
        System.err.println("codebase=<URL>");
        System.err.println("              only dump output if specified codebase");
        System.err.println("              is being checked");
        System.err.println();
        System.err.println("Note: Separate multiple options with a comma");
        System.exit(0);
    }
    public static Debug getInstance(String option)
    {
        return getInstance(option, option);
    }
    public static Debug getInstance(String option, String prefix)
    {
        if (isOn(option)) {
            Debug d = new Debug();
            d.prefix = prefix;
            return d;
        } else {
            return null;
        }
    }
    public static boolean isOn(String option)
    {
        if (args == null)
            return false;
        else {
            if (args.indexOf("all") != -1)
                return true;
            else
                return (args.indexOf(option) != -1);
        }
    }
    public void println(String message)
    {
        System.err.println(prefix + ": "+message);
    }
    public void println()
    {
        System.err.println(prefix + ":");
    }
    public static void println(String prefix, String message)
    {
        System.err.println(prefix + ": "+message);
    }
    public static String toHexString(BigInteger b) {
        String hexValue = b.toString(16);
        StringBuffer buf = new StringBuffer(hexValue.length()*2);
        if (hexValue.startsWith("-")) {
            buf.append("   -");
            hexValue = hexValue.substring(1);
        } else {
            buf.append("    ");     
        }
        if ((hexValue.length()%2) != 0) {
            hexValue = "0" + hexValue;
        }
        int i=0;
        while (i < hexValue.length()) {
            buf.append(hexValue.substring(i, i+2));
            i+=2;
            if (i!= hexValue.length()) {
                if ((i%64) == 0) {
                    buf.append("\n    ");     
                } else if (i%8 == 0) {
                    buf.append(" ");     
                }
            }
        }
        return buf.toString();
    }
    private static String marshal(String args) {
        if (args != null) {
            StringBuffer target = new StringBuffer();
            StringBuffer source = new StringBuffer(args);
            String keyReg = "[Pp][Ee][Rr][Mm][Ii][Ss][Ss][Ii][Oo][Nn]=";
            String keyStr = "permission=";
            String reg = keyReg +
                "[a-zA-Z_$][a-zA-Z0-9_$]*([.][a-zA-Z_$][a-zA-Z0-9_$]*)*";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(source);
            StringBuffer left = new StringBuffer();
            while (matcher.find()) {
                String matched = matcher.group();
                target.append(matched.replaceFirst(keyReg, keyStr));
                target.append("  ");
                matcher.appendReplacement(left, "");
            }
            matcher.appendTail(left);
            source = left;
            keyReg = "[Cc][Oo][Dd][Ee][Bb][Aa][Ss][Ee]=";
            keyStr = "codebase=";
            reg = keyReg + "[^, ;]*";
            pattern = Pattern.compile(reg);
            matcher = pattern.matcher(source);
            left = new StringBuffer();
            while (matcher.find()) {
                String matched = matcher.group();
                target.append(matched.replaceFirst(keyReg, keyStr));
                target.append("  ");
                matcher.appendReplacement(left, "");
            }
            matcher.appendTail(left);
            source = left;
            target.append(source.toString().toLowerCase(Locale.ENGLISH));
            return target.toString();
        }
        return null;
    }
    private final static char[] hexDigits = "0123456789abcdef".toCharArray();
    public static String toString(byte[] b) {
        if (b == null) {
            return "(null)";
        }
        StringBuilder sb = new StringBuilder(b.length * 3);
        for (int i = 0; i < b.length; i++) {
            int k = b[i] & 0xff;
            if (i != 0) {
                sb.append(':');
            }
            sb.append(hexDigits[k >>> 4]);
            sb.append(hexDigits[k & 0xf]);
        }
        return sb.toString();
    }
}

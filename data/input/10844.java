public class MessageUtils {
    public MessageUtils() { }
    public static String subst(String patt, String arg) {
        String args[] = { arg };
        return subst(patt, args);
    }
    public static String subst(String patt, String arg1, String arg2) {
        String args[] = { arg1, arg2 };
        return subst(patt, args);
    }
    public static String subst(String patt, String arg1, String arg2,
                               String arg3) {
        String args[] = { arg1, arg2, arg3 };
        return subst(patt, args);
    }
    public static String subst(String patt, String args[]) {
        StringBuffer result = new StringBuffer();
        int len = patt.length();
        for (int i = 0; i >= 0 && i < len; i++) {
            char ch = patt.charAt(i);
            if (ch == '%') {
                if (i != len) {
                    int index = Character.digit(patt.charAt(i + 1), 10);
                    if (index == -1) {
                        result.append(patt.charAt(i + 1));
                        i++;
                    } else if (index < args.length) {
                        result.append(args[index]);
                        i++;
                    }
                }
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
    public static String substProp(String propName, String arg) {
        return subst(System.getProperty(propName), arg);
    }
    public static String substProp(String propName, String arg1, String arg2) {
        return subst(System.getProperty(propName), arg1, arg2);
    }
    public static String substProp(String propName, String arg1, String arg2,
                                   String arg3) {
        return subst(System.getProperty(propName), arg1, arg2, arg3);
    }
    public static native void toStderr(String msg);
    public static native void toStdout(String msg);
    public static void err(String s) {
        toStderr(s + "\n");
    }
    public static void out(String s) {
        toStdout(s + "\n");
    }
    public static void where() {
        Throwable t = new Throwable();
        StackTraceElement[] es = t.getStackTrace();
        for (int i = 1; i < es.length; i++)
            toStderr("\t" + es[i].toString() + "\n");
    }
}

public abstract class Messages {
    private static final String LTB = "%B";
    private static final char NL  = '\n';
    private static final String lineSeparator =
        System.getProperty ("line.separator");
    private static final Properties m = new Properties ();
    private static boolean loadNeeded = true;
    private static final synchronized void loadDefaultProperties () {
        if (!loadNeeded)
            return;
        try {
            m.load (FileLocator.locateLocaleSpecificFileInClassPath (
                "com/sun/tools/corba/se/idl/som/cff/cff.properties"));
        } catch (IOException ioe) { }
        fixMessages (m);  
        loadNeeded = false;
    }
    private static final void fixMessages (Properties p) {
        Enumeration keys = p.keys ();
        Enumeration elems = p.elements ();
        while (keys.hasMoreElements ()) {
            String key = (String) keys.nextElement ();
            String elem = (String) elems.nextElement ();
            int i = elem.indexOf (LTB);
            boolean changed = false;
            while (i != -1) {
                if (i == 0)
                    elem = " " + elem.substring (2);
                else
                    elem = elem.substring (0, i) + " " + elem.substring (i+2);
                changed = true;
                i = elem.indexOf (LTB);
            }
            int lsIncr = lineSeparator.length () - 1;
            for (i=0; i<elem.length (); i++) {
                if (elem.charAt (i) == NL) {
                    elem = elem.substring (0, i) +
                        lineSeparator + elem.substring (i+1);
                    i += lsIncr;
                    changed = true;
                }
            }
            if (changed)
                p.put (key, elem);
        }
    }
    public static final synchronized void msgLoad (String propertyFileName)
        throws IOException {
        m.load (FileLocator.locateLocaleSpecificFileInClassPath (
            propertyFileName));
        fixMessages (m);   
        loadNeeded = false;
    }
    public static final String msg (String msgkey) {
        if (loadNeeded)
            loadDefaultProperties ();
        String msgtext = m.getProperty (msgkey, msgkey);
        return msgtext;
    }
    public static final String msg (String msgkey, String parm) {
        if (loadNeeded)
            loadDefaultProperties ();
        String msgtext = m.getProperty (msgkey, msgkey);
        int i = msgtext.indexOf ("%1");
        if (i >= 0) {
            String ending = "";
            if ((i+2) < msgtext.length ())
                ending = msgtext.substring (i+2);
            return msgtext.substring (0, i) + parm + ending;
        } else
            msgtext += " " + parm;
        return msgtext;
    }
    public static final String msg (String msgkey, int parm) {
        return msg (msgkey, String.valueOf (parm));
    }
    public static final String msg (String msgkey, String parm1, String parm2) {
        if (loadNeeded)
            loadDefaultProperties ();
        String result = m.getProperty (msgkey, msgkey);
        String ending = "";
        int i = result.indexOf ("%1");
        if (i >= 0) {
            if ((i+2) < result.length ())
                ending = result.substring (i+2);
            result = result.substring (0, i) + parm1 + ending;
        } else
            result += " " + parm1;
        i = result.indexOf ("%2");
        if (i >= 0) {
            ending = "";
            if ((i+2) < result.length ())
                ending = result.substring (i+2);
            result = result.substring (0, i) + parm2 + ending;
        } else
            result += " " + parm2;
        return result;
    }
    public static final String msg (String msgkey, int parm1, String parm2) {
        return msg (msgkey, String.valueOf (parm1), parm2);
    }
    public static final String msg (String msgkey, String parm1, int parm2) {
        return msg (msgkey, parm1, String.valueOf (parm2));
    }
    public static final String msg (String msgkey, int parm1, int parm2) {
        return msg (msgkey, String.valueOf (parm1), String.valueOf (parm2));
    }
    public static final String msg (String msgkey, String parm1,
                            String parm2, String parm3) {
        if (loadNeeded)
            loadDefaultProperties ();
        String result = m.getProperty (msgkey, msgkey);
        result = substituteString(result, 1, parm1);
        result = substituteString(result, 2, parm2);
        result = substituteString(result, 3, parm3);
        return result;
    }
    private static String substituteString(String orig, int paramNum,
                     String subst){
        String result = orig;
        String paramSubst = "%"+ paramNum;
        int len = paramSubst.length();
        int index = result.indexOf (paramSubst);
        String ending = "";
        if (index >= 0) {
            if ((index+len) < result.length ())
                ending = result.substring (index+len);
            result = result.substring (0, index) + subst + ending;
        }
        else result += " " + subst;
         return result;
    }
}

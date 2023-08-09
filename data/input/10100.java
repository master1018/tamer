public class PropertyExpander {
    public static class ExpandException extends GeneralSecurityException {
        private static final long serialVersionUID = -7941948581406161702L;
        public ExpandException(String msg) {
            super(msg);
        }
    }
    public static String expand(String value)
        throws ExpandException
    {
        return expand(value, false);
    }
     public static String expand(String value, boolean encodeURL)
         throws ExpandException
     {
        if (value == null)
            return null;
        int p = value.indexOf("${", 0);
        if (p == -1) return value;
        StringBuffer sb = new StringBuffer(value.length());
        int max = value.length();
        int i = 0;  
    scanner:
        while (p < max) {
            if (p > i) {
                sb.append(value.substring(i, p));
                i = p;
            }
            int pe = p+2;
            if (pe < max && value.charAt(pe) == '{') {
                pe = value.indexOf("}}", pe);
                if (pe == -1 || pe+2 == max) {
                    sb.append(value.substring(p));
                    break scanner;
                } else {
                    pe++;
                    sb.append(value.substring(p, pe+1));
                }
            } else {
                while ((pe < max) && (value.charAt(pe) != '}')) {
                    pe++;
                }
                if (pe == max) {
                    sb.append(value.substring(p, pe));
                    break scanner;
                }
                String prop = value.substring(p+2, pe);
                if (prop.equals("/")) {
                    sb.append(java.io.File.separatorChar);
                } else {
                    String val = System.getProperty(prop);
                    if (val != null) {
                        if (encodeURL) {
                            try {
                                if (sb.length() > 0 ||
                                    !(new URI(val)).isAbsolute()) {
                                    val = sun.net.www.ParseUtil.encodePath(val);
                                }
                            } catch (URISyntaxException use) {
                                val = sun.net.www.ParseUtil.encodePath(val);
                            }
                        }
                        sb.append(val);
                    } else {
                        throw new ExpandException(
                                             "unable to expand property " +
                                             prop);
                    }
                }
            }
            i = pe+1;
            p = value.indexOf("${", i);
            if (p == -1) {
                if (i < max) {
                    sb.append(value.substring(i, max));
                }
                break scanner;
            }
        }
        return sb.toString();
    }
}

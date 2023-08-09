public class PlatformClasses  {
    static String[] names = null;
    public static synchronized String[] getNames() {
        if (names == null) {
            LinkedList<String> list = new LinkedList<String>();
            InputStream str
                = PlatformClasses.class
                    .getResourceAsStream("/com/sun/tools/hat/resources/platform_names.txt");
            if (str != null) {
                try {
                    BufferedReader rdr
                        = new BufferedReader(new InputStreamReader(str));
                    for (;;) {
                        String s = rdr.readLine();
                        if (s == null) {
                            break;
                        } else if (s.length() > 0) {
                            list.add(s);
                        }
                    }
                    rdr.close();
                    str.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            names = list.toArray(new String[list.size()]);
        }
        return names;
    }
    public static boolean isPlatformClass(JavaClass clazz) {
        if (clazz.isBootstrap()) {
            return true;
        }
        String name = clazz.getName();
        if (name.startsWith("[")) {
            int index = name.lastIndexOf('[');
            if (index != -1) {
                if (name.charAt(index + 1) != 'L') {
                    return true;
                }
                name = name.substring(index + 2);
            }
        }
        String[] nms = getNames();
        for (int i = 0; i < nms.length; i++) {
            if (name.startsWith(nms[i])) {
                return true;
            }
        }
        return false;
    }
}

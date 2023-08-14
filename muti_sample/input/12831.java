public class GenerateKeyList {
    public static void main(String[] args) throws Exception {
        doOutputFor("sun.util.resources", "CalendarData", System.out);
        doOutputFor("sun.util.resources", "CurrencyNames", System.out);
        doOutputFor("sun.util.resources", "LocaleNames", System.out);
        doOutputFor("sun.util.resources", "TimeZoneNames", System.out);
        doOutputFor("sun.text.resources", "CollationData", System.out);
        doOutputFor("sun.text.resources", "FormatData", System.out);
    };
    public static void doOutputFor(String packageName,
            String resourceBundleName, PrintStream out)
                    throws Exception {
        Locale[] availableLocales = Locale.getAvailableLocales();
        ResourceBundle bundle = ResourceBundle.getBundle(packageName +
                        resourceBundleName, new Locale("", "", ""));
        dumpResourceBundle(resourceBundleName + "/", bundle, out);
        for (int i = 0; i < availableLocales.length; i++) {
            bundle = ResourceBundle.getBundle(packageName + resourceBundleName,
                            availableLocales[i]);
            dumpResourceBundle(resourceBundleName + "/" + availableLocales[i].toString(),
                            bundle, out);
        }
    }
    public static void dumpResourceBundle(String pathName, ResourceBundle bundle,
                    PrintStream out) throws Exception {
        Enumeration keys = bundle.getKeys();
        while(keys.hasMoreElements()) {
            String key = (String)(keys.nextElement());
            dumpResource(pathName + "/" + key, bundle.getObject(key), out);
        }
    }
    public static void dumpResource(String pathName, Object resource, PrintStream out)
                    throws Exception {
        if (resource instanceof String[]) {
            String[] stringList = (String[])resource;
            for (int i = 0; i < stringList.length; i++)
                out.println(pathName + "/" + i);
        }
        else if (resource instanceof String[][]) {
            String[][] stringArray = (String[][])resource;
            if (pathName.startsWith("TimeZoneNames")) {
                for (int i = 0; i < stringArray.length; i++)
                    for (int j = 0; j < stringArray[i].length; j++)
                        out.println(pathName + "/" + i + "/" + j);
            }
            else {
                for (int i = 0; i < stringArray.length; i++)
                    out.println(pathName + "/" + stringArray[i][0]);
            }
        }
        else
            out.println(pathName);
    }
}

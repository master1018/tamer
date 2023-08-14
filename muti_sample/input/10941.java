public class GenDocletBeanInfo {
    static String[] ATTRIBUTE_NAMES = { "bound",
                                     "constrained",
                                     "expert",
                                     "hidden",
                                     "preferred",
                                     "displayname",
                                     "propertyeditorclass",
                                     "customizerclass",
                                     "displayname",
                                     "description",
                                     "enum",
                                     "attribute" };
    private static boolean DEBUG = false;
    private static String fileDir = "";
    private static String templateDir = "";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static int optionLength(String option) {
        if (option.equals("-t"))
            return 2;
        if (option.equals("-d"))
            return 2;
        if (option.equals("-x"))
            return 2;
        return 0;
    }
    public static boolean start(RootDoc doc) {
        readOptions(doc.options());
        if (templateDir.length() == 0) {
            System.err.println("-t option not specified");
            return false;
        }
        if (fileDir.length() == 0) {
            System.err.println("-d option not specified");
            return false;
        }
        GenSwingBeanInfo generator = new GenSwingBeanInfo(fileDir, templateDir, DEBUG);
        Hashtable dochash = new Hashtable();
        DocBeanInfo dbi;
        String previousClass = null;
        ClassDoc[] classes = doc.classes();
        for (int cnt = 0; cnt < classes.length; cnt++) {
            String className = classes[cnt].qualifiedName();
            if (previousClass != null &&
                className.startsWith(previousClass) &&
                className.charAt(previousClass.length()) == '.') {
                continue;
            }
            previousClass = className;
            System.out.println("\n>>> Generating beaninfo for " + className + "...");
            Tag[] tags = classes[cnt].tags();
            for (int i = 0; i < tags.length; i++) {
                if (tags[i].kind().equalsIgnoreCase("@beaninfo")) {
                    if (DEBUG)
                       System.out.println("GenDocletBeanInfo: found @beaninfo tagged Class: " + tags[i].text());
                    dbi = genDocletInfo(tags[i].text(), classes[cnt].name());
                    dochash.put(dbi.name, dbi);
                    break;
                }
            }
            int startPos = -1;
            MethodDoc[] methods = classes[cnt].methods();
            for (int j = 0; j < methods.length; j++) {
                tags = methods[j].tags();
                for (int x = 0; x < tags.length; x++){
                    if (tags[x].kind().equalsIgnoreCase("@beaninfo")){
                        if ((methods[j].name().startsWith("get")) ||
                            (methods[j].name().startsWith("set")))
                            startPos = 3;
                        else if (methods[j].name().startsWith("is"))
                            startPos = 2;
                        else
                            startPos = 0;
                        String propDesc =
                            Introspector.decapitalize((methods[j].name()).substring(startPos));
                        if (DEBUG)
                            System.out.println("GenDocletBeanInfo: found @beaninfo tagged Method: " + tags[x].text());
                        dbi = genDocletInfo(tags[x].text(), propDesc);
                        dochash.put(dbi.name, dbi);
                        break;
                    }
                }
            }
            if (DEBUG) {
                System.out.println(">>>>DocletBeanInfo for class: " + classes[cnt].name());
                Enumeration e = dochash.elements();
                while (e.hasMoreElements()) {
                    DocBeanInfo db = (DocBeanInfo)e.nextElement();
                    System.out.println(db.toString());
                }
            }
            generator.genBeanInfo(classes[cnt].containingPackage().name(),
                                        classes[cnt].name(), dochash);
            dochash.clear();
        } 
        return true;
    }
    private static void readOptions(String[][] options)  {
        for (int i = 0; i < options.length; i++){
            if (options[i][0].equals("-t")) {
                templateDir = options[i][1];
            } else if (options[i][0].equals("-d")) {
                fileDir = options[i][1];
            } else if (options[i][0].equals("-x")){
                if (options[i][1].equals("true"))
                    DEBUG=true;
                else
                    DEBUG=false;
            }
        }
    }
    private static DocBeanInfo genDocletInfo(String text, String name) {
        int beanflags = 0;
        String desc = "null";
        String displayname = "null";
        String propertyeditorclass = "null";
        String customizerclass = "null";
        String value = "null";
        HashMap attribs = null;
        HashMap enums = null;
        int index;
        for (int j = 0; j < ATTRIBUTE_NAMES.length; j++){
            index = 0;
            if ((index = text.indexOf(ATTRIBUTE_NAMES[j])) != -1){
                value = getValue((text).substring(index),ATTRIBUTE_NAMES[j]);
                if (ATTRIBUTE_NAMES[j].equalsIgnoreCase("attribute")) {
                    attribs = getAttributeMap(value, " ");
                }
                if (ATTRIBUTE_NAMES[j].equalsIgnoreCase("enum")) {
                    enums = getAttributeMap(value, " \n");
                }
                else if (ATTRIBUTE_NAMES[j].equals("displayname")){
                    displayname = value;
                }
                else if (ATTRIBUTE_NAMES[j].equalsIgnoreCase("propertyeditorclass")) {
                    propertyeditorclass = value;
                }
                else if (ATTRIBUTE_NAMES[j].equalsIgnoreCase("customizerclass")){
                    customizerclass = value;
                }
                else if ((ATTRIBUTE_NAMES[j].equalsIgnoreCase("bound"))
                         && (value.equalsIgnoreCase(TRUE)))
                    beanflags = beanflags | DocBeanInfo.BOUND;
                else if ((ATTRIBUTE_NAMES[j].equalsIgnoreCase("expert"))
                         && (value.equalsIgnoreCase(TRUE)))
                    beanflags = beanflags | DocBeanInfo.EXPERT;
                else if ((ATTRIBUTE_NAMES[j].equalsIgnoreCase("constrained"))
                         && (value.equalsIgnoreCase(TRUE)))
                    beanflags = beanflags | DocBeanInfo.CONSTRAINED;
                else if ((ATTRIBUTE_NAMES[j].equalsIgnoreCase("hidden"))
                         && (value.equalsIgnoreCase(TRUE)))
                    beanflags = beanflags | DocBeanInfo.HIDDEN;
                else if ((ATTRIBUTE_NAMES[j].equalsIgnoreCase("preferred"))
                         && (value.equalsIgnoreCase(TRUE)))
                    beanflags = beanflags | DocBeanInfo.PREFERRED;
                else if (ATTRIBUTE_NAMES[j].equalsIgnoreCase("description")){
                    desc = value;
                }
            }
        }
        return new DocBeanInfo(name, beanflags, desc,displayname,
                                         propertyeditorclass, customizerclass,
                                         attribs, enums);
    }
    private static String getValue(String substring, String prop) {
        StringTokenizer t;
        String value = "null";
        try {
            if (prop.equalsIgnoreCase("attribute")){
                StringBuffer tmp = new StringBuffer();
                try {
                    t = new StringTokenizer(substring, " :\n");
                    t.nextToken().trim();
                    while (t.hasMoreTokens()){
                        tmp.append(t.nextToken().trim()).append(" ");
                        tmp.append(t.nextToken().trim()).append(" ");
                        String test = t.nextToken().trim();
                        if (!(test.equalsIgnoreCase("attribute")))
                            break;
                    }
                } catch (Exception e){
                }
                value = tmp.toString();
            }
            else if (prop.equalsIgnoreCase("enum")){
                t = new StringTokenizer(substring, ":");
                t.nextToken().trim(); 
                StringBuffer tmp = new StringBuffer(t.nextToken().trim());
                for (int i = 0; i < ATTRIBUTE_NAMES.length; i++){
                    if (tmp.toString().endsWith(ATTRIBUTE_NAMES[i])){
                        int len = ATTRIBUTE_NAMES[i].length();
                        tmp.setLength(tmp.length() - len);
                        break;
                    }
                }
                value = tmp.toString();
            }
            else if (prop.equalsIgnoreCase("description")){
                t = new StringTokenizer(substring, ":");
                t.nextToken().trim(); 
                StringBuffer tmp = new StringBuffer(t.nextToken().trim());
                for (int i = 0; i < ATTRIBUTE_NAMES.length; i++){
                    if (tmp.toString().endsWith(ATTRIBUTE_NAMES[i])){
                        int len = ATTRIBUTE_NAMES[i].length();
                        tmp.setLength(tmp.length() - len);
                        break;
                    }
                }
                value = hansalizeIt(tmp.toString());
            }
            else {
                t = new StringTokenizer(substring, ":\n");
                t.nextToken().trim(); 
                value = t.nextToken().trim();
            }
            return value;
        }
        catch (Exception e){
            return "invalidValue";
        }
    }
    private static HashMap getAttributeMap(String str, String delim)  {
        StringTokenizer t = new StringTokenizer(str, delim);
        HashMap map = null;
        String key;
        String value;
        int num = t.countTokens()/2;
        if (num > 0)  {
            map = new HashMap();
            for (int i = 0; i < num; i++) {
                key = t.nextToken().trim();
                value = t.nextToken().trim();
                map.put(key, value);
            }
        }
        return map;
    }
    private static String hansalizeIt(String from){
        char [] chars = from.toCharArray();
        int len = chars.length;
        int toss = 0;
        for (int i = 0; i < len; i++){
            if ((chars[i] == ' ')) {
                if (i+1 < len) {
                    if ((chars[i+1] == ' ' ) || (chars[i+1] == '\n'))
                        {
                            --len;
                            System.arraycopy(chars,i+1,chars,i,len-i);
                            --i;
                        }
                }
            }
            if (chars[i] == '\n'){
                chars[i] = ' ';
                i -= 2;
            }
            if (chars[i] == '\\') {
                if (i+1 < len) {
                    if (chars[i+1] == 'n'){
                        chars[i+1] = ' ';
                        --len;
                        System.arraycopy(chars,i+1, chars,i, len-i);
                        --i;
                    }
                }
            }
        }
        return new String(chars,0,len);
    }
}

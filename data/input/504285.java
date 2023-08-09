public class TypeUtils {
    private void TypeUtils() {}
    private static final HashMap<String,String> sQuickConvert;
    static {
        sQuickConvert = new HashMap<String,String>();
        sQuickConvert.put("boolean", "Z");
        sQuickConvert.put("byte", "B");
        sQuickConvert.put("char", "C");
        sQuickConvert.put("short", "S");
        sQuickConvert.put("int", "I");
        sQuickConvert.put("float", "F");
        sQuickConvert.put("long", "J");
        sQuickConvert.put("double", "D");
        sQuickConvert.put("void", "V");
        sQuickConvert.put("java.lang.Object", "Ljava/lang/Object;");
        sQuickConvert.put("java.lang.String", "Ljava/lang/String;");
        sQuickConvert.put("java.util.ArrayList", "Ljava/util/ArrayList;");
        sQuickConvert.put("java.util.HashMap", "Ljava/util/HashMap;");
    };
    public static String typeToDescriptor(String type) {
        String quick = sQuickConvert.get(type);
        if (quick != null)
            return quick;
        int arrayDepth = 0;
        int firstPosn = -1;
        int posn = -1;
        while ((posn = type.indexOf('[', posn+1)) != -1) {
            if (firstPosn == -1)
                firstPosn = posn;
            arrayDepth++;
        }
        if (firstPosn != -1)
            type = type.substring(0, firstPosn);
        StringBuilder builder = new StringBuilder();
        while (arrayDepth-- > 0)
            builder.append("[");
        quick = sQuickConvert.get(type);
        if (quick != null) {
            builder.append(quick);
        } else {
            builder.append("L");
            builder.append(type.replace('.', '/'));
            builder.append(";");
        }
        return builder.toString();
    }
    public static String simpleClassNameToBinary(String className) {
        return className.replace('.', '$');
    }
    static String classNameOnly(String typeName) {
        int start = typeName.lastIndexOf(".");
        if (start < 0) {
            return typeName;
        } else {
            return typeName.substring(start+1);
        }
    }
    static String packageNameOnly(String typeName) {
        int end = typeName.lastIndexOf(".");
        if (end < 0) {
            return "";
        } else {
            return typeName.substring(0, end);
        }
    }
    public static String ambiguousToBinaryName(String typeName, ApiList apiList) {
        typeName = stripAngleBrackets(typeName);
        if (typeName.length() == 1) {
            typeName = "java.lang.Object";
        } else if (typeName.length() == 3 &&
                   typeName.substring(1, 3).equals("[]")) {
            typeName = "java.lang.Object[]";
        } else if (typeName.length() == 4 &&
                   typeName.substring(1, 4).equals("...")) {
            typeName = "java.lang.Object[]";
        }
        int ellipsisIndex = typeName.indexOf("...");
        if (ellipsisIndex >= 0) {
            String newTypeName = typeName.substring(0, ellipsisIndex) + "[]";
            typeName = newTypeName;
        }
        if (typeName.indexOf('$') >= 0)
            return typeName;
        int lastDot = typeName.lastIndexOf('.');
        if (lastDot < 0)
            return typeName;
        int nextDot = lastDot;
        while (nextDot >= 0) {
            String testName = typeName.substring(0, nextDot);
            if (apiList.getPackage(testName) != null) {
                break;
            }
            nextDot = typeName.lastIndexOf('.', nextDot-1);
        }
        if (nextDot < 0) {
            System.out.println("+++ no pkg name found on " + typeName + typeName.length());
            typeName = typeName.replace('.', '$');
        } else if (nextDot == lastDot) {
        } else {
            String oldClassName = typeName;
            typeName = typeName.substring(0, nextDot+1) +
                typeName.substring(nextDot+1).replace('.', '$');
        }
        return typeName;
    }
    private static String stripAngleBrackets(String str) {
        int ltIndex = str.indexOf('<');
        if (ltIndex < 0)
            return str;
        int gtIndex = str.lastIndexOf('>');
        if (gtIndex < 0) {
            System.err.println("ERROR: found '<' without '>': " + str);
            return str;     
        }
        return str.substring(0, ltIndex) + str.substring(gtIndex+1);
    }
}

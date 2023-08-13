public class NewNamesFormat {
    public static void main(String[] args) throws Exception {
        checkRes("sun.security.util.Resources");
        checkRes("sun.security.util.AuthResources");
        checkRes("sun.security.tools.JarSignerResources");
    }
    private static void checkRes(String resName) throws Exception {
        System.out.println("Checking " + resName + "...");
        Class clazz = Class.forName(resName);
        Method m = clazz.getMethod("getContents");
        Object[][] contents = (Object[][])m.invoke(clazz.newInstance());
        Set<String> keys = new HashSet<String>();
        for (Object[] pair: contents) {
            String key = (String)pair[0];
            if (keys.contains(key)) {
                System.out.println("Found dup: " + key);
                throw new Exception();
            }
            checkKey(key);
            keys.add(key);
        }
    }
    private static void checkKey(String key) throws Exception {
        for (char c: key.toCharArray()) {
            if (Character.isLetter(c) || Character.isDigit(c) ||
                    c == '{' || c == '}' || c == '.') {
            } else {
                System.out.println("Illegal char [" + c + "] in key: " + key);
                throw new Exception();
            }
        }
    }
}

public class Test4676532 {
    private static final String DATA
            = "<java>\n"
            + " <object class=\"test.Test\">\n"
            + "  <void property=\"message\">\n"
            + "   <string>Hello, world</string>\n"
            + "  </void>\n"
            + " </object>\n"
            + "</java> ";
    public static void main(String[] args) throws Exception {
        StringBuilder sb = new StringBuilder(256);
        sb.append("file:");
        sb.append(System.getProperty("test.src", "."));
        sb.append(File.separatorChar);
        sb.append("test.jar");
        URL[] url = {new URL(sb.toString())};
        URLClassLoader cl = new URLClassLoader(url);
        Class type = cl.loadClass("test.Test");
        if (type == null) {
            throw new Error("could not find class test.Test");
        }
        InputStream stream = new ByteArrayInputStream(DATA.getBytes());
        ExceptionListener el = new ExceptionListener() {
            public void exceptionThrown(Exception exception) {
                throw new Error("unexpected exception", exception);
            }
        };
        XMLDecoder decoder = new XMLDecoder(stream, null, el, cl);
        Object object = decoder.readObject();
        decoder.close();
        if (!type.equals(object.getClass())) {
            throw new Error("unexpected " + object.getClass());
        }
    }
}

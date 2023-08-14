public class ConfigFileWithBlank {
    public static void main(String[] args) throws Exception {
        File f = new File("a b c");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write("".getBytes());
        fos.close();
        System.err.println(f.toURI());
        try {
            Configuration.getInstance("JavaLoginConfig", new URIParameter(f.toURI()));
        } finally {
            f.delete();
        }
    }
}

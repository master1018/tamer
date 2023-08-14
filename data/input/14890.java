public class EmptyJar {
    public static void main(String args[]) throws Exception {
        try {
            JarInputStream is = new JarInputStream
                (new ByteArrayInputStream(new byte[0]));
        } catch (NullPointerException e) {
            throw new Exception("unexpected NullPointerException");
        }
    }
}

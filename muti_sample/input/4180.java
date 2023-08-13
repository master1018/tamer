public class InvalidConstructorInput {
    public static void main(String[] args) {
        try {
            byte[] bytes = { 'a' };
            X500Principal p = new X500Principal(bytes);
            throw new SecurityException("test failed: #1");
        } catch (RuntimeException re) {
        }
        try {
            String dir = System.getProperty("test.src");
            if (dir == null)
                dir = ".";
            FileInputStream fis = new FileInputStream
                                (dir + "/InvalidConstructorInput.java");
            X500Principal p = new X500Principal(fis);
            throw new SecurityException("test failed: #2.1");
        } catch (FileNotFoundException fnfe) {
            throw new SecurityException("test failed: #2.2");
        } catch (RuntimeException re) {
        }
        System.out.println("Test passed");
    }
}

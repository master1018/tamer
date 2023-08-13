public class AccessDenied {
    public static void main(String[] args)
                throws Exception {
        File dir = new File(System.getProperty("test.dir", "."),
                         "hugo");
        dir.deleteOnExit();
        if (!dir.mkdir()) {
            throw new Exception("Could not create directory:" + dir);
        }
        System.out.println("Created directory:" + dir);
        File file = new File(System.getProperty("test.dir", "."), "hugo");
        boolean result = file.createNewFile();
        System.out.println("CreateNewFile() for:" + file + " returned:" +
                        result);
        if (result) {
            throw new Exception(
                "Expected createNewFile() to return false but it returned true");
        }
    }
}

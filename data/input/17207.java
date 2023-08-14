public class CreateNewFile {
    public static void main(String[] args) throws Exception {
        File f = new File(System.getProperty("test.dir", "."),
                          "x.CreateNewFile");
        if (f.exists() && !f.delete())
            throw new Exception("Cannot delete test file " + f);
        if (!f.createNewFile())
            throw new Exception("Cannot create new file " + f);
        if (!f.exists())
            throw new Exception("Did not create new file " + f);
        if (f.createNewFile())
            throw new Exception("Created existing file " + f);
        try {
            f = new File("/");
            if (f.createNewFile())
                throw new Exception("Created root directory!");
        } catch (IOException e) {
        }
    }
}

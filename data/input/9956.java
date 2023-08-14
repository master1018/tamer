public class SetReadOnly {
    public static void main(String[] args) throws Exception {
        File d = new File(System.getProperty("test.dir", "."));
        File f = new File(d, "x.SetReadOnly");
        if (f.exists()) {
            if (!f.delete())
                throw new Exception("Can't delete test file " + f);
        }
        if (f.setReadOnly())
            throw new Exception("Succeeded on non-existent file: " + f);
        OutputStream o = new FileOutputStream(f);
        o.write('x');
        o.close();
        if (!f.setReadOnly())
            throw new Exception(f + ": Failed on file");
        if (f.canWrite())
            throw new Exception(f + ": File is writeable");
        f = new File(d, "x.SetReadOnly.dir");
        if (f.exists()) {
            if (!f.delete())
                throw new Exception("Can't delete test directory " + f);
        }
        if (!f.mkdir())
            throw new Exception(f + ": Cannot create directory");
        if (f.setReadOnly() && f.canWrite())
            throw new Exception(f + ": Directory is writeable");
        if (!f.delete())
            throw new Exception(f + ": Cannot delete directory");
    }
}

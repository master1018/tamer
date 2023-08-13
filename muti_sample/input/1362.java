public class RenameDelete {
    public static void main(String[] args) throws Exception {
        boolean success = false;
        if (File.separatorChar != '\\') {
            System.err.println("Not a win32 platform -- test inapplicable");
            return;
        }
        File f1 = new File(".");
        StringBuffer location = new StringBuffer("\\");
        location.append(f1.getCanonicalPath());
        StringBuffer fromLocation = new StringBuffer(location.toString()+"\\From");
        StringBuffer toLocation = new StringBuffer(location.toString()+"\\To");
        f1 = new File(fromLocation.toString());
        File f2 = new File(toLocation.toString());
        if(f1.exists() || f2.exists()) {
            System.err.println("Directories exist -- test not valid");
            return;
        }
        System.err.println("Create:"+f1.mkdir());
        System.err.println("Exist as directory:"+f1.exists()+" "+f1.isDirectory());
        success = f1.renameTo(f2);
        System.err.println("Rename:"+success);
        if (!success)
            throw new RuntimeException("File method rename did not function");
        success = f2.delete();
        System.err.println("Delete:"+success);
        if (!success)
            throw new RuntimeException("File method delete did not function");
    }
}

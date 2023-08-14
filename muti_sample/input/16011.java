public class bug6484091 {
    public static void main(String[] args) {
        File dir = FileSystemView.getFileSystemView().getDefaultDirectory();
        printDirContent(dir);
        System.setSecurityManager(new SecurityManager());
        try {
            printDirContent(dir);
            throw new RuntimeException("Dir content was derived bypass SecurityManager");
        } catch (AccessControlException e) {
        }
    }
    private static void printDirContent(File dir) {
        System.out.println("Files in " + dir.getAbsolutePath() + ":");
        for (File file : dir.listFiles()) {
            System.out.println(file.getName());
        }
    }
}

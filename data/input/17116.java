public class bug6570445 {
    public static void main(String[] args) {
        System.setSecurityManager(new SecurityManager());
        FileSystemView.getFileSystemView().getRoots();
        System.out.println("Passed.");
    }
}

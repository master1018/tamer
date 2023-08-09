public class WinSpecialFiles {
    public static void main(String[] args) throws Exception {
        String osName = System.getProperty("os.name");
        if (!osName.startsWith("Windows")) {
            return;
        }
        File root = new File("C:\\");
        File[] dir = root.listFiles();
        for (int i = 0; i < dir.length; i++) {
            if (!dir[i].exists()) {
                throw new Exception("exists() returns false for <"
                                    + dir[i].getPath() + ">");
            }
            String name = dir[i].getPath().toLowerCase();
            if (name.indexOf("pagefile.sys") != -1 ||
                name.indexOf("hiberfil.sys") != -1) {
                if (dir[i].length() == 0) {
                    throw new Exception("Size of existing <"
                                        + dir[i].getPath()
                                        + " is ZERO");
                }
            }
        }
    }
}

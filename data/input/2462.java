public class MaxPath {
    public static void main(String[] args) throws Exception {
        String osName = System.getProperty("os.name");
        if (!osName.startsWith("Windows")) {
            return;
        }
        int MAX_PATH = 260;
        String dir = new File(".").getAbsolutePath() + "\\";
        String padding = "1234567890123456789012345678901234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890";
        for (int i = 240 - dir.length(); i < MAX_PATH - dir.length(); i++) {
            String longname = dir + padding.substring(0, i);
            try {
                File f = new File(longname);
                if (f.createNewFile()) {
                    if (!f.exists() || !f.canRead()) {
                        throw new RuntimeException("Failed at length: " + longname.length());
                    }
                    f.delete();
                }
            } catch (IOException e) {
                System.out.println("Failed at length: " + longname.length());
                throw e;
            }
        }
    }
}

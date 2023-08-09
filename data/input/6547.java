public class FontFile {
    public static void main(String[] args) throws Exception {
        String sep = System.getProperty("file.separator");
        String fname = ".." + sep + "A.ttf";
        String dir = System.getProperty("test.src");
        if (dir != null) {
            fname = dir + sep + fname;
        }
        final String name = fname;
        System.out.println("Will try to access " + name);
        if (!(new File(name)).canRead()) {
           System.out.println("File not available : can't run test");
           return;
        }
        System.out.println("File is available. Verify no access under SM");
        System.setSecurityManager(new SecurityManager());
        try {
            new FileInputStream(name);
            throw new Error("Something wrong with test environment");
        } catch (SecurityException exc) {
        }
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT,
            new File("nosuchfile") {
                    private boolean read;
                    @Override public String getPath() {
                        if (read) {
                            return name;
                        } else {
                            read = true;
                            return "somefile";
                        }
                    }
                    @Override public boolean canRead() {
                        return true;
                    }
               }
            );
          System.err.println(font.getFontName());
          throw new RuntimeException("No expected exception");
        }  catch (IOException e) {
          System.err.println("Test passed.");
        }
    }
}

    class SourcePath {
    private final char dirSeparator = File.pathSeparatorChar;
    private String pathstr;
    private File[] sourcePath;
    public SourcePath(String pathstr) {
        init(pathstr);
    }
    public SourcePath() {
        init(System.getProperty("env.class.path"));
    }
    private void init(String pathstr) {
        if (pathstr == null ||  pathstr.length() == 0) {
            pathstr = ".";
        }
        int noOfFileSep = 0;
        int index = 0;
        this.pathstr = pathstr; 
        while ((index = pathstr.indexOf(dirSeparator, index)) != -1) {
            noOfFileSep++;
            index++;
        }
        File[] tempPath = new File[noOfFileSep + 1];
        int tempPathIndex = 0;
        int len = pathstr.length();
        int sepPos = 0;
        for (index = 0; index < len; index = sepPos + 1) {
            sepPos = pathstr.indexOf(dirSeparator, index);
            if (sepPos < 0) {
                sepPos = len;
            }
            File file = new File(pathstr.substring(index, sepPos));
            if (file.isDirectory()) {
                tempPath[tempPathIndex++] = file;
            } 
        }
        sourcePath = new File[tempPathIndex];
        System.arraycopy((Object)tempPath, 0, (Object)sourcePath,
                         0, tempPathIndex);
    }
    public File getDirectory(String name) {
        for (int i = 0; i < sourcePath.length; i++) {
            File directoryNeeded = new File(sourcePath[i], name);
            if (directoryNeeded.isDirectory()) {
                return directoryNeeded;
            }
        }
        return null;
    }
    public String toString() {
        return pathstr;
    }
}

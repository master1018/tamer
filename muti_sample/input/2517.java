public class LocalVmManager {
    private String userName;                 
    private File tmpdir;
    private Pattern userPattern;
    private Matcher userMatcher;
    private FilenameFilter userFilter;
    private Pattern filePattern;
    private Matcher fileMatcher;
    private FilenameFilter fileFilter;
    private Pattern tmpFilePattern;
    private Matcher tmpFileMatcher;
    private FilenameFilter tmpFileFilter;
    public LocalVmManager() {
        this(null);
    }
    public LocalVmManager(String user) {
        this.userName = user;
        if (userName == null) {
            tmpdir = new File(PerfDataFile.getTempDirectory());
            userPattern = Pattern.compile(PerfDataFile.userDirNamePattern);
            userMatcher = userPattern.matcher("");
            userFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    userMatcher.reset(name);
                    return userMatcher.lookingAt();
                }
            };
        } else {
            tmpdir = new File(PerfDataFile.getTempDirectory(userName));
        }
        filePattern = Pattern.compile(PerfDataFile.fileNamePattern);
        fileMatcher = filePattern.matcher("");
        fileFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                fileMatcher.reset(name);
                return fileMatcher.matches();
            }
        };
        tmpFilePattern = Pattern.compile(PerfDataFile.tmpFileNamePattern);
        tmpFileMatcher = tmpFilePattern.matcher("");
        tmpFileFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                tmpFileMatcher.reset(name);
                return tmpFileMatcher.matches();
            }
        };
    }
    public synchronized Set<Integer> activeVms() {
        Set<Integer> jvmSet = new HashSet<Integer>();
        if (! tmpdir.isDirectory()) {
            return jvmSet;
        }
        if (userName == null) {
            File[] dirs = tmpdir.listFiles(userFilter);
            for (int i = 0 ; i < dirs.length; i ++) {
                if (!dirs[i].isDirectory()) {
                    continue;
                }
                File[] files = dirs[i].listFiles(fileFilter);
                if (files != null) {
                    for (int j = 0; j < files.length; j++) {
                        if (files[j].isFile() && files[j].canRead()) {
                            jvmSet.add(new Integer(
                                    PerfDataFile.getLocalVmId(files[j])));
                        }
                    }
                }
            }
        } else {
            File[] files = tmpdir.listFiles(fileFilter);
            if (files != null) {
                for (int j = 0; j < files.length; j++) {
                    if (files[j].isFile() && files[j].canRead()) {
                        jvmSet.add(new Integer(
                                PerfDataFile.getLocalVmId(files[j])));
                    }
                }
            }
        }
        File[] files = tmpdir.listFiles(tmpFileFilter);
        if (files != null) {
            for (int j = 0; j < files.length; j++) {
                if (files[j].isFile() && files[j].canRead()) {
                    jvmSet.add(new Integer(
                            PerfDataFile.getLocalVmId(files[j])));
                }
            }
        }
        return jvmSet;
    }
}

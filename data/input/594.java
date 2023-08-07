public class FTPTaskMirrorImpl implements FTPTaskMirror {
    private static final int CODE_521 = 521;
    private static final SimpleDateFormat TIMESTAMP_LOGGING_SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private final FTPTask task;
    private Vector dirCache = new Vector();
    private int transferred = 0;
    private int skipped = 0;
    public FTPTaskMirrorImpl(FTPTask task) {
        this.task = task;
    }
    protected static class FTPFileProxy extends File {
        private final FTPFile file;
        private final String[] parts;
        private final String name;
        public FTPFileProxy(FTPFile file) {
            super(file.getName());
            name = file.getName();
            this.file = file;
            parts = FileUtils.getPathStack(name);
        }
        public FTPFileProxy(String completePath) {
            super(completePath);
            file = null;
            name = completePath;
            parts = FileUtils.getPathStack(completePath);
        }
        public boolean exists() {
            return true;
        }
        public String getAbsolutePath() {
            return name;
        }
        public String getName() {
            return parts.length > 0 ? parts[parts.length - 1] : name;
        }
        public String getParent() {
            String result = "";
            for (int i = 0; i < parts.length - 1; i++) {
                result += File.separatorChar + parts[i];
            }
            return result;
        }
        public String getPath() {
            return name;
        }
        public boolean isAbsolute() {
            return true;
        }
        public boolean isDirectory() {
            return file == null;
        }
        public boolean isFile() {
            return file != null;
        }
        public boolean isHidden() {
            return false;
        }
        public long lastModified() {
            if (file != null) {
                return file.getTimestamp().getTimeInMillis();
            }
            return 0;
        }
        public long length() {
            if (file != null) {
                return file.getSize();
            }
            return 0;
        }
    }
    protected class FTPDirectoryScanner extends DirectoryScanner {
        protected FTPClient ftp = null;
        private String rootPath = null;
        private boolean remoteSystemCaseSensitive = false;
        private boolean remoteSensitivityChecked = false;
        public FTPDirectoryScanner(FTPClient ftp) {
            super();
            this.ftp = ftp;
            this.setFollowSymlinks(false);
        }
        public void scan() {
            if (includes == null) {
                includes = new String[1];
                includes[0] = "**";
            }
            if (excludes == null) {
                excludes = new String[0];
            }
            filesIncluded = new Vector();
            filesNotIncluded = new Vector();
            filesExcluded = new Vector();
            dirsIncluded = new Vector();
            dirsNotIncluded = new Vector();
            dirsExcluded = new Vector();
            try {
                String cwd = ftp.printWorkingDirectory();
                forceRemoteSensitivityCheck();
                checkIncludePatterns();
                clearCaches();
                ftp.changeWorkingDirectory(cwd);
            } catch (IOException e) {
                throw new BuildException("Unable to scan FTP server: ", e);
            }
        }
        private void checkIncludePatterns() {
            Hashtable newroots = new Hashtable();
            for (int icounter = 0; icounter < includes.length; icounter++) {
                String newpattern = SelectorUtils.rtrimWildcardTokens(includes[icounter]);
                newroots.put(newpattern, includes[icounter]);
            }
            if (task.getRemotedir() == null) {
                try {
                    task.setRemotedir(ftp.printWorkingDirectory());
                } catch (IOException e) {
                    throw new BuildException("could not read current ftp directory", task.getLocation());
                }
            }
            AntFTPFile baseFTPFile = new AntFTPRootFile(ftp, task.getRemotedir());
            rootPath = baseFTPFile.getAbsolutePath();
            if (newroots.containsKey("")) {
                scandir(rootPath, "", true);
            } else {
                Enumeration enum2 = newroots.keys();
                while (enum2.hasMoreElements()) {
                    String currentelement = (String) enum2.nextElement();
                    String originalpattern = (String) newroots.get(currentelement);
                    AntFTPFile myfile = new AntFTPFile(baseFTPFile, currentelement);
                    boolean isOK = true;
                    boolean traversesSymlinks = false;
                    String path = null;
                    if (myfile.exists()) {
                        forceRemoteSensitivityCheck();
                        if (remoteSensitivityChecked && remoteSystemCaseSensitive && isFollowSymlinks()) {
                            path = myfile.getFastRelativePath();
                        } else {
                            try {
                                path = myfile.getRelativePath();
                                traversesSymlinks = myfile.isTraverseSymlinks();
                            } catch (IOException be) {
                                throw new BuildException(be, task.getLocation());
                            } catch (BuildException be) {
                                isOK = false;
                            }
                        }
                    } else {
                        isOK = false;
                    }
                    if (isOK) {
                        currentelement = path.replace(task.getSeparator().charAt(0), File.separatorChar);
                        if (!isFollowSymlinks() && traversesSymlinks) {
                            continue;
                        }
                        if (myfile.isDirectory()) {
                            if (isIncluded(currentelement) && currentelement.length() > 0) {
                                accountForIncludedDir(currentelement, myfile, true);
                            } else {
                                if (currentelement.length() > 0) {
                                    if (currentelement.charAt(currentelement.length() - 1) != File.separatorChar) {
                                        currentelement = currentelement + File.separatorChar;
                                    }
                                }
                                scandir(myfile.getAbsolutePath(), currentelement, true);
                            }
                        } else {
                            if (isCaseSensitive && originalpattern.equals(currentelement)) {
                                accountForIncludedFile(currentelement);
                            } else if (!isCaseSensitive && originalpattern.equalsIgnoreCase(currentelement)) {
                                accountForIncludedFile(currentelement);
                            }
                        }
                    }
                }
            }
        }
        protected void scandir(String dir, String vpath, boolean fast) {
            if (fast && hasBeenScanned(vpath)) {
                return;
            }
            try {
                if (!ftp.changeWorkingDirectory(dir)) {
                    return;
                }
                String completePath = null;
                if (!vpath.equals("")) {
                    completePath = rootPath + task.getSeparator() + vpath.replace(File.separatorChar, task.getSeparator().charAt(0));
                } else {
                    completePath = rootPath;
                }
                FTPFile[] newfiles = listFiles(completePath, false);
                if (newfiles == null) {
                    ftp.changeToParentDirectory();
                    return;
                }
                for (int i = 0; i < newfiles.length; i++) {
                    FTPFile file = newfiles[i];
                    if (file != null && !file.getName().equals(".") && !file.getName().equals("..")) {
                        String name = vpath + file.getName();
                        scannedDirs.put(name, new FTPFileProxy(file));
                        if (isFunctioningAsDirectory(ftp, dir, file)) {
                            boolean slowScanAllowed = true;
                            if (!isFollowSymlinks() && file.isSymbolicLink()) {
                                dirsExcluded.addElement(name);
                                slowScanAllowed = false;
                            } else if (isIncluded(name)) {
                                accountForIncludedDir(name, new AntFTPFile(ftp, file, completePath), fast);
                            } else {
                                dirsNotIncluded.addElement(name);
                                if (fast && couldHoldIncluded(name)) {
                                    scandir(file.getName(), name + File.separator, fast);
                                }
                            }
                            if (!fast && slowScanAllowed) {
                                scandir(file.getName(), name + File.separator, fast);
                            }
                        } else {
                            if (!isFollowSymlinks() && file.isSymbolicLink()) {
                                filesExcluded.addElement(name);
                            } else if (isFunctioningAsFile(ftp, dir, file)) {
                                accountForIncludedFile(name);
                            }
                        }
                    }
                }
                ftp.changeToParentDirectory();
            } catch (IOException e) {
                throw new BuildException("Error while communicating with FTP " + "server: ", e);
            }
        }
        private void accountForIncludedFile(String name) {
            if (!filesIncluded.contains(name) && !filesExcluded.contains(name)) {
                if (isIncluded(name)) {
                    if (!isExcluded(name) && isSelected(name, (File) scannedDirs.get(name))) {
                        filesIncluded.addElement(name);
                    } else {
                        filesExcluded.addElement(name);
                    }
                } else {
                    filesNotIncluded.addElement(name);
                }
            }
        }
        private void accountForIncludedDir(String name, AntFTPFile file, boolean fast) {
            if (!dirsIncluded.contains(name) && !dirsExcluded.contains(name)) {
                if (!isExcluded(name)) {
                    if (fast) {
                        if (file.isSymbolicLink()) {
                            try {
                                file.getClient().changeWorkingDirectory(file.curpwd);
                            } catch (IOException ioe) {
                                throw new BuildException("could not change directory to curpwd");
                            }
                            scandir(file.getLink(), name + File.separator, fast);
                        } else {
                            try {
                                file.getClient().changeWorkingDirectory(file.curpwd);
                            } catch (IOException ioe) {
                                throw new BuildException("could not change directory to curpwd");
                            }
                            scandir(file.getName(), name + File.separator, fast);
                        }
                    }
                    dirsIncluded.addElement(name);
                } else {
                    dirsExcluded.addElement(name);
                    if (fast && couldHoldIncluded(name)) {
                        try {
                            file.getClient().changeWorkingDirectory(file.curpwd);
                        } catch (IOException ioe) {
                            throw new BuildException("could not change directory to curpwd");
                        }
                        scandir(file.getName(), name + File.separator, fast);
                    }
                }
            }
        }
        private Map fileListMap = new HashMap();
        private Map scannedDirs = new HashMap();
        private boolean hasBeenScanned(String vpath) {
            return scannedDirs.containsKey(vpath);
        }
        private void clearCaches() {
            fileListMap.clear();
            scannedDirs.clear();
        }
        public FTPFile[] listFiles(String directory, boolean changedir) {
            String currentPath = directory;
            if (changedir) {
                try {
                    boolean result = ftp.changeWorkingDirectory(directory);
                    if (!result) {
                        return null;
                    }
                    currentPath = ftp.printWorkingDirectory();
                } catch (IOException ioe) {
                    throw new BuildException(ioe, task.getLocation());
                }
            }
            if (fileListMap.containsKey(currentPath)) {
                task.log("filelist map used in listing files", Project.MSG_DEBUG);
                return ((FTPFile[]) fileListMap.get(currentPath));
            }
            FTPFile[] result = null;
            try {
                result = ftp.listFiles();
            } catch (IOException ioe) {
                throw new BuildException(ioe, task.getLocation());
            }
            fileListMap.put(currentPath, result);
            if (!remoteSensitivityChecked) {
                checkRemoteSensitivity(result, directory);
            }
            return result;
        }
        private void forceRemoteSensitivityCheck() {
            if (!remoteSensitivityChecked) {
                try {
                    checkRemoteSensitivity(ftp.listFiles(), ftp.printWorkingDirectory());
                } catch (IOException ioe) {
                    throw new BuildException(ioe, task.getLocation());
                }
            }
        }
        public FTPFile[] listFiles(String directory) {
            return listFiles(directory, true);
        }
        private void checkRemoteSensitivity(FTPFile[] array, String directory) {
            if (array == null) {
                return;
            }
            boolean candidateFound = false;
            String target = null;
            for (int icounter = 0; icounter < array.length; icounter++) {
                if (array[icounter] != null && array[icounter].isDirectory()) {
                    if (!array[icounter].getName().equals(".") && !array[icounter].getName().equals("..")) {
                        candidateFound = true;
                        target = fiddleName(array[icounter].getName());
                        task.log("will try to cd to " + target + " where a directory called " + array[icounter].getName() + " exists", Project.MSG_DEBUG);
                        for (int pcounter = 0; pcounter < array.length; pcounter++) {
                            if (array[pcounter] != null && pcounter != icounter && target.equals(array[pcounter].getName())) {
                                candidateFound = false;
                            }
                        }
                        if (candidateFound) {
                            break;
                        }
                    }
                }
            }
            if (candidateFound) {
                try {
                    task.log("testing case sensitivity, attempting to cd to " + target, Project.MSG_DEBUG);
                    remoteSystemCaseSensitive = !ftp.changeWorkingDirectory(target);
                } catch (IOException ioe) {
                    remoteSystemCaseSensitive = true;
                } finally {
                    try {
                        ftp.changeWorkingDirectory(directory);
                    } catch (IOException ioe) {
                        throw new BuildException(ioe, task.getLocation());
                    }
                }
                task.log("remote system is case sensitive : " + remoteSystemCaseSensitive, Project.MSG_VERBOSE);
                remoteSensitivityChecked = true;
            }
        }
        private String fiddleName(String origin) {
            StringBuffer result = new StringBuffer();
            for (int icounter = 0; icounter < origin.length(); icounter++) {
                if (Character.isLowerCase(origin.charAt(icounter))) {
                    result.append(Character.toUpperCase(origin.charAt(icounter)));
                } else if (Character.isUpperCase(origin.charAt(icounter))) {
                    result.append(Character.toLowerCase(origin.charAt(icounter)));
                } else {
                    result.append(origin.charAt(icounter));
                }
            }
            return result.toString();
        }
        protected class AntFTPFile {
            private FTPClient client;
            private String curpwd;
            private FTPFile ftpFile;
            private AntFTPFile parent = null;
            private boolean relativePathCalculated = false;
            private boolean traversesSymlinks = false;
            private String relativePath = "";
            public AntFTPFile(FTPClient client, FTPFile ftpFile, String curpwd) {
                this.client = client;
                this.ftpFile = ftpFile;
                this.curpwd = curpwd;
            }
            public AntFTPFile(AntFTPFile parent, String path) {
                this.parent = parent;
                this.client = parent.client;
                Vector pathElements = SelectorUtils.tokenizePath(path);
                try {
                    boolean result = this.client.changeWorkingDirectory(parent.getAbsolutePath());
                    if (!result) {
                        return;
                    }
                    this.curpwd = parent.getAbsolutePath();
                } catch (IOException ioe) {
                    throw new BuildException("could not change working dir to " + parent.curpwd);
                }
                for (int fcount = 0; fcount < pathElements.size() - 1; fcount++) {
                    String currentPathElement = (String) pathElements.elementAt(fcount);
                    try {
                        boolean result = this.client.changeWorkingDirectory(currentPathElement);
                        if (!result && !isCaseSensitive() && (remoteSystemCaseSensitive || !remoteSensitivityChecked)) {
                            currentPathElement = findPathElementCaseUnsensitive(this.curpwd, currentPathElement);
                            if (currentPathElement == null) {
                                return;
                            }
                        } else if (!result) {
                            return;
                        }
                        this.curpwd = this.curpwd + task.getSeparator() + currentPathElement;
                    } catch (IOException ioe) {
                        throw new BuildException("could not change working dir to " + (String) pathElements.elementAt(fcount) + " from " + this.curpwd);
                    }
                }
                String lastpathelement = (String) pathElements.elementAt(pathElements.size() - 1);
                FTPFile[] theFiles = listFiles(this.curpwd);
                this.ftpFile = getFile(theFiles, lastpathelement);
            }
            private String findPathElementCaseUnsensitive(String parentPath, String soughtPathElement) {
                FTPFile[] theFiles = listFiles(parentPath, false);
                if (theFiles == null) {
                    return null;
                }
                for (int icounter = 0; icounter < theFiles.length; icounter++) {
                    if (theFiles[icounter] != null && theFiles[icounter].getName().equalsIgnoreCase(soughtPathElement)) {
                        return theFiles[icounter].getName();
                    }
                }
                return null;
            }
            public boolean exists() {
                return (ftpFile != null);
            }
            public String getLink() {
                return ftpFile.getLink();
            }
            public String getName() {
                return ftpFile.getName();
            }
            public String getAbsolutePath() {
                return curpwd + task.getSeparator() + ftpFile.getName();
            }
            public String getFastRelativePath() {
                String absPath = getAbsolutePath();
                if (absPath.indexOf(rootPath + task.getSeparator()) == 0) {
                    return absPath.substring(rootPath.length() + task.getSeparator().length());
                }
                return null;
            }
            public String getRelativePath() throws IOException, BuildException {
                if (!relativePathCalculated) {
                    if (parent != null) {
                        traversesSymlinks = parent.isTraverseSymlinks();
                        relativePath = getRelativePath(parent.getAbsolutePath(), parent.getRelativePath());
                    } else {
                        relativePath = getRelativePath(rootPath, "");
                        relativePathCalculated = true;
                    }
                }
                return relativePath;
            }
            private String getRelativePath(String currentPath, String currentRelativePath) {
                Vector pathElements = SelectorUtils.tokenizePath(getAbsolutePath(), task.getSeparator());
                Vector pathElements2 = SelectorUtils.tokenizePath(currentPath, task.getSeparator());
                String relPath = currentRelativePath;
                for (int pcount = pathElements2.size(); pcount < pathElements.size(); pcount++) {
                    String currentElement = (String) pathElements.elementAt(pcount);
                    FTPFile[] theFiles = listFiles(currentPath);
                    FTPFile theFile = null;
                    if (theFiles != null) {
                        theFile = getFile(theFiles, currentElement);
                    }
                    if (!relPath.equals("")) {
                        relPath = relPath + task.getSeparator();
                    }
                    if (theFile == null) {
                        relPath = relPath + currentElement;
                        currentPath = currentPath + task.getSeparator() + currentElement;
                        task.log("Hidden file " + relPath + " assumed to not be a symlink.", Project.MSG_VERBOSE);
                    } else {
                        traversesSymlinks = traversesSymlinks || theFile.isSymbolicLink();
                        relPath = relPath + theFile.getName();
                        currentPath = currentPath + task.getSeparator() + theFile.getName();
                    }
                }
                return relPath;
            }
            public FTPFile getFile(FTPFile[] theFiles, String lastpathelement) {
                if (theFiles == null) {
                    return null;
                }
                for (int fcount = 0; fcount < theFiles.length; fcount++) {
                    if (theFiles[fcount] != null) {
                        if (theFiles[fcount].getName().equals(lastpathelement)) {
                            return theFiles[fcount];
                        } else if (!isCaseSensitive() && theFiles[fcount].getName().equalsIgnoreCase(lastpathelement)) {
                            return theFiles[fcount];
                        }
                    }
                }
                return null;
            }
            public boolean isDirectory() {
                return ftpFile.isDirectory();
            }
            public boolean isSymbolicLink() {
                return ftpFile.isSymbolicLink();
            }
            protected FTPClient getClient() {
                return client;
            }
            protected void setCurpwd(String curpwd) {
                this.curpwd = curpwd;
            }
            public String getCurpwd() {
                return curpwd;
            }
            public boolean isTraverseSymlinks() throws IOException, BuildException {
                if (!relativePathCalculated) {
                    getRelativePath();
                }
                return traversesSymlinks;
            }
            public String toString() {
                return "AntFtpFile: " + curpwd + "%" + ftpFile;
            }
        }
        protected class AntFTPRootFile extends AntFTPFile {
            private String remotedir;
            public AntFTPRootFile(FTPClient aclient, String remotedir) {
                super(aclient, null, remotedir);
                this.remotedir = remotedir;
                try {
                    this.getClient().changeWorkingDirectory(this.remotedir);
                    this.setCurpwd(this.getClient().printWorkingDirectory());
                } catch (IOException ioe) {
                    throw new BuildException(ioe, task.getLocation());
                }
            }
            public String getAbsolutePath() {
                return this.getCurpwd();
            }
            public String getRelativePath() throws BuildException, IOException {
                return "";
            }
        }
    }
    private boolean isFunctioningAsDirectory(FTPClient ftp, String dir, FTPFile file) {
        boolean result = false;
        String currentWorkingDir = null;
        if (file.isDirectory()) {
            return true;
        } else if (file.isFile()) {
            return false;
        }
        try {
            currentWorkingDir = ftp.printWorkingDirectory();
        } catch (IOException ioe) {
            task.log("could not find current working directory " + dir + " while checking a symlink", Project.MSG_DEBUG);
        }
        if (currentWorkingDir != null) {
            try {
                result = ftp.changeWorkingDirectory(file.getLink());
            } catch (IOException ioe) {
                task.log("could not cd to " + file.getLink() + " while checking a symlink", Project.MSG_DEBUG);
            }
            if (result) {
                boolean comeback = false;
                try {
                    comeback = ftp.changeWorkingDirectory(currentWorkingDir);
                } catch (IOException ioe) {
                    task.log("could not cd back to " + dir + " while checking a symlink", Project.MSG_ERR);
                } finally {
                    if (!comeback) {
                        throw new BuildException("could not cd back to " + dir + " while checking a symlink");
                    }
                }
            }
        }
        return result;
    }
    private boolean isFunctioningAsFile(FTPClient ftp, String dir, FTPFile file) {
        if (file.isDirectory()) {
            return false;
        } else if (file.isFile()) {
            return true;
        }
        return !isFunctioningAsDirectory(ftp, dir, file);
    }
    protected void executeRetryable(RetryHandler h, Retryable r, String descr) throws IOException {
        h.execute(r, descr);
    }
    protected int transferFiles(final FTPClient ftp, FileSet fs) throws IOException, BuildException {
        DirectoryScanner ds;
        if (task.getAction() == FTPTask.SEND_FILES) {
            ds = fs.getDirectoryScanner(task.getProject());
        } else {
            ds = new FTPDirectoryScanner(ftp);
            fs.setupDirectoryScanner(ds, task.getProject());
            ds.setFollowSymlinks(fs.isFollowSymlinks());
            ds.scan();
        }
        String[] dsfiles = null;
        if (task.getAction() == FTPTask.RM_DIR) {
            dsfiles = ds.getIncludedDirectories();
        } else {
            dsfiles = ds.getIncludedFiles();
        }
        String dir = null;
        if ((ds.getBasedir() == null) && ((task.getAction() == FTPTask.SEND_FILES) || (task.getAction() == FTPTask.GET_FILES))) {
            throw new BuildException("the dir attribute must be set for send " + "and get actions");
        } else {
            if ((task.getAction() == FTPTask.SEND_FILES) || (task.getAction() == FTPTask.GET_FILES)) {
                dir = ds.getBasedir().getAbsolutePath();
            }
        }
        BufferedWriter bw = null;
        try {
            if (task.getAction() == FTPTask.LIST_FILES) {
                File pd = task.getListing().getParentFile();
                if (!pd.exists()) {
                    pd.mkdirs();
                }
                bw = new BufferedWriter(new FileWriter(task.getListing()));
            }
            RetryHandler h = new RetryHandler(task.getRetriesAllowed(), task);
            if (task.getAction() == FTPTask.RM_DIR) {
                for (int i = dsfiles.length - 1; i >= 0; i--) {
                    final String dsfile = dsfiles[i];
                    executeRetryable(h, new Retryable() {
                        public void execute() throws IOException {
                            rmDir(ftp, dsfile);
                        }
                    }, dsfile);
                }
            } else {
                final BufferedWriter fbw = bw;
                final String fdir = dir;
                if (task.isNewer()) {
                    task.setGranularityMillis(task.getTimestampGranularity().getMilliseconds(task.getAction()));
                }
                for (int i = 0; i < dsfiles.length; i++) {
                    final String dsfile = dsfiles[i];
                    executeRetryable(h, new Retryable() {
                        public void execute() throws IOException {
                            switch(task.getAction()) {
                                case FTPTask.SEND_FILES:
                                    sendFile(ftp, fdir, dsfile);
                                    break;
                                case FTPTask.GET_FILES:
                                    getFile(ftp, fdir, dsfile);
                                    break;
                                case FTPTask.DEL_FILES:
                                    delFile(ftp, dsfile);
                                    break;
                                case FTPTask.LIST_FILES:
                                    listFile(ftp, fbw, dsfile);
                                    break;
                                case FTPTask.CHMOD:
                                    doSiteCommand(ftp, "chmod " + task.getChmod() + " " + resolveFile(dsfile));
                                    transferred++;
                                    break;
                                default:
                                    throw new BuildException("unknown ftp action " + task.getAction());
                            }
                        }
                    }, dsfile);
                }
            }
        } finally {
            if (bw != null) {
                bw.close();
            }
        }
        return dsfiles.length;
    }
    protected void transferFiles(FTPClient ftp) throws IOException, BuildException {
        transferred = 0;
        skipped = 0;
        if (task.getFilesets().size() == 0) {
            throw new BuildException("at least one fileset must be specified.");
        } else {
            for (int i = 0; i < task.getFilesets().size(); i++) {
                FileSet fs = (FileSet) task.getFilesets().elementAt(i);
                if (fs != null) {
                    transferFiles(ftp, fs);
                }
            }
        }
        task.log(transferred + " " + FTPTask.ACTION_TARGET_STRS[task.getAction()] + " " + FTPTask.COMPLETED_ACTION_STRS[task.getAction()]);
        if (skipped != 0) {
            task.log(skipped + " " + FTPTask.ACTION_TARGET_STRS[task.getAction()] + " were not successfully " + FTPTask.COMPLETED_ACTION_STRS[task.getAction()]);
        }
    }
    protected String resolveFile(String file) {
        return file.replace(System.getProperty("file.separator").charAt(0), task.getSeparator().charAt(0));
    }
    protected void createParents(FTPClient ftp, String filename) throws IOException, BuildException {
        File dir = new File(filename);
        if (dirCache.contains(dir)) {
            return;
        }
        Vector parents = new Vector();
        String dirname;
        while ((dirname = dir.getParent()) != null) {
            File checkDir = new File(dirname);
            if (dirCache.contains(checkDir)) {
                break;
            }
            dir = checkDir;
            parents.addElement(dir);
        }
        int i = parents.size() - 1;
        if (i >= 0) {
            String cwd = ftp.printWorkingDirectory();
            String parent = dir.getParent();
            if (parent != null) {
                if (!ftp.changeWorkingDirectory(resolveFile(parent))) {
                    throw new BuildException("could not change to " + "directory: " + ftp.getReplyString());
                }
            }
            while (i >= 0) {
                dir = (File) parents.elementAt(i--);
                if (!ftp.changeWorkingDirectory(dir.getName())) {
                    task.log("creating remote directory " + resolveFile(dir.getPath()), Project.MSG_VERBOSE);
                    if (!ftp.makeDirectory(dir.getName())) {
                        handleMkDirFailure(ftp);
                    }
                    if (!ftp.changeWorkingDirectory(dir.getName())) {
                        throw new BuildException("could not change to " + "directory: " + ftp.getReplyString());
                    }
                }
                dirCache.addElement(dir);
            }
            ftp.changeWorkingDirectory(cwd);
        }
    }
    private long getTimeDiff(FTPClient ftp) {
        long returnValue = 0;
        File tempFile = findFileName(ftp);
        try {
            FILE_UTILS.createNewFile(tempFile);
            long localTimeStamp = tempFile.lastModified();
            BufferedInputStream instream = new BufferedInputStream(new FileInputStream(tempFile));
            ftp.storeFile(tempFile.getName(), instream);
            instream.close();
            boolean success = FTPReply.isPositiveCompletion(ftp.getReplyCode());
            if (success) {
                FTPFile[] ftpFiles = ftp.listFiles(tempFile.getName());
                if (ftpFiles.length == 1) {
                    long remoteTimeStamp = ftpFiles[0].getTimestamp().getTime().getTime();
                    returnValue = localTimeStamp - remoteTimeStamp;
                }
                ftp.deleteFile(ftpFiles[0].getName());
            }
            Delete mydelete = new Delete();
            mydelete.bindToOwner(task);
            mydelete.setFile(tempFile.getCanonicalFile());
            mydelete.execute();
        } catch (Exception e) {
            throw new BuildException(e, task.getLocation());
        }
        return returnValue;
    }
    private File findFileName(FTPClient ftp) {
        FTPFile[] theFiles = null;
        final int maxIterations = 1000;
        for (int counter = 1; counter < maxIterations; counter++) {
            File localFile = FILE_UTILS.createTempFile("ant" + Integer.toString(counter), ".tmp", null, false, false);
            String fileName = localFile.getName();
            boolean found = false;
            try {
                if (theFiles == null) {
                    theFiles = ftp.listFiles();
                }
                for (int counter2 = 0; counter2 < theFiles.length; counter2++) {
                    if (theFiles[counter2] != null && theFiles[counter2].getName().equals(fileName)) {
                        found = true;
                        break;
                    }
                }
            } catch (IOException ioe) {
                throw new BuildException(ioe, task.getLocation());
            }
            if (!found) {
                localFile.deleteOnExit();
                return localFile;
            }
        }
        return null;
    }
    protected boolean isUpToDate(FTPClient ftp, File localFile, String remoteFile) throws IOException, BuildException {
        task.log("checking date for " + remoteFile, Project.MSG_VERBOSE);
        FTPFile[] files = ftp.listFiles(remoteFile);
        if (files == null || files.length == 0) {
            if (task.getAction() == FTPTask.SEND_FILES) {
                task.log("Could not date test remote file: " + remoteFile + "assuming out of date.", Project.MSG_VERBOSE);
                return false;
            } else {
                throw new BuildException("could not date test remote file: " + ftp.getReplyString());
            }
        }
        long remoteTimestamp = files[0].getTimestamp().getTime().getTime();
        long localTimestamp = localFile.lastModified();
        long adjustedRemoteTimestamp = remoteTimestamp + task.getTimeDiffMillis() + task.getGranularityMillis();
        StringBuffer msg;
        synchronized (TIMESTAMP_LOGGING_SDF) {
            msg = new StringBuffer("   [").append(TIMESTAMP_LOGGING_SDF.format(new Date(localTimestamp))).append("] local");
        }
        task.log(msg.toString(), Project.MSG_VERBOSE);
        synchronized (TIMESTAMP_LOGGING_SDF) {
            msg = new StringBuffer("   [").append(TIMESTAMP_LOGGING_SDF.format(new Date(adjustedRemoteTimestamp))).append("] remote");
        }
        if (remoteTimestamp != adjustedRemoteTimestamp) {
            synchronized (TIMESTAMP_LOGGING_SDF) {
                msg.append(" - (raw: ").append(TIMESTAMP_LOGGING_SDF.format(new Date(remoteTimestamp))).append(")");
            }
        }
        task.log(msg.toString(), Project.MSG_VERBOSE);
        if (task.getAction() == FTPTask.SEND_FILES) {
            return adjustedRemoteTimestamp >= localTimestamp;
        } else {
            return localTimestamp >= adjustedRemoteTimestamp;
        }
    }
    protected void doSiteCommand(FTPClient ftp, String theCMD) throws IOException, BuildException {
        boolean rc;
        String[] myReply = null;
        task.log("Doing Site Command: " + theCMD, Project.MSG_VERBOSE);
        rc = ftp.sendSiteCommand(theCMD);
        if (!rc) {
            task.log("Failed to issue Site Command: " + theCMD, Project.MSG_WARN);
        } else {
            myReply = ftp.getReplyStrings();
            for (int x = 0; x < myReply.length; x++) {
                if (myReply[x].indexOf("200") == -1) {
                    task.log(myReply[x], Project.MSG_WARN);
                }
            }
        }
    }
    protected void sendFile(FTPClient ftp, String dir, String filename) throws IOException, BuildException {
        InputStream instream = null;
        try {
            File file = task.getProject().resolveFile(new File(dir, filename).getPath());
            if (task.isNewer() && isUpToDate(ftp, file, resolveFile(filename))) {
                return;
            }
            if (task.isVerbose()) {
                task.log("transferring " + file.getAbsolutePath());
            }
            instream = new BufferedInputStream(new FileInputStream(file));
            createParents(ftp, filename);
            ftp.storeFile(resolveFile(filename), instream);
            boolean success = FTPReply.isPositiveCompletion(ftp.getReplyCode());
            if (!success) {
                String s = "could not put file: " + ftp.getReplyString();
                if (task.isSkipFailedTransfers()) {
                    task.log(s, Project.MSG_WARN);
                    skipped++;
                } else {
                    throw new BuildException(s);
                }
            } else {
                if (task.getChmod() != null) {
                    doSiteCommand(ftp, "chmod " + task.getChmod() + " " + resolveFile(filename));
                }
                task.log("File " + file.getAbsolutePath() + " copied to " + task.getServer(), Project.MSG_VERBOSE);
                transferred++;
            }
        } finally {
            if (instream != null) {
                try {
                    instream.close();
                } catch (IOException ex) {
                }
            }
        }
    }
    protected void delFile(FTPClient ftp, String filename) throws IOException, BuildException {
        if (task.isVerbose()) {
            task.log("deleting " + filename);
        }
        if (!ftp.deleteFile(resolveFile(filename))) {
            String s = "could not delete file: " + ftp.getReplyString();
            if (task.isSkipFailedTransfers()) {
                task.log(s, Project.MSG_WARN);
                skipped++;
            } else {
                throw new BuildException(s);
            }
        } else {
            task.log("File " + filename + " deleted from " + task.getServer(), Project.MSG_VERBOSE);
            transferred++;
        }
    }
    protected void rmDir(FTPClient ftp, String dirname) throws IOException, BuildException {
        if (task.isVerbose()) {
            task.log("removing " + dirname);
        }
        if (!ftp.removeDirectory(resolveFile(dirname))) {
            String s = "could not remove directory: " + ftp.getReplyString();
            if (task.isSkipFailedTransfers()) {
                task.log(s, Project.MSG_WARN);
                skipped++;
            } else {
                throw new BuildException(s);
            }
        } else {
            task.log("Directory " + dirname + " removed from " + task.getServer(), Project.MSG_VERBOSE);
            transferred++;
        }
    }
    protected void getFile(FTPClient ftp, String dir, String filename) throws IOException, BuildException {
        OutputStream outstream = null;
        try {
            File file = task.getProject().resolveFile(new File(dir, filename).getPath());
            if (task.isNewer() && isUpToDate(ftp, file, resolveFile(filename))) {
                return;
            }
            if (task.isVerbose()) {
                task.log("transferring " + filename + " to " + file.getAbsolutePath());
            }
            File pdir = file.getParentFile();
            if (!pdir.exists()) {
                pdir.mkdirs();
            }
            outstream = new BufferedOutputStream(new FileOutputStream(file));
            ftp.retrieveFile(resolveFile(filename), outstream);
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                String s = "could not get file: " + ftp.getReplyString();
                if (task.isSkipFailedTransfers()) {
                    task.log(s, Project.MSG_WARN);
                    skipped++;
                } else {
                    throw new BuildException(s);
                }
            } else {
                task.log("File " + file.getAbsolutePath() + " copied from " + task.getServer(), Project.MSG_VERBOSE);
                transferred++;
                if (task.isPreserveLastModified()) {
                    outstream.close();
                    outstream = null;
                    FTPFile[] remote = ftp.listFiles(resolveFile(filename));
                    if (remote.length > 0) {
                        FILE_UTILS.setFileLastModified(file, remote[0].getTimestamp().getTime().getTime());
                    }
                }
            }
        } finally {
            if (outstream != null) {
                try {
                    outstream.close();
                } catch (IOException ex) {
                }
            }
        }
    }
    protected void listFile(FTPClient ftp, BufferedWriter bw, String filename) throws IOException, BuildException {
        if (task.isVerbose()) {
            task.log("listing " + filename);
        }
        FTPFile[] ftpfiles = ftp.listFiles(resolveFile(filename));
        if (ftpfiles != null && ftpfiles.length > 0) {
            bw.write(ftpfiles[0].toString());
            bw.newLine();
            transferred++;
        }
    }
    protected void makeRemoteDir(FTPClient ftp, String dir) throws IOException, BuildException {
        String workingDirectory = ftp.printWorkingDirectory();
        if (task.isVerbose()) {
            if (dir.indexOf("/") == 0 || workingDirectory == null) {
                task.log("Creating directory: " + dir + " in /");
            } else {
                task.log("Creating directory: " + dir + " in " + workingDirectory);
            }
        }
        if (dir.indexOf("/") == 0) {
            ftp.changeWorkingDirectory("/");
        }
        String subdir = "";
        StringTokenizer st = new StringTokenizer(dir, "/");
        while (st.hasMoreTokens()) {
            subdir = st.nextToken();
            task.log("Checking " + subdir, Project.MSG_DEBUG);
            if (!ftp.changeWorkingDirectory(subdir)) {
                if (!ftp.makeDirectory(subdir)) {
                    int rc = ftp.getReplyCode();
                    if (!(task.isIgnoreNoncriticalErrors() && (rc == FTPReply.CODE_550 || rc == FTPReply.CODE_553 || rc == CODE_521))) {
                        throw new BuildException("could not create directory: " + ftp.getReplyString());
                    }
                    if (task.isVerbose()) {
                        task.log("Directory already exists");
                    }
                } else {
                    if (task.isVerbose()) {
                        task.log("Directory created OK");
                    }
                    ftp.changeWorkingDirectory(subdir);
                }
            }
        }
        if (workingDirectory != null) {
            ftp.changeWorkingDirectory(workingDirectory);
        }
    }
    private void handleMkDirFailure(FTPClient ftp) throws BuildException {
        int rc = ftp.getReplyCode();
        if (!(task.isIgnoreNoncriticalErrors() && (rc == FTPReply.CODE_550 || rc == FTPReply.CODE_553 || rc == CODE_521))) {
            throw new BuildException("could not create directory: " + ftp.getReplyString());
        }
    }
    public void doFTP() throws BuildException {
        FTPClient ftp = null;
        try {
            task.log("Opening FTP connection to " + task.getServer(), Project.MSG_VERBOSE);
            ftp = new FTPClient();
            if (task.isConfigurationSet()) {
                ftp = FTPConfigurator.configure(ftp, task);
            }
            ftp.setRemoteVerificationEnabled(task.getEnableRemoteVerification());
            ftp.connect(task.getServer(), task.getPort());
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                throw new BuildException("FTP connection failed: " + ftp.getReplyString());
            }
            task.log("connected", Project.MSG_VERBOSE);
            task.log("logging in to FTP server", Project.MSG_VERBOSE);
            if ((task.getAccount() != null && !ftp.login(task.getUserid(), task.getPassword(), task.getAccount())) || (task.getAccount() == null && !ftp.login(task.getUserid(), task.getPassword()))) {
                throw new BuildException("Could not login to FTP server");
            }
            task.log("login succeeded", Project.MSG_VERBOSE);
            if (task.isBinary()) {
                ftp.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
                if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                    throw new BuildException("could not set transfer type: " + ftp.getReplyString());
                }
            } else {
                ftp.setFileType(org.apache.commons.net.ftp.FTP.ASCII_FILE_TYPE);
                if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                    throw new BuildException("could not set transfer type: " + ftp.getReplyString());
                }
            }
            if (task.isPassive()) {
                task.log("entering passive mode", Project.MSG_VERBOSE);
                ftp.enterLocalPassiveMode();
                if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                    throw new BuildException("could not enter into passive " + "mode: " + ftp.getReplyString());
                }
            }
            if (task.getInitialSiteCommand() != null) {
                RetryHandler h = new RetryHandler(task.getRetriesAllowed(), task);
                final FTPClient lftp = ftp;
                executeRetryable(h, new Retryable() {
                    public void execute() throws IOException {
                        doSiteCommand(lftp, task.getInitialSiteCommand());
                    }
                }, "initial site command: " + task.getInitialSiteCommand());
            }
            if (task.getUmask() != null) {
                RetryHandler h = new RetryHandler(task.getRetriesAllowed(), task);
                final FTPClient lftp = ftp;
                executeRetryable(h, new Retryable() {
                    public void execute() throws IOException {
                        doSiteCommand(lftp, "umask " + task.getUmask());
                    }
                }, "umask " + task.getUmask());
            }
            if (task.getAction() == FTPTask.MK_DIR) {
                RetryHandler h = new RetryHandler(task.getRetriesAllowed(), task);
                final FTPClient lftp = ftp;
                executeRetryable(h, new Retryable() {
                    public void execute() throws IOException {
                        makeRemoteDir(lftp, task.getRemotedir());
                    }
                }, task.getRemotedir());
            } else if (task.getAction() == FTPTask.SITE_CMD) {
                RetryHandler h = new RetryHandler(task.getRetriesAllowed(), task);
                final FTPClient lftp = ftp;
                executeRetryable(h, new Retryable() {
                    public void execute() throws IOException {
                        doSiteCommand(lftp, task.getSiteCommand());
                    }
                }, "Site Command: " + task.getSiteCommand());
            } else {
                if (task.getRemotedir() != null) {
                    task.log("changing the remote directory", Project.MSG_VERBOSE);
                    ftp.changeWorkingDirectory(task.getRemotedir());
                    if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                        throw new BuildException("could not change remote " + "directory: " + ftp.getReplyString());
                    }
                }
                if (task.isNewer() && task.isTimeDiffAuto()) {
                    task.setTimeDiffMillis(getTimeDiff(ftp));
                }
                task.log(FTPTask.ACTION_STRS[task.getAction()] + " " + FTPTask.ACTION_TARGET_STRS[task.getAction()]);
                transferFiles(ftp);
            }
        } catch (IOException ex) {
            throw new BuildException("error during FTP transfer: " + ex, ex);
        } finally {
            if (ftp != null && ftp.isConnected()) {
                try {
                    task.log("disconnecting", Project.MSG_VERBOSE);
                    ftp.logout();
                    ftp.disconnect();
                } catch (IOException ex) {
                }
            }
        }
    }
}

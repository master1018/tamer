public abstract class FileSystemView {
    static FileSystemView windowsFileSystemView = null;
    static FileSystemView unixFileSystemView = null;
    static FileSystemView genericFileSystemView = null;
    private boolean useSystemExtensionHiding =
            UIManager.getDefaults().getBoolean("FileChooser.useSystemExtensionHiding");
    public static FileSystemView getFileSystemView() {
        if(File.separatorChar == '\\') {
            if(windowsFileSystemView == null) {
                windowsFileSystemView = new WindowsFileSystemView();
            }
            return windowsFileSystemView;
        }
        if(File.separatorChar == '/') {
            if(unixFileSystemView == null) {
                unixFileSystemView = new UnixFileSystemView();
            }
            return unixFileSystemView;
        }
        if(genericFileSystemView == null) {
            genericFileSystemView = new GenericFileSystemView();
        }
        return genericFileSystemView;
    }
    public FileSystemView() {
        final WeakReference<FileSystemView> weakReference = new WeakReference<FileSystemView>(this);
        UIManager.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                FileSystemView fileSystemView = weakReference.get();
                if (fileSystemView == null) {
                    UIManager.removePropertyChangeListener(this);
                } else {
                    if (evt.getPropertyName().equals("lookAndFeel")) {
                        fileSystemView.useSystemExtensionHiding =
                                UIManager.getDefaults().getBoolean("FileChooser.useSystemExtensionHiding");
                    }
                }
            }
        });
    }
    public boolean isRoot(File f) {
        if (f == null || !f.isAbsolute()) {
            return false;
        }
        File[] roots = getRoots();
        for (File root : roots) {
            if (root.equals(f)) {
                return true;
            }
        }
        return false;
    }
    public Boolean isTraversable(File f) {
        return Boolean.valueOf(f.isDirectory());
    }
    public String getSystemDisplayName(File f) {
        if (f == null) {
            return null;
        }
        String name = f.getName();
        if (!name.equals("..") && !name.equals(".") &&
                (useSystemExtensionHiding || !isFileSystem(f) || isFileSystemRoot(f)) &&
                (f instanceof ShellFolder || f.exists())) {
            try {
                name = getShellFolder(f).getDisplayName();
            } catch (FileNotFoundException e) {
                return null;
            }
            if (name == null || name.length() == 0) {
                name = f.getPath(); 
            }
        }
        return name;
    }
    public String getSystemTypeDescription(File f) {
        return null;
    }
    public Icon getSystemIcon(File f) {
        if (f == null) {
            return null;
        }
        ShellFolder sf;
        try {
            sf = getShellFolder(f);
        } catch (FileNotFoundException e) {
            return null;
        }
        Image img = sf.getIcon(false);
        if (img != null) {
            return new ImageIcon(img, sf.getFolderType());
        } else {
            return UIManager.getIcon(f.isDirectory() ? "FileView.directoryIcon" : "FileView.fileIcon");
        }
    }
    public boolean isParent(File folder, File file) {
        if (folder == null || file == null) {
            return false;
        } else if (folder instanceof ShellFolder) {
                File parent = file.getParentFile();
                if (parent != null && parent.equals(folder)) {
                    return true;
                }
            File[] children = getFiles(folder, false);
            for (File child : children) {
                if (file.equals(child)) {
                    return true;
                }
            }
            return false;
        } else {
            return folder.equals(file.getParentFile());
        }
    }
    public File getChild(File parent, String fileName) {
        if (parent instanceof ShellFolder) {
            File[] children = getFiles(parent, false);
            for (File child : children) {
                if (child.getName().equals(fileName)) {
                    return child;
                }
            }
        }
        return createFileObject(parent, fileName);
    }
    public boolean isFileSystem(File f) {
        if (f instanceof ShellFolder) {
            ShellFolder sf = (ShellFolder)f;
            return sf.isFileSystem() && !(sf.isLink() && sf.isDirectory());
        } else {
            return true;
        }
    }
    public abstract File createNewFolder(File containingDir) throws IOException;
    public boolean isHiddenFile(File f) {
        return f.isHidden();
    }
    public boolean isFileSystemRoot(File dir) {
        return ShellFolder.isFileSystemRoot(dir);
    }
    public boolean isDrive(File dir) {
        return false;
    }
    public boolean isFloppyDrive(File dir) {
        return false;
    }
    public boolean isComputerNode(File dir) {
        return ShellFolder.isComputerNode(dir);
    }
    public File[] getRoots() {
        File[] roots = (File[])ShellFolder.get("roots");
        for (int i = 0; i < roots.length; i++) {
            if (isFileSystemRoot(roots[i])) {
                roots[i] = createFileSystemRoot(roots[i]);
            }
        }
        return roots;
    }
    public File getHomeDirectory() {
        return createFileObject(System.getProperty("user.home"));
    }
    public File getDefaultDirectory() {
        File f = (File)ShellFolder.get("fileChooserDefaultFolder");
        if (isFileSystemRoot(f)) {
            f = createFileSystemRoot(f);
        }
        return f;
    }
    public File createFileObject(File dir, String filename) {
        if(dir == null) {
            return new File(filename);
        } else {
            return new File(dir, filename);
        }
    }
    public File createFileObject(String path) {
        File f = new File(path);
        if (isFileSystemRoot(f)) {
            f = createFileSystemRoot(f);
        }
        return f;
    }
    public File[] getFiles(File dir, boolean useFileHiding) {
        List<File> files = new ArrayList<File>();
        if (!(dir instanceof ShellFolder)) {
            try {
                dir = getShellFolder(dir);
            } catch (FileNotFoundException e) {
                return new File[0];
            }
        }
        File[] names = ((ShellFolder) dir).listFiles(!useFileHiding);
        if (names == null) {
            return new File[0];
        }
        for (File f : names) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            if (!(f instanceof ShellFolder)) {
                if (isFileSystemRoot(f)) {
                    f = createFileSystemRoot(f);
                }
                try {
                    f = ShellFolder.getShellFolder(f);
                } catch (FileNotFoundException e) {
                    continue;
                } catch (InternalError e) {
                    continue;
                }
            }
            if (!useFileHiding || !isHiddenFile(f)) {
                files.add(f);
            }
        }
        return files.toArray(new File[files.size()]);
    }
    public File getParentDirectory(File dir) {
        if (dir == null || !dir.exists()) {
            return null;
        }
        ShellFolder sf;
        try {
            sf = getShellFolder(dir);
        } catch (FileNotFoundException e) {
            return null;
        }
        File psf = sf.getParentFile();
        if (psf == null) {
            return null;
        }
        if (isFileSystem(psf)) {
            File f = psf;
            if (!f.exists()) {
                File ppsf = psf.getParentFile();
                if (ppsf == null || !isFileSystem(ppsf)) {
                    f = createFileSystemRoot(f);
                }
            }
            return f;
        } else {
            return psf;
        }
    }
    ShellFolder getShellFolder(File f) throws FileNotFoundException {
        if (!(f instanceof ShellFolder) && !(f instanceof FileSystemRoot) && isFileSystemRoot(f)) {
            f = createFileSystemRoot(f);
        }
        try {
            return ShellFolder.getShellFolder(f);
        } catch (InternalError e) {
            System.err.println("FileSystemView.getShellFolder: f="+f);
            e.printStackTrace();
            return null;
        }
    }
    protected File createFileSystemRoot(File f) {
        return new FileSystemRoot(f);
    }
    static class FileSystemRoot extends File {
        public FileSystemRoot(File f) {
            super(f,"");
        }
        public FileSystemRoot(String s) {
            super(s);
        }
        public boolean isDirectory() {
            return true;
        }
        public String getName() {
            return getPath();
        }
    }
}
class UnixFileSystemView extends FileSystemView {
    private static final String newFolderString =
            UIManager.getString("FileChooser.other.newFolder");
    private static final String newFolderNextString  =
            UIManager.getString("FileChooser.other.newFolder.subsequent");
    public File createNewFolder(File containingDir) throws IOException {
        if(containingDir == null) {
            throw new IOException("Containing directory is null:");
        }
        File newFolder;
        newFolder = createFileObject(containingDir, newFolderString);
        int i = 1;
        while (newFolder.exists() && i < 100) {
            newFolder = createFileObject(containingDir, MessageFormat.format(
                    newFolderNextString, new Integer(i)));
            i++;
        }
        if(newFolder.exists()) {
            throw new IOException("Directory already exists:" + newFolder.getAbsolutePath());
        } else {
            newFolder.mkdirs();
        }
        return newFolder;
    }
    public boolean isFileSystemRoot(File dir) {
        return dir != null && dir.getAbsolutePath().equals("/");
    }
    public boolean isDrive(File dir) {
        return isFloppyDrive(dir);
    }
    public boolean isFloppyDrive(File dir) {
        return false;
    }
    public boolean isComputerNode(File dir) {
        if (dir != null) {
            String parent = dir.getParent();
            if (parent != null && parent.equals("/net")) {
                return true;
            }
        }
        return false;
    }
}
class WindowsFileSystemView extends FileSystemView {
    private static final String newFolderString =
            UIManager.getString("FileChooser.win32.newFolder");
    private static final String newFolderNextString  =
            UIManager.getString("FileChooser.win32.newFolder.subsequent");
    public Boolean isTraversable(File f) {
        return Boolean.valueOf(isFileSystemRoot(f) || isComputerNode(f) || f.isDirectory());
    }
    public File getChild(File parent, String fileName) {
        if (fileName.startsWith("\\")
            && !fileName.startsWith("\\\\")
            && isFileSystem(parent)) {
            String path = parent.getAbsolutePath();
            if (path.length() >= 2
                && path.charAt(1) == ':'
                && Character.isLetter(path.charAt(0))) {
                return createFileObject(path.substring(0, 2) + fileName);
            }
        }
        return super.getChild(parent, fileName);
    }
    public String getSystemTypeDescription(File f) {
        if (f == null) {
            return null;
        }
        try {
            return getShellFolder(f).getFolderType();
        } catch (FileNotFoundException e) {
            return null;
        }
    }
    public File getHomeDirectory() {
        return getRoots()[0];
    }
    public File createNewFolder(File containingDir) throws IOException {
        if(containingDir == null) {
            throw new IOException("Containing directory is null:");
        }
        File newFolder = createFileObject(containingDir, newFolderString);
        int i = 2;
        while (newFolder.exists() && i < 100) {
            newFolder = createFileObject(containingDir, MessageFormat.format(
                newFolderNextString, new Integer(i)));
            i++;
        }
        if(newFolder.exists()) {
            throw new IOException("Directory already exists:" + newFolder.getAbsolutePath());
        } else {
            newFolder.mkdirs();
        }
        return newFolder;
    }
    public boolean isDrive(File dir) {
        return isFileSystemRoot(dir);
    }
    public boolean isFloppyDrive(final File dir) {
        String path = AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return dir.getAbsolutePath();
            }
        });
        return path != null && (path.equals("A:\\") || path.equals("B:\\"));
    }
    public File createFileObject(String path) {
        if (path.length() >= 2 && path.charAt(1) == ':' && Character.isLetter(path.charAt(0))) {
            if (path.length() == 2) {
                path += "\\";
            } else if (path.charAt(2) != '\\') {
                path = path.substring(0, 2) + "\\" + path.substring(2);
            }
        }
        return super.createFileObject(path);
    }
    protected File createFileSystemRoot(File f) {
        return new FileSystemRoot(f) {
            public boolean exists() {
                return true;
            }
        };
    }
}
class GenericFileSystemView extends FileSystemView {
    private static final String newFolderString =
            UIManager.getString("FileChooser.other.newFolder");
    public File createNewFolder(File containingDir) throws IOException {
        if(containingDir == null) {
            throw new IOException("Containing directory is null:");
        }
        File newFolder = createFileObject(containingDir, newFolderString);
        if(newFolder.exists()) {
            throw new IOException("Directory already exists:" + newFolder.getAbsolutePath());
        } else {
            newFolder.mkdirs();
        }
        return newFolder;
    }
}

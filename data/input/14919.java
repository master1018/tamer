public abstract class ShellFolder extends File {
    private static final String COLUMN_NAME = "FileChooser.fileNameHeaderText";
    private static final String COLUMN_SIZE = "FileChooser.fileSizeHeaderText";
    private static final String COLUMN_DATE = "FileChooser.fileDateHeaderText";
    protected ShellFolder parent;
    ShellFolder(ShellFolder parent, String pathname) {
        super((pathname != null) ? pathname : "ShellFolder");
        this.parent = parent;
    }
    public boolean isFileSystem() {
        return (!getPath().startsWith("ShellFolder"));
    }
    protected abstract Object writeReplace() throws java.io.ObjectStreamException;
    public String getParent() {
        if (parent == null && isFileSystem()) {
            return super.getParent();
        }
        if (parent != null) {
            return (parent.getPath());
        } else {
            return null;
        }
    }
    public File getParentFile() {
        if (parent != null) {
            return parent;
        } else if (isFileSystem()) {
            return super.getParentFile();
        } else {
            return null;
        }
    }
    public File[] listFiles() {
        return listFiles(true);
    }
    public File[] listFiles(boolean includeHiddenFiles) {
        File[] files = super.listFiles();
        if (!includeHiddenFiles) {
            Vector v = new Vector();
            int nameCount = (files == null) ? 0 : files.length;
            for (int i = 0; i < nameCount; i++) {
                if (!files[i].isHidden()) {
                    v.addElement(files[i]);
                }
            }
            files = (File[])v.toArray(new File[v.size()]);
        }
        return files;
    }
    public abstract boolean isLink();
    public abstract ShellFolder getLinkLocation() throws FileNotFoundException;
    public abstract String getDisplayName();
    public abstract String getFolderType();
    public abstract String getExecutableType();
    public int compareTo(File file2) {
        if (file2 == null || !(file2 instanceof ShellFolder)
            || ((file2 instanceof ShellFolder) && ((ShellFolder)file2).isFileSystem())) {
            if (isFileSystem()) {
                return super.compareTo(file2);
            } else {
                return -1;
            }
        } else {
            if (isFileSystem()) {
                return 1;
            } else {
                return getName().compareTo(file2.getName());
            }
        }
    }
    public Image getIcon(boolean getLargeIcon) {
        return null;
    }
    private static ShellFolderManager shellFolderManager;
    private static Invoker invoker;
    static {
        String managerClassName = (String)Toolkit.getDefaultToolkit().
                                      getDesktopProperty("Shell.shellFolderManager");
        Class managerClass = null;
        try {
            managerClass = Class.forName(managerClassName);
        } catch(ClassNotFoundException e) {
        } catch(NullPointerException e) {
        }
        if (managerClass == null) {
            managerClass = ShellFolderManager.class;
        }
        try {
            shellFolderManager =
                (ShellFolderManager)managerClass.newInstance();
        } catch (InstantiationException e) {
            throw new Error("Could not instantiate Shell Folder Manager: "
            + managerClass.getName());
        } catch (IllegalAccessException e) {
            throw new Error ("Could not access Shell Folder Manager: "
            + managerClass.getName());
        }
        invoker = shellFolderManager.createInvoker();
    }
    public static ShellFolder getShellFolder(File file) throws FileNotFoundException {
        if (file instanceof ShellFolder) {
            return (ShellFolder)file;
        }
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        return shellFolderManager.createShellFolder(file);
    }
    public static Object get(String key) {
        return shellFolderManager.get(key);
    }
    public static boolean isComputerNode(File dir) {
        return shellFolderManager.isComputerNode(dir);
    }
    public static boolean isFileSystemRoot(File dir) {
        return shellFolderManager.isFileSystemRoot(dir);
    }
    public static File getNormalizedFile(File f) throws IOException {
        File canonical = f.getCanonicalFile();
        if (f.equals(canonical)) {
            return canonical;
        }
        return new File(f.toURI().normalize());
    }
    public static void sort(final List<? extends File> files) {
        if (files == null || files.size() <= 1) {
            return;
        }
        invoke(new Callable<Void>() {
            public Void call() {
                File commonParent = null;
                for (File file : files) {
                    File parent = file.getParentFile();
                    if (parent == null || !(file instanceof ShellFolder)) {
                        commonParent = null;
                        break;
                    }
                    if (commonParent == null) {
                        commonParent = parent;
                    } else {
                        if (commonParent != parent && !commonParent.equals(parent)) {
                            commonParent = null;
                            break;
                        }
                    }
                }
                if (commonParent instanceof ShellFolder) {
                    ((ShellFolder) commonParent).sortChildren(files);
                } else {
                    Collections.sort(files, FILE_COMPARATOR);
                }
                return null;
            }
        });
    }
    public void sortChildren(final List<? extends File> files) {
        invoke(new Callable<Void>() {
            public Void call() {
                Collections.sort(files, FILE_COMPARATOR);
                return null;
            }
        });
    }
    public boolean isAbsolute() {
        return (!isFileSystem() || super.isAbsolute());
    }
    public File getAbsoluteFile() {
        return (isFileSystem() ? super.getAbsoluteFile() : this);
    }
    public boolean canRead() {
        return (isFileSystem() ? super.canRead() : true);       
    }
    public boolean canWrite() {
        return (isFileSystem() ? super.canWrite() : false);     
    }
    public boolean exists() {
        return (!isFileSystem() || isFileSystemRoot(this) || super.exists()) ;
    }
    public boolean isDirectory() {
        return (isFileSystem() ? super.isDirectory() : true);   
    }
    public boolean isFile() {
        return (isFileSystem() ? super.isFile() : !isDirectory());      
    }
    public long lastModified() {
        return (isFileSystem() ? super.lastModified() : 0L);    
    }
    public long length() {
        return (isFileSystem() ? super.length() : 0L);  
    }
    public boolean createNewFile() throws IOException {
        return (isFileSystem() ? super.createNewFile() : false);
    }
    public boolean delete() {
        return (isFileSystem() ? super.delete() : false);       
    }
    public void deleteOnExit() {
        if (isFileSystem()) {
            super.deleteOnExit();
        } else {
        }
    }
    public boolean mkdir() {
        return (isFileSystem() ? super.mkdir() : false);
    }
    public boolean mkdirs() {
        return (isFileSystem() ? super.mkdirs() : false);
    }
    public boolean renameTo(File dest) {
        return (isFileSystem() ? super.renameTo(dest) : false); 
    }
    public boolean setLastModified(long time) {
        return (isFileSystem() ? super.setLastModified(time) : false); 
    }
    public boolean setReadOnly() {
        return (isFileSystem() ? super.setReadOnly() : false); 
    }
    public String toString() {
        return (isFileSystem() ? super.toString() : getDisplayName());
    }
    public static ShellFolderColumnInfo[] getFolderColumns(File dir) {
        ShellFolderColumnInfo[] columns = null;
        if (dir instanceof ShellFolder) {
            columns = ((ShellFolder) dir).getFolderColumns();
        }
        if (columns == null) {
            columns = new ShellFolderColumnInfo[]{
                    new ShellFolderColumnInfo(COLUMN_NAME, 150,
                            SwingConstants.LEADING, true, null,
                            FILE_COMPARATOR),
                    new ShellFolderColumnInfo(COLUMN_SIZE, 75,
                            SwingConstants.RIGHT, true, null,
                            DEFAULT_COMPARATOR, true),
                    new ShellFolderColumnInfo(COLUMN_DATE, 130,
                            SwingConstants.LEADING, true, null,
                            DEFAULT_COMPARATOR, true)
            };
        }
        return columns;
    }
    public ShellFolderColumnInfo[] getFolderColumns() {
        return null;
    }
    public static Object getFolderColumnValue(File file, int column) {
        if (file instanceof ShellFolder) {
            Object value = ((ShellFolder)file).getFolderColumnValue(column);
            if (value != null) {
                return value;
            }
        }
        if (file == null || !file.exists()) {
            return null;
        }
        switch (column) {
            case 0:
                return file;
            case 1: 
                return file.isDirectory() ? null : Long.valueOf(file.length());
            case 2: 
                if (isFileSystemRoot(file)) {
                    return null;
                }
                long time = file.lastModified();
                return (time == 0L) ? null : new Date(time);
            default:
                return null;
        }
    }
    public Object getFolderColumnValue(int column) {
        return null;
    }
    public static <T> T invoke(Callable<T> task) {
        try {
            return invoke(task, RuntimeException.class);
        } catch (InterruptedException e) {
            return null;
        }
    }
    public static <T, E extends Throwable> T invoke(Callable<T> task, Class<E> exceptionClass)
            throws InterruptedException, E {
        try {
            return invoker.invoke(task);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                throw (InterruptedException) e;
            }
            if (exceptionClass.isInstance(e)) {
                throw exceptionClass.cast(e);
            }
            throw new RuntimeException("Unexpected error", e);
        }
    }
    public static interface Invoker {
        <T> T invoke(Callable<T> task) throws Exception;
    }
    private static final Comparator DEFAULT_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            int gt;
            if (o1 == null && o2 == null) {
                gt = 0;
            } else if (o1 != null && o2 == null) {
                gt = 1;
            } else if (o1 == null && o2 != null) {
                gt = -1;
            } else if (o1 instanceof Comparable) {
                gt = ((Comparable) o1).compareTo(o2);
            } else {
                gt = 0;
            }
            return gt;
        }
    };
    private static final Comparator<File> FILE_COMPARATOR = new Comparator<File>() {
        public int compare(File f1, File f2) {
            ShellFolder sf1 = null;
            ShellFolder sf2 = null;
            if (f1 instanceof ShellFolder) {
                sf1 = (ShellFolder) f1;
                if (sf1.isFileSystem()) {
                    sf1 = null;
                }
            }
            if (f2 instanceof ShellFolder) {
                sf2 = (ShellFolder) f2;
                if (sf2.isFileSystem()) {
                    sf2 = null;
                }
            }
            if (sf1 != null && sf2 != null) {
                return sf1.compareTo(sf2);
            } else if (sf1 != null) {
                return -1;
            } else if (sf2 != null) {
                return 1;
            } else {
                String name1 = f1.getName();
                String name2 = f2.getName();
                int diff = name1.compareToIgnoreCase(name2);
                if (diff != 0) {
                    return diff;
                } else {
                    return name1.compareTo(name2);
                }
            }
        }
    };
}

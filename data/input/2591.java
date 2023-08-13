final class Win32ShellFolder2 extends ShellFolder {
    private static native void initIDs();
    static {
        initIDs();
    }
    public static final int DESKTOP = 0x0000;
    public static final int INTERNET = 0x0001;
    public static final int PROGRAMS = 0x0002;
    public static final int CONTROLS = 0x0003;
    public static final int PRINTERS = 0x0004;
    public static final int PERSONAL = 0x0005;
    public static final int FAVORITES = 0x0006;
    public static final int STARTUP = 0x0007;
    public static final int RECENT = 0x0008;
    public static final int SENDTO = 0x0009;
    public static final int BITBUCKET = 0x000a;
    public static final int STARTMENU = 0x000b;
    public static final int DESKTOPDIRECTORY = 0x0010;
    public static final int DRIVES = 0x0011;
    public static final int NETWORK = 0x0012;
    public static final int NETHOOD = 0x0013;
    public static final int FONTS = 0x0014;
    public static final int TEMPLATES = 0x0015;
    public static final int COMMON_STARTMENU = 0x0016;
    public static final int COMMON_PROGRAMS = 0X0017;
    public static final int COMMON_STARTUP = 0x0018;
    public static final int COMMON_DESKTOPDIRECTORY = 0x0019;
    public static final int APPDATA = 0x001a;
    public static final int PRINTHOOD = 0x001b;
    public static final int ALTSTARTUP = 0x001d;
    public static final int COMMON_ALTSTARTUP = 0x001e;
    public static final int COMMON_FAVORITES = 0x001f;
    public static final int INTERNET_CACHE = 0x0020;
    public static final int COOKIES = 0x0021;
    public static final int HISTORY = 0x0022;
    public static final int ATTRIB_CANCOPY          = 0x00000001;
    public static final int ATTRIB_CANMOVE          = 0x00000002;
    public static final int ATTRIB_CANLINK          = 0x00000004;
    public static final int ATTRIB_CANRENAME        = 0x00000010;
    public static final int ATTRIB_CANDELETE        = 0x00000020;
    public static final int ATTRIB_HASPROPSHEET     = 0x00000040;
    public static final int ATTRIB_DROPTARGET       = 0x00000100;
    public static final int ATTRIB_LINK             = 0x00010000;
    public static final int ATTRIB_SHARE            = 0x00020000;
    public static final int ATTRIB_READONLY         = 0x00040000;
    public static final int ATTRIB_GHOSTED          = 0x00080000;
    public static final int ATTRIB_HIDDEN           = 0x00080000;
    public static final int ATTRIB_FILESYSANCESTOR  = 0x10000000;
    public static final int ATTRIB_FOLDER           = 0x20000000;
    public static final int ATTRIB_FILESYSTEM       = 0x40000000;
    public static final int ATTRIB_HASSUBFOLDER     = 0x80000000;
    public static final int ATTRIB_VALIDATE         = 0x01000000;
    public static final int ATTRIB_REMOVABLE        = 0x02000000;
    public static final int ATTRIB_COMPRESSED       = 0x04000000;
    public static final int ATTRIB_BROWSABLE        = 0x08000000;
    public static final int ATTRIB_NONENUMERATED    = 0x00100000;
    public static final int ATTRIB_NEWCONTENT       = 0x00200000;
    public static final int SHGDN_NORMAL            = 0;
    public static final int SHGDN_INFOLDER          = 1;
    public static final int SHGDN_INCLUDE_NONFILESYS= 0x2000;
    public static final int SHGDN_FORADDRESSBAR     = 0x4000;
    public static final int SHGDN_FORPARSING        = 0x8000;
    public enum SystemIcon {
        IDI_APPLICATION(32512),
        IDI_HAND(32513),
        IDI_ERROR(32513),
        IDI_QUESTION(32514),
        IDI_EXCLAMATION(32515),
        IDI_WARNING(32515),
        IDI_ASTERISK(32516),
        IDI_INFORMATION(32516),
        IDI_WINLOGO(32517);
        private final int iconID;
        SystemIcon(int iconID) {
            this.iconID = iconID;
        }
        public int getIconID() {
            return iconID;
        }
    }
    static class FolderDisposer implements sun.java2d.DisposerRecord {
        long absolutePIDL;
        long pIShellFolder;
        long relativePIDL;
        boolean disposed;
        public void dispose() {
            if (disposed) return;
            invoke(new Callable<Void>() {
                public Void call() {
                    if (relativePIDL != 0) {
                        releasePIDL(relativePIDL);
                    }
                    if (absolutePIDL != 0) {
                        releasePIDL(absolutePIDL);
                    }
                    if (pIShellFolder != 0) {
                        releaseIShellFolder(pIShellFolder);
                    }
                    return null;
                }
            });
            disposed = true;
        }
    }
    FolderDisposer disposer = new FolderDisposer();
    private void setIShellFolder(long pIShellFolder) {
        disposer.pIShellFolder = pIShellFolder;
    }
    private void setRelativePIDL(long relativePIDL) {
        disposer.relativePIDL = relativePIDL;
    }
    private long pIShellIcon = -1L;
    private String folderType = null;
    private String displayName = null;
    private Image smallIcon = null;
    private Image largeIcon = null;
    private Boolean isDir = null;
    private boolean isPersonal;
    private static String composePathForCsidl(int csidl) throws IOException, InterruptedException {
        String path = getFileSystemPath(csidl);
        return path == null
                ? ("ShellFolder: 0x" + Integer.toHexString(csidl))
                : path;
    }
    Win32ShellFolder2(final int csidl) throws IOException, InterruptedException {
        super(null, composePathForCsidl(csidl));
        invoke(new Callable<Void>() {
            public Void call() throws InterruptedException {
                if (csidl == DESKTOP) {
                    initDesktop();
                } else {
                    initSpecial(getDesktop().getIShellFolder(), csidl);
                    long pIDL = disposer.relativePIDL;
                    parent = getDesktop();
                    while (pIDL != 0) {
                        long childPIDL = copyFirstPIDLEntry(pIDL);
                        if (childPIDL != 0) {
                            pIDL = getNextPIDLEntry(pIDL);
                            if (pIDL != 0) {
                                parent = new Win32ShellFolder2((Win32ShellFolder2) parent, childPIDL);
                            } else {
                                disposer.relativePIDL = childPIDL;
                            }
                        } else {
                            break;
                        }
                    }
                }
                return null;
            }
        }, InterruptedException.class);
        sun.java2d.Disposer.addRecord(this, disposer);
    }
    Win32ShellFolder2(Win32ShellFolder2 parent, long pIShellFolder, long relativePIDL, String path) {
        super(parent, (path != null) ? path : "ShellFolder: ");
        this.disposer.pIShellFolder = pIShellFolder;
        this.disposer.relativePIDL = relativePIDL;
        sun.java2d.Disposer.addRecord(this, disposer);
    }
    Win32ShellFolder2(final Win32ShellFolder2 parent, final long relativePIDL) throws InterruptedException {
        super(parent,
            invoke(new Callable<String>() {
                public String call() {
                    return getFileSystemPath(parent.getIShellFolder(), relativePIDL);
                }
            }, RuntimeException.class)
        );
        this.disposer.relativePIDL = relativePIDL;
        sun.java2d.Disposer.addRecord(this, disposer);
    }
    private native void initDesktop();
    private native void initSpecial(long desktopIShellFolder, int csidl);
    public void setIsPersonal() {
        isPersonal = true;
    }
    protected Object writeReplace() throws java.io.ObjectStreamException {
        return invoke(new Callable<File>() {
            public File call() {
                if (isFileSystem()) {
                    return new File(getPath());
                } else {
                    Win32ShellFolder2 drives = Win32ShellFolderManager2.getDrives();
                    if (drives != null) {
                        File[] driveRoots = drives.listFiles();
                        if (driveRoots != null) {
                            for (int i = 0; i < driveRoots.length; i++) {
                                if (driveRoots[i] instanceof Win32ShellFolder2) {
                                    Win32ShellFolder2 sf = (Win32ShellFolder2) driveRoots[i];
                                    if (sf.isFileSystem() && !sf.hasAttribute(ATTRIB_REMOVABLE)) {
                                        return new File(sf.getPath());
                                    }
                                }
                            }
                        }
                    }
                    return new File("C:\\");
                }
            }
        });
    }
    protected void dispose() {
        disposer.dispose();
    }
    static native long getNextPIDLEntry(long pIDL);
    static native long copyFirstPIDLEntry(long pIDL);
    private static native long combinePIDLs(long ppIDL, long pIDL);
    static native void releasePIDL(long pIDL);
    private static native void releaseIShellFolder(long pIShellFolder);
    private long getIShellFolder() {
        if (disposer.pIShellFolder == 0) {
            try {
                disposer.pIShellFolder = invoke(new Callable<Long>() {
                    public Long call() {
                        assert(isDirectory());
                        assert(parent != null);
                        long parentIShellFolder = getParentIShellFolder();
                        if (parentIShellFolder == 0) {
                            throw new InternalError("Parent IShellFolder was null for "
                                    + getAbsolutePath());
                        }
                        long pIShellFolder = bindToObject(parentIShellFolder,
                                disposer.relativePIDL);
                        if (pIShellFolder == 0) {
                            throw new InternalError("Unable to bind "
                                    + getAbsolutePath() + " to parent");
                        }
                        return pIShellFolder;
                    }
                }, RuntimeException.class);
            } catch (InterruptedException e) {
            }
        }
        return disposer.pIShellFolder;
    }
    public long getParentIShellFolder() {
        Win32ShellFolder2 parent = (Win32ShellFolder2)getParentFile();
        if (parent == null) {
            return getIShellFolder();
        }
        return parent.getIShellFolder();
    }
    public long getRelativePIDL() {
        if (disposer.relativePIDL == 0) {
            throw new InternalError("Should always have a relative PIDL");
        }
        return disposer.relativePIDL;
    }
    private long getAbsolutePIDL() {
        if (parent == null) {
            return getRelativePIDL();
        } else {
            if (disposer.absolutePIDL == 0) {
                disposer.absolutePIDL = combinePIDLs(((Win32ShellFolder2)parent).getAbsolutePIDL(), getRelativePIDL());
            }
            return disposer.absolutePIDL;
        }
    }
    public Win32ShellFolder2 getDesktop() {
        return Win32ShellFolderManager2.getDesktop();
    }
    public long getDesktopIShellFolder() {
        return getDesktop().getIShellFolder();
    }
    private static boolean pathsEqual(String path1, String path2) {
        return path1.equalsIgnoreCase(path2);
    }
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Win32ShellFolder2)) {
            if (!(o instanceof File)) {
                return super.equals(o);
            }
            return pathsEqual(getPath(), ((File) o).getPath());
        }
        Win32ShellFolder2 rhs = (Win32ShellFolder2) o;
        if ((parent == null && rhs.parent != null) ||
            (parent != null && rhs.parent == null)) {
            return false;
        }
        if (isFileSystem() && rhs.isFileSystem()) {
            return (pathsEqual(getPath(), rhs.getPath()) &&
                    (parent == rhs.parent || parent.equals(rhs.parent)));
        }
        if (parent == rhs.parent || parent.equals(rhs.parent)) {
            try {
                return pidlsEqual(getParentIShellFolder(), disposer.relativePIDL, rhs.disposer.relativePIDL);
            } catch (InterruptedException e) {
                return false;
            }
        }
        return false;
    }
    private static boolean pidlsEqual(final long pIShellFolder, final long pidl1, final long pidl2)
            throws InterruptedException {
        return invoke(new Callable<Boolean>() {
            public Boolean call() {
                return compareIDs(pIShellFolder, pidl1, pidl2) == 0;
            }
        }, RuntimeException.class);
    }
    private static native int compareIDs(long pParentIShellFolder, long pidl1, long pidl2);
    private volatile Boolean cachedIsFileSystem;
    public boolean isFileSystem() {
        if (cachedIsFileSystem == null) {
            cachedIsFileSystem = hasAttribute(ATTRIB_FILESYSTEM);
        }
        return cachedIsFileSystem;
    }
    public boolean hasAttribute(final int attribute) {
        Boolean result = invoke(new Callable<Boolean>() {
            public Boolean call() {
                return (getAttributes0(getParentIShellFolder(),
                    getRelativePIDL(), attribute)
                    & attribute) != 0;
            }
        });
        return result != null && result;
    }
    private static native int getAttributes0(long pParentIShellFolder, long pIDL, int attrsMask);
    private static String getFileSystemPath(final long parentIShellFolder, final long relativePIDL) {
        int linkedFolder = ATTRIB_LINK | ATTRIB_FOLDER;
        if (parentIShellFolder == Win32ShellFolderManager2.getNetwork().getIShellFolder() &&
                getAttributes0(parentIShellFolder, relativePIDL, linkedFolder) == linkedFolder) {
            String s =
                    getFileSystemPath(Win32ShellFolderManager2.getDesktop().getIShellFolder(),
                            getLinkLocation(parentIShellFolder, relativePIDL, false));
            if (s != null && s.startsWith("\\\\")) {
                return s;
            }
        }
        return getDisplayNameOf(parentIShellFolder, relativePIDL, SHGDN_FORPARSING);
    }
    static String getFileSystemPath(final int csidl) throws IOException, InterruptedException {
        return invoke(new Callable<String>() {
            public String call() throws IOException {
                return getFileSystemPath0(csidl);
            }
        }, IOException.class);
    }
    private static native String getFileSystemPath0(int csidl) throws IOException;
    private static boolean isNetworkRoot(String path) {
        return (path.equals("\\\\") || path.equals("\\") || path.equals("
    }
    public File getParentFile() {
        return parent;
    }
    public boolean isDirectory() {
        if (isDir == null) {
            if (hasAttribute(ATTRIB_FOLDER) && !hasAttribute(ATTRIB_BROWSABLE)) {
                isDir = Boolean.TRUE;
            } else if (isLink()) {
                ShellFolder linkLocation = getLinkLocation(false);
                isDir = Boolean.valueOf(linkLocation != null && linkLocation.isDirectory());
            } else {
                isDir = Boolean.FALSE;
            }
        }
        return isDir.booleanValue();
    }
    private long getEnumObjects(final boolean includeHiddenFiles) throws InterruptedException {
        return invoke(new Callable<Long>() {
            public Long call() {
                boolean isDesktop = disposer.pIShellFolder == getDesktopIShellFolder();
                return getEnumObjects(disposer.pIShellFolder, isDesktop, includeHiddenFiles);
            }
        }, RuntimeException.class);
    }
    private native long getEnumObjects(long pIShellFolder, boolean isDesktop,
                                       boolean includeHiddenFiles);
    private native long getNextChild(long pEnumObjects);
    private native void releaseEnumObjects(long pEnumObjects);
    private static native long bindToObject(long parentIShellFolder, long pIDL);
    public File[] listFiles(final boolean includeHiddenFiles) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(getPath());
        }
        try {
            return invoke(new Callable<File[]>() {
                public File[] call() throws InterruptedException {
                    if (!isDirectory()) {
                        return null;
                    }
                    if (isLink() && !hasAttribute(ATTRIB_FOLDER)) {
                        return new File[0];
                    }
                    Win32ShellFolder2 desktop = Win32ShellFolderManager2.getDesktop();
                    Win32ShellFolder2 personal = Win32ShellFolderManager2.getPersonal();
                    long pIShellFolder = getIShellFolder();
                    ArrayList<Win32ShellFolder2> list = new ArrayList<Win32ShellFolder2>();
                    long pEnumObjects = getEnumObjects(includeHiddenFiles);
                    if (pEnumObjects != 0) {
                        try {
                            long childPIDL;
                            int testedAttrs = ATTRIB_FILESYSTEM | ATTRIB_FILESYSANCESTOR;
                            do {
                                childPIDL = getNextChild(pEnumObjects);
                                boolean releasePIDL = true;
                                if (childPIDL != 0 &&
                                        (getAttributes0(pIShellFolder, childPIDL, testedAttrs) & testedAttrs) != 0) {
                                    Win32ShellFolder2 childFolder;
                                    if (Win32ShellFolder2.this.equals(desktop)
                                            && personal != null
                                            && pidlsEqual(pIShellFolder, childPIDL, personal.disposer.relativePIDL)) {
                                        childFolder = personal;
                                    } else {
                                        childFolder = new Win32ShellFolder2(Win32ShellFolder2.this, childPIDL);
                                        releasePIDL = false;
                                    }
                                    list.add(childFolder);
                                }
                                if (releasePIDL) {
                                    releasePIDL(childPIDL);
                                }
                            } while (childPIDL != 0 && !Thread.currentThread().isInterrupted());
                        } finally {
                            releaseEnumObjects(pEnumObjects);
                        }
                    }
                    return Thread.currentThread().isInterrupted()
                        ? new File[0]
                        : list.toArray(new ShellFolder[list.size()]);
                }
            }, InterruptedException.class);
        } catch (InterruptedException e) {
            return new File[0];
        }
    }
    Win32ShellFolder2 getChildByPath(final String filePath) throws InterruptedException {
        return invoke(new Callable<Win32ShellFolder2>() {
            public Win32ShellFolder2 call() throws InterruptedException {
                long pIShellFolder = getIShellFolder();
                long pEnumObjects = getEnumObjects(true);
                Win32ShellFolder2 child = null;
                long childPIDL;
                while ((childPIDL = getNextChild(pEnumObjects)) != 0) {
                    if (getAttributes0(pIShellFolder, childPIDL, ATTRIB_FILESYSTEM) != 0) {
                        String path = getFileSystemPath(pIShellFolder, childPIDL);
                        if (path != null && path.equalsIgnoreCase(filePath)) {
                            long childIShellFolder = bindToObject(pIShellFolder, childPIDL);
                            child = new Win32ShellFolder2(Win32ShellFolder2.this,
                                    childIShellFolder, childPIDL, path);
                            break;
                        }
                    }
                    releasePIDL(childPIDL);
                }
                releaseEnumObjects(pEnumObjects);
                return child;
            }
        }, InterruptedException.class);
    }
    private volatile Boolean cachedIsLink;
    public boolean isLink() {
        if (cachedIsLink == null) {
            cachedIsLink = hasAttribute(ATTRIB_LINK);
        }
        return cachedIsLink;
    }
    public boolean isHidden() {
        return hasAttribute(ATTRIB_HIDDEN);
    }
    private static native long getLinkLocation(long parentIShellFolder,
                                        long relativePIDL, boolean resolve);
    public ShellFolder getLinkLocation()  {
        return getLinkLocation(true);
    }
    private ShellFolder getLinkLocation(final boolean resolve) {
        return invoke(new Callable<ShellFolder>() {
            public ShellFolder call() {
                if (!isLink()) {
                    return null;
                }
                ShellFolder location = null;
                long linkLocationPIDL = getLinkLocation(getParentIShellFolder(),
                        getRelativePIDL(), resolve);
                if (linkLocationPIDL != 0) {
                    try {
                        location =
                                Win32ShellFolderManager2.createShellFolderFromRelativePIDL(getDesktop(),
                                        linkLocationPIDL);
                    } catch (InterruptedException e) {
                    } catch (InternalError e) {
                    }
                }
                return location;
            }
        });
    }
    long parseDisplayName(final String name) throws IOException, InterruptedException {
        return invoke(new Callable<Long>() {
            public Long call() throws IOException {
                return parseDisplayName0(getIShellFolder(), name);
            }
        }, IOException.class);
    }
    private static native long parseDisplayName0(long pIShellFolder, String name) throws IOException;
    private static native String getDisplayNameOf(long parentIShellFolder,
                                                  long relativePIDL,
                                                  int attrs);
    public String getDisplayName() {
        if (displayName == null) {
            displayName =
                invoke(new Callable<String>() {
                    public String call() {
                        return getDisplayNameOf(getParentIShellFolder(),
                                getRelativePIDL(), SHGDN_NORMAL);
                    }
                });
        }
        return displayName;
    }
    private static native String getFolderType(long pIDL);
    public String getFolderType() {
        if (folderType == null) {
            final long absolutePIDL = getAbsolutePIDL();
            folderType =
                invoke(new Callable<String>() {
                    public String call() {
                        return getFolderType(absolutePIDL);
                    }
                });
        }
        return folderType;
    }
    private native String getExecutableType(String path);
    public String getExecutableType() {
        if (!isFileSystem()) {
            return null;
        }
        return getExecutableType(getAbsolutePath());
    }
    private static Map smallSystemImages = new HashMap();
    private static Map largeSystemImages = new HashMap();
    private static Map smallLinkedSystemImages = new HashMap();
    private static Map largeLinkedSystemImages = new HashMap();
    private static native long getIShellIcon(long pIShellFolder);
    private static native int getIconIndex(long parentIShellIcon, long relativePIDL);
    private static native long getIcon(String absolutePath, boolean getLargeIcon);
    private static native long extractIcon(long parentIShellFolder, long relativePIDL,
                                           boolean getLargeIcon);
    private static native long getSystemIcon(int iconID);
    private static native long getIconResource(String libName, int iconID,
                                               int cxDesired, int cyDesired,
                                               boolean useVGAColors);
    private static native int[] getIconBits(long hIcon, int iconSize);
    private static native void disposeIcon(long hIcon);
    static native int[] getStandardViewButton0(int iconIndex);
    private long getIShellIcon() {
        if (pIShellIcon == -1L) {
            pIShellIcon = getIShellIcon(getIShellFolder());
        }
        return pIShellIcon;
    }
    private static Image makeIcon(long hIcon, boolean getLargeIcon) {
        if (hIcon != 0L && hIcon != -1L) {
            int size = getLargeIcon ? 32 : 16;
            int[] iconBits = getIconBits(hIcon, size);
            if (iconBits != null) {
                BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                img.setRGB(0, 0, size, size, iconBits, 0, size);
                return img;
            }
        }
        return null;
    }
    public Image getIcon(final boolean getLargeIcon) {
        Image icon = getLargeIcon ? largeIcon : smallIcon;
        if (icon == null) {
            icon =
                invoke(new Callable<Image>() {
                    public Image call() {
                        Image newIcon = null;
                        if (isFileSystem()) {
                            long parentIShellIcon = (parent != null)
                                ? ((Win32ShellFolder2) parent).getIShellIcon()
                                : 0L;
                            long relativePIDL = getRelativePIDL();
                            int index = getIconIndex(parentIShellIcon, relativePIDL);
                            if (index > 0) {
                                Map imageCache;
                                if (isLink()) {
                                    imageCache = getLargeIcon ? largeLinkedSystemImages : smallLinkedSystemImages;
                                } else {
                                    imageCache = getLargeIcon ? largeSystemImages : smallSystemImages;
                                }
                                newIcon = (Image) imageCache.get(Integer.valueOf(index));
                                if (newIcon == null) {
                                    long hIcon = getIcon(getAbsolutePath(), getLargeIcon);
                                    newIcon = makeIcon(hIcon, getLargeIcon);
                                    disposeIcon(hIcon);
                                    if (newIcon != null) {
                                        imageCache.put(Integer.valueOf(index), newIcon);
                                    }
                                }
                            }
                        }
                        if (newIcon == null) {
                            long hIcon = extractIcon(getParentIShellFolder(),
                                getRelativePIDL(), getLargeIcon);
                            newIcon = makeIcon(hIcon, getLargeIcon);
                            disposeIcon(hIcon);
                        }
                        if (newIcon == null) {
                            newIcon = Win32ShellFolder2.super.getIcon(getLargeIcon);
                        }
                        return newIcon;
                    }
                });
            if (getLargeIcon) {
                largeIcon = icon;
            } else {
                smallIcon = icon;
            }
        }
        return icon;
    }
    static Image getSystemIcon(SystemIcon iconType) {
        long hIcon = getSystemIcon(iconType.getIconID());
        Image icon = makeIcon(hIcon, true);
        disposeIcon(hIcon);
        return icon;
    }
    static Image getShell32Icon(int iconID, boolean getLargeIcon) {
        boolean useVGAColors = true; 
        int size = getLargeIcon ? 32 : 16;
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        String shellIconBPP = (String)toolkit.getDesktopProperty("win.icon.shellIconBPP");
        if (shellIconBPP != null) {
            useVGAColors = shellIconBPP.equals("4");
        }
        long hIcon = getIconResource("shell32.dll", iconID, size, size, useVGAColors);
        if (hIcon != 0) {
            Image icon = makeIcon(hIcon, getLargeIcon);
            disposeIcon(hIcon);
            return icon;
        }
        return null;
    }
    public File getCanonicalFile() throws IOException {
        return this;
    }
    public boolean isSpecial() {
        return isPersonal || !isFileSystem() || (this == getDesktop());
    }
    public int compareTo(File file2) {
        if (!(file2 instanceof Win32ShellFolder2)) {
            if (isFileSystem() && !isSpecial()) {
                return super.compareTo(file2);
            } else {
                return -1; 
            }
        }
        return Win32ShellFolderManager2.compareShellFolders(this, (Win32ShellFolder2) file2);
    }
    private static final int LVCFMT_LEFT = 0;
    private static final int LVCFMT_RIGHT = 1;
    private static final int LVCFMT_CENTER = 2;
    public ShellFolderColumnInfo[] getFolderColumns() {
        return invoke(new Callable<ShellFolderColumnInfo[]>() {
            public ShellFolderColumnInfo[] call() {
                ShellFolderColumnInfo[] columns = doGetColumnInfo(getIShellFolder());
                if (columns != null) {
                    List<ShellFolderColumnInfo> notNullColumns =
                            new ArrayList<ShellFolderColumnInfo>();
                    for (int i = 0; i < columns.length; i++) {
                        ShellFolderColumnInfo column = columns[i];
                        if (column != null) {
                            column.setAlignment(column.getAlignment() == LVCFMT_RIGHT
                                    ? SwingConstants.RIGHT
                                    : column.getAlignment() == LVCFMT_CENTER
                                    ? SwingConstants.CENTER
                                    : SwingConstants.LEADING);
                            column.setComparator(new ColumnComparator(getIShellFolder(), i));
                            notNullColumns.add(column);
                        }
                    }
                    columns = new ShellFolderColumnInfo[notNullColumns.size()];
                    notNullColumns.toArray(columns);
                }
                return columns;
            }
        });
    }
    public Object getFolderColumnValue(final int column) {
        return invoke(new Callable<Object>() {
            public Object call() {
                return doGetColumnValue(getParentIShellFolder(), getRelativePIDL(), column);
            }
        });
    }
    private native ShellFolderColumnInfo[] doGetColumnInfo(long iShellFolder2);
    private native Object doGetColumnValue(long parentIShellFolder2, long childPIDL, int columnIdx);
    private static native int compareIDsByColumn(long pParentIShellFolder, long pidl1, long pidl2, int columnIdx);
    public void sortChildren(final List<? extends File> files) {
        invoke(new Callable<Void>() {
            public Void call() {
                Collections.sort(files, new ColumnComparator(getIShellFolder(), 0));
                return null;
            }
        });
    }
    private static class ColumnComparator implements Comparator<File> {
        private final long parentIShellFolder;
        private final int columnIdx;
        public ColumnComparator(long parentIShellFolder, int columnIdx) {
            this.parentIShellFolder = parentIShellFolder;
            this.columnIdx = columnIdx;
        }
        public int compare(final File o, final File o1) {
            Integer result = invoke(new Callable<Integer>() {
                public Integer call() {
                    if (o instanceof Win32ShellFolder2
                        && o1 instanceof Win32ShellFolder2) {
                        return compareIDsByColumn(parentIShellFolder,
                            ((Win32ShellFolder2) o).getRelativePIDL(),
                            ((Win32ShellFolder2) o1).getRelativePIDL(),
                            columnIdx);
                    }
                    return 0;
                }
            });
            return result == null ? 0 : result;
        }
    }
}

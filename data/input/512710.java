public final class FileListingService {
    private final static Pattern sApkPattern =
        Pattern.compile(".*\\.apk", Pattern.CASE_INSENSITIVE); 
    private final static String PM_FULL_LISTING = "pm list packages -f"; 
    private final static Pattern sPmPattern = Pattern.compile("^package:(.+?)=(.+)$"); 
    public final static String DIRECTORY_DATA = "data"; 
    public final static String DIRECTORY_SDCARD = "sdcard"; 
    public final static String DIRECTORY_MNT = "mnt"; 
    public final static String DIRECTORY_SYSTEM = "system"; 
    public final static String DIRECTORY_TEMP = "tmp"; 
    public final static String DIRECTORY_APP = "app"; 
    private final static String[] sRootLevelApprovedItems = {
        DIRECTORY_DATA,
        DIRECTORY_SDCARD,
        DIRECTORY_SYSTEM,
        DIRECTORY_TEMP,
        DIRECTORY_MNT,
    };
    public static final long REFRESH_RATE = 5000L;
    static final long REFRESH_TEST = (long)(REFRESH_RATE * .8);
    public static final int TYPE_FILE = 0;
    public static final int TYPE_DIRECTORY = 1;
    public static final int TYPE_DIRECTORY_LINK = 2;
    public static final int TYPE_BLOCK = 3;
    public static final int TYPE_CHARACTER = 4;
    public static final int TYPE_LINK = 5;
    public static final int TYPE_SOCKET = 6;
    public static final int TYPE_FIFO = 7;
    public static final int TYPE_OTHER = 8;
    public static final String FILE_SEPARATOR = "/"; 
    private static final String FILE_ROOT = "/"; 
    private static Pattern sLsPattern = Pattern.compile(
        "^([bcdlsp-][-r][-w][-xsS][-r][-w][-xsS][-r][-w][-xstST])\\s+(\\S+)\\s+(\\S+)\\s+([\\d\\s,]*)\\s+(\\d{4}-\\d\\d-\\d\\d)\\s+(\\d\\d:\\d\\d)\\s+(.*)$"); 
    private Device mDevice;
    private FileEntry mRoot;
    private ArrayList<Thread> mThreadList = new ArrayList<Thread>();
    public final static class FileEntry {
        private final static Pattern sEscapePattern = Pattern.compile(
                "([\\\\()*+?\"'#/\\s])"); 
        private static Comparator<FileEntry> sEntryComparator = new Comparator<FileEntry>() {
            public int compare(FileEntry o1, FileEntry o2) {
                if (o1 instanceof FileEntry && o2 instanceof FileEntry) {
                    FileEntry fe1 = (FileEntry)o1;
                    FileEntry fe2 = (FileEntry)o2;
                    return fe1.name.compareTo(fe2.name);
                }
                return 0;
            }
        };
        FileEntry parent;
        String name;
        String info;
        String permissions;
        String size;
        String date;
        String time;
        String owner;
        String group;
        int type;
        boolean isAppPackage;
        boolean isRoot;
        long fetchTime = 0;
        final ArrayList<FileEntry> mChildren = new ArrayList<FileEntry>();
        private FileEntry(FileEntry parent, String name, int type, boolean isRoot) {
            this.parent = parent;
            this.name = name;
            this.type = type;
            this.isRoot = isRoot;
            checkAppPackageStatus();
        }
        public String getName() {
            return name;
        }
        public String getSize() {
            return size;
        }
        public int getSizeValue() {
            return Integer.parseInt(size);
        }
        public String getDate() {
            return date;
        }
        public String getTime() {
            return time;
        }
        public String getPermissions() {
            return permissions;
        }
        public String getInfo() {
            return info;
        }
        public String getFullPath() {
            if (isRoot) {
                return FILE_ROOT;
            }
            StringBuilder pathBuilder = new StringBuilder();
            fillPathBuilder(pathBuilder, false);
            return pathBuilder.toString();
        }
        public String getFullEscapedPath() {
            StringBuilder pathBuilder = new StringBuilder();
            fillPathBuilder(pathBuilder, true);
            return pathBuilder.toString();
        }
        public String[] getPathSegments() {
            ArrayList<String> list = new ArrayList<String>();
            fillPathSegments(list);
            return list.toArray(new String[list.size()]);
        }
        public int getType() {
            return type;
        }
        public boolean isDirectory() {
            return type == TYPE_DIRECTORY || type == TYPE_DIRECTORY_LINK;
        }
        public FileEntry getParent() {
            return parent;
        }
        public FileEntry[] getCachedChildren() {
            return mChildren.toArray(new FileEntry[mChildren.size()]);
        }
        public FileEntry findChild(String name) {
            for (FileEntry entry : mChildren) {
                if (entry.name.equals(name)) {
                    return entry;
                }
            }
            return null;
        }
        public boolean isRoot() {
            return isRoot;
        }
        void addChild(FileEntry child) {
            mChildren.add(child);
        }
        void setChildren(ArrayList<FileEntry> newChildren) {
            mChildren.clear();
            mChildren.addAll(newChildren);
        }
        boolean needFetch() {
            if (fetchTime == 0) {
                return true;
            }
            long current = System.currentTimeMillis();
            if (current-fetchTime > REFRESH_TEST) {
                return true;
            }
            return false;
        }
        public boolean isApplicationPackage() {
            return isAppPackage;
        }
        public boolean isAppFileName() {
            Matcher m = sApkPattern.matcher(name);
            return m.matches();
        }
        protected void fillPathBuilder(StringBuilder pathBuilder, boolean escapePath) {
            if (isRoot) {
                return;
            }
            if (parent != null) {
                parent.fillPathBuilder(pathBuilder, escapePath);
            }
            pathBuilder.append(FILE_SEPARATOR);
            pathBuilder.append(escapePath ? escape(name) : name);
        }
        protected void fillPathSegments(ArrayList<String> list) {
            if (isRoot) {
                return;
            }
            if (parent != null) {
                parent.fillPathSegments(list);
            }
            list.add(name);
        }
        private void checkAppPackageStatus() {
            isAppPackage = false;
            String[] segments = getPathSegments();
            if (type == TYPE_FILE && segments.length == 3 && isAppFileName()) {
                isAppPackage = DIRECTORY_APP.equals(segments[1]) &&
                    (DIRECTORY_SYSTEM.equals(segments[0]) || DIRECTORY_DATA.equals(segments[0]));
            }
        }
        private String escape(String entryName) {
            return sEscapePattern.matcher(entryName).replaceAll("\\\\$1"); 
        }
    }
    private class LsReceiver extends MultiLineReceiver {
        private ArrayList<FileEntry> mEntryList;
        private ArrayList<String> mLinkList;
        private FileEntry[] mCurrentChildren;
        private FileEntry mParentEntry;
        public LsReceiver(FileEntry parentEntry, ArrayList<FileEntry> entryList,
                ArrayList<String> linkList) {
            mParentEntry = parentEntry;
            mCurrentChildren = parentEntry.getCachedChildren();
            mEntryList = entryList;
            mLinkList = linkList;
        }
        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                if (line.length() == 0) {
                    continue;
                }
                Matcher m = sLsPattern.matcher(line);
                if (m.matches() == false) {
                    continue;
                }
                String name = m.group(7);
                if (mParentEntry.isRoot()) {
                    boolean found = false;
                    for (String approved : sRootLevelApprovedItems) {
                        if (approved.equals(name)) {
                            found = true;
                            break;
                        }
                    }
                    if (found == false) {
                        continue;
                    }
                }
                String permissions = m.group(1);
                String owner = m.group(2);
                String group = m.group(3);
                String size = m.group(4);
                String date = m.group(5);
                String time = m.group(6);
                String info = null;
                int objectType = TYPE_OTHER;
                switch (permissions.charAt(0)) {
                    case '-' :
                        objectType = TYPE_FILE;
                        break;
                    case 'b' :
                        objectType = TYPE_BLOCK;
                        break;
                    case 'c' :
                        objectType = TYPE_CHARACTER;
                        break;
                    case 'd' :
                        objectType = TYPE_DIRECTORY;
                        break;
                    case 'l' :
                        objectType = TYPE_LINK;
                        break;
                    case 's' :
                        objectType = TYPE_SOCKET;
                        break;
                    case 'p' :
                        objectType = TYPE_FIFO;
                        break;
                }
                if (objectType == TYPE_LINK) {
                    String[] segments = name.split("\\s->\\s"); 
                    if (segments.length == 2) {
                        name = segments[0];
                        info = segments[1];
                        String[] pathSegments = info.split(FILE_SEPARATOR);
                        if (pathSegments.length == 1) {
                            if ("..".equals(pathSegments[0])) { 
                                objectType = TYPE_DIRECTORY_LINK;
                            } else {
                            }
                        }
                    }
                    info = "-> " + info; 
                }
                FileEntry entry = getExistingEntry(name);
                if (entry == null) {
                    entry = new FileEntry(mParentEntry, name, objectType, false );
                }
                entry.permissions = permissions;
                entry.size = size;
                entry.date = date;
                entry.time = time;
                entry.owner = owner;
                entry.group = group;
                if (objectType == TYPE_LINK) {
                    entry.info = info;
                }
                mEntryList.add(entry);
            }
        }
        private FileEntry getExistingEntry(String name) {
            for (int i = 0 ; i < mCurrentChildren.length; i++) {
                FileEntry e = mCurrentChildren[i];
                if (e != null) {
                    if (name.equals(e.name)) {
                        mCurrentChildren[i] = null;
                        return e;
                    }
                }
            }
            return null;
        }
        public boolean isCancelled() {
            return false;
        }
        public void finishLinks() {
        }
    }
    public interface IListingReceiver {
        public void setChildren(FileEntry entry, FileEntry[] children);
        public void refreshEntry(FileEntry entry);
    }
    FileListingService(Device device) {
        mDevice = device;
    }
    public FileEntry getRoot() {
        if (mDevice != null) {
            if (mRoot == null) {
                mRoot = new FileEntry(null , "" , TYPE_DIRECTORY,
                        true );
            }
            return mRoot;
        }
        return null;
    }
    public FileEntry[] getChildren(final FileEntry entry, boolean useCache,
            final IListingReceiver receiver) {
        if (useCache && entry.needFetch() == false) {
            return entry.getCachedChildren();
        }
        if (receiver == null) {
            doLs(entry);
            return entry.getCachedChildren();
        }
        Thread t = new Thread("ls " + entry.getFullPath()) { 
            @Override
            public void run() {
                doLs(entry);
                receiver.setChildren(entry, entry.getCachedChildren());
                final FileEntry[] children = entry.getCachedChildren();
                if (children.length > 0 && children[0].isApplicationPackage()) {
                    final HashMap<String, FileEntry> map = new HashMap<String, FileEntry>();
                    for (FileEntry child : children) {
                        String path = child.getFullPath();
                        map.put(path, child);
                    }
                    String command = PM_FULL_LISTING;
                    try {
                        mDevice.executeShellCommand(command, new MultiLineReceiver() {
                            @Override
                            public void processNewLines(String[] lines) {
                                for (String line : lines) {
                                    if (line.length() > 0) {
                                        Matcher m = sPmPattern.matcher(line);
                                        if (m.matches()) {
                                            FileEntry entry = map.get(m.group(1));
                                            if (entry != null) {
                                                entry.info = m.group(2);
                                                receiver.refreshEntry(entry);
                                            }
                                        }
                                    }
                                }
                            }
                            public boolean isCancelled() {
                                return false;
                            }
                        });
                    } catch (IOException e) {
                    }
                }
                synchronized (mThreadList) {
                    mThreadList.remove(this);
                    if (mThreadList.size() > 0) {
                        Thread t = mThreadList.get(0);
                        t.start();
                    }
                }
            }
        };
        synchronized (mThreadList) {
            mThreadList.add(t);
            if (mThreadList.size() == 1) {
                t.start();
            }
        }
        return null;
    }
    private void doLs(FileEntry entry) {
        ArrayList<FileEntry> entryList = new ArrayList<FileEntry>();
        ArrayList<String> linkList = new ArrayList<String>();
        try {
            String command = "ls -l " + entry.getFullPath(); 
            LsReceiver receiver = new LsReceiver(entry, entryList, linkList);
            mDevice.executeShellCommand(command, receiver);
            receiver.finishLinks();
        } catch (IOException e) {
        }
        entry.fetchTime = System.currentTimeMillis();
        Collections.sort(entryList, FileEntry.sEntryComparator);
        entry.setChildren(entryList);
    }
}

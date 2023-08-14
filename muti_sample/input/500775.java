class FilePreferencesImpl extends AbstractPreferences {
    private static final String PREFS_FILE_NAME = "prefs.xml"; 
    private static String USER_HOME;
    private static String SYSTEM_HOME;
    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                USER_HOME = System.getProperty("user.home") + "/.java/.userPrefs";
                SYSTEM_HOME = System.getProperty("java.home") + "/.systemPrefs";
                return null;
            }
        });
    }
    private String path;
    private Properties prefs;
    private File prefsFile;
    private File dir;
    private Set<String> removed = new HashSet<String>();
    private Set<String> updated = new HashSet<String>();
    FilePreferencesImpl(boolean userNode) {
        super(null, ""); 
        this.userNode = userNode;
        path = userNode ? USER_HOME : SYSTEM_HOME;
        initPrefs();
    }
    private FilePreferencesImpl(AbstractPreferences parent, String name) {
        super(parent, name);
        path = ((FilePreferencesImpl) parent).path + File.separator + name;
        initPrefs();
    }
    private void initPrefs() {
        dir = new File(path);
        newNode = (AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                return Boolean.valueOf(!dir.exists());
            }
        })).booleanValue();
        prefsFile = new File(path + File.separator + PREFS_FILE_NAME);
        prefs = XMLParser.loadFilePrefs(prefsFile);
    }
    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        String[] names = AccessController
        .doPrivileged(new PrivilegedAction<String[]>() {
            public String[] run() {
                return dir.list(new FilenameFilter() {
                    public boolean accept(File parent, String name) {
                        return new File(path + File.separator + name).isDirectory();
                    }
                });
            }
        });
        if (null == names) {
            throw new BackingStoreException(
                    Messages.getString("prefs.3", toString()));  
        }
        return names;
    }
    @Override
    protected AbstractPreferences childSpi(String name) {
        FilePreferencesImpl child = new FilePreferencesImpl(this, name);
        return child;
    }
    @Override
    protected void flushSpi() throws BackingStoreException {
        try {
            if(isRemoved()){
                return;
            }
            Properties currentPrefs = XMLParser.loadFilePrefs(prefsFile);
            Iterator<String> it = removed.iterator();
            while (it.hasNext()) {
                currentPrefs.remove(it.next());
            }
            removed.clear();
            it = updated.iterator();
            while (it.hasNext()) {
                Object key = it.next();
                currentPrefs.put(key, prefs.get(key));
            }
            updated.clear();
            prefs = currentPrefs;
            XMLParser.flushFilePrefs(prefsFile, prefs);
        } catch (Exception e) {
            throw new BackingStoreException(e);
        }
    }
    @Override
    protected String getSpi(String key) {
        try {
            if (null == prefs) {
                prefs = XMLParser.loadFilePrefs(prefsFile);
            }
            return prefs.getProperty(key);
        } catch (Exception e) {
            return null;
        }
    }
    @Override
    protected String[] keysSpi() throws BackingStoreException {
        final Set<Object> ks = prefs.keySet();
        return ks.toArray(new String[ks.size()]);
    }
    @Override
    protected void putSpi(String name, String value) {
        prefs.setProperty(name, value);
        updated.add(name);
    }
    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        boolean removeSucceed = (AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                prefsFile.delete();
                return Boolean.valueOf(dir.delete());
            }
        })).booleanValue();
        if (!removeSucceed) {
            throw new BackingStoreException(Messages.getString("prefs.4", toString()));  
        }
    }
    @Override
    protected void removeSpi(String key) {
        prefs.remove(key);
        updated.remove(key);
        removed.add(key);
    }
    @Override
    protected void syncSpi() throws BackingStoreException {
        flushSpi();
    }
}

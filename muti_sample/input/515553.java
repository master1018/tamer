public abstract class AbstractPreferences extends Preferences {
    private static final List<EventObject> events = new LinkedList<EventObject>();
    private static final EventDispatcher dispatcher = new EventDispatcher("Preference Event Dispatcher"); 
    static {
        dispatcher.setDaemon(true);
        dispatcher.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Preferences uroot = Preferences.userRoot();
                Preferences sroot = Preferences.systemRoot();
                try {
                    uroot.flush();
                } catch (BackingStoreException e) {
                }
                try {
                    sroot.flush();
                } catch (BackingStoreException e) {
                }
            }
        });
    }
    boolean userNode;
    private static class Lock {}
    protected final Object lock;
    protected boolean newNode;
    private Map<String, AbstractPreferences> cachedNode;
    private List<EventListener> nodeChangeListeners;
    private List<EventListener> preferenceChangeListeners;
    private String nodeName;
    private AbstractPreferences parentPref;
    private boolean isRemoved;
    private AbstractPreferences root;
    protected AbstractPreferences(AbstractPreferences parent, String name) {
        if ((null == parent ^ name.length() == 0) || name.indexOf("/") >= 0) { 
            throw new IllegalArgumentException();
        }
        root = null == parent ? this : parent.root;
        nodeChangeListeners = new LinkedList<EventListener>();
        preferenceChangeListeners = new LinkedList<EventListener>();
        isRemoved = false;
        cachedNode = new HashMap<String, AbstractPreferences>();
        nodeName = name;
        parentPref = parent;
        lock = new Lock();
        userNode = root.userNode;
    }
    protected final AbstractPreferences[] cachedChildren() {
        return cachedNode.values().toArray(new AbstractPreferences[cachedNode.size()]);
    }
    protected AbstractPreferences getChild(String name)
            throws BackingStoreException {
        synchronized (lock) {
            checkState();
            AbstractPreferences result = null;
            String[] childrenNames = childrenNames();
            for (int i = 0; i < childrenNames.length; i++) {
                if (childrenNames[i].equals(name)) {
                    result = childSpi(name);
                    break;
                }
            }
            return result;
        }
    }
    protected boolean isRemoved() {
        synchronized (lock) {
            return isRemoved;
        }
    }
    protected abstract void flushSpi() throws BackingStoreException;
    protected abstract String[] childrenNamesSpi() throws BackingStoreException;
    protected abstract AbstractPreferences childSpi(String name);
    protected abstract void putSpi(String name, String value);
    protected abstract String getSpi(String key);
    protected abstract String[] keysSpi() throws BackingStoreException;
    protected abstract void removeNodeSpi() throws BackingStoreException;
    protected abstract void removeSpi(String key);
    protected abstract void syncSpi() throws BackingStoreException;
    @Override
    public String absolutePath() {
        if (parentPref == null) {
            return "/"; 
        } else if (parentPref == root) {
            return "/" + nodeName; 
        }
        return parentPref.absolutePath() + "/" + nodeName; 
    }
    @Override
    public String[] childrenNames() throws BackingStoreException {
        synchronized (lock) {
            checkState();
            TreeSet<String> result = new TreeSet<String>(cachedNode.keySet());
            String[] names = childrenNamesSpi();
            for (int i = 0; i < names.length; i++) {
                result.add(names[i]);
            }
            return result.toArray(new String[result.size()]);
        }
    }
    @Override
    public void clear() throws BackingStoreException {
        synchronized (lock) {
            String[] keyList = keys();
            for (int i = 0; i < keyList.length; i++) {
                remove(keyList[i]);
            }
        }
    }
    @Override
    public void exportNode(OutputStream ostream) throws IOException,
            BackingStoreException {
        if(ostream == null) {
            throw new NullPointerException(Messages.getString("prefs.5"));  
        }
        checkState();
        XMLParser.exportPrefs(this, ostream, false);
    }
    @Override
    public void exportSubtree(OutputStream ostream) throws IOException,
            BackingStoreException {
        if(ostream == null) {
            throw new NullPointerException(Messages.getString("prefs.5"));  
        }
        checkState();
        XMLParser.exportPrefs(this, ostream, true);
    }
    @Override
    public void flush() throws BackingStoreException {
        synchronized (lock) {
            flushSpi();
        }
        AbstractPreferences[] cc = cachedChildren();
        int i;
        for (i = 0; i < cc.length; i++) {
            cc[i].flush();
        }
    }
    @Override
    public String get(String key, String deflt) {
        if (key == null) {
            throw new NullPointerException();
        }
        String result = null;
        synchronized (lock) {
            checkState();
            try {
                result = getSpi(key);
            } catch (Exception e) {
            }
        }
        return (result == null ? deflt : result);
    }
    @Override
    public boolean getBoolean(String key, boolean deflt) {
        String result = get(key, null);
        if (result == null) {
            return deflt;
        }
        if ("true".equalsIgnoreCase(result)) { 
            return true;
        } else if ("false".equalsIgnoreCase(result)) { 
            return false;
        } else {
            return deflt;
        }
    }
    @Override
    public byte[] getByteArray(String key, byte[] deflt) {
        String svalue = get(key, null);
        if (svalue == null) {
            return deflt;
        }
        if (svalue.length() == 0) { 
            return new byte[0];
        }
        try {
            byte[] bavalue = svalue.getBytes("US-ASCII"); 
            if (bavalue.length % 4 != 0) {
                return deflt;
            }
            return Base64.decode(bavalue);
        } catch (Exception e) {
            return deflt;
        }
    }
    @Override
    public double getDouble(String key, double deflt) {
        String result = get(key, null);
        if (result == null) {
            return deflt;
        }
        try {
            return Double.parseDouble(result);
        } catch (NumberFormatException e) {
            return deflt;
        }
    }
    @Override
    public float getFloat(String key, float deflt) {
        String result = get(key, null);
        if (result == null) {
            return deflt;
        }
        try {
            return Float.parseFloat(result);
        } catch (NumberFormatException e) {
            return deflt;
        }
    }
    @Override
    public int getInt(String key, int deflt) {
        String result = get(key, null);
        if (result == null) {
            return deflt;
        }
        try {
            return Integer.parseInt(result);
        } catch (NumberFormatException e) {
            return deflt;
        }
    }
    @Override
    public long getLong(String key, long deflt) {
        String result = get(key, null);
        if (result == null) {
            return deflt;
        }
        try {
            return Long.parseLong(result);
        } catch (NumberFormatException e) {
            return deflt;
        }
    }
    @Override
    public boolean isUserNode() {
        return root == Preferences.userRoot();
    }
    @Override
    public String[] keys() throws BackingStoreException {
        synchronized (lock) {
            checkState();
            return keysSpi();
        }
    }
    @Override
    public String name() {
        return nodeName;
    }
    @Override
    public Preferences node(String name) {
        AbstractPreferences startNode = null;
        synchronized (lock) {
            checkState();
            validateName(name);
            if ("".equals(name)) { 
                return this;
            } else if ("/".equals(name)) { 
                return root;
            }
            if (name.startsWith("/")) { 
                startNode = root;
                name = name.substring(1);
            } else {
                startNode = this;
            }
        }
        try {
            return startNode.nodeImpl(name, true);
        } catch (BackingStoreException e) {
            return null;
        }
    }
    private void validateName(String name) {
        if (name.endsWith("/") && name.length() > 1) { 
            throw new IllegalArgumentException(Messages.getString("prefs.6")); 
        }
        if (name.indexOf("
            throw new IllegalArgumentException(Messages.getString("prefs.7")); 
        }
    }
    private AbstractPreferences nodeImpl(String path, boolean createNew)
            throws BackingStoreException {
        String[] names = path.split("/");
        AbstractPreferences currentNode = this;
        AbstractPreferences temp = null;
        if (null != currentNode) {
            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                synchronized (currentNode.lock) {
                    temp = currentNode.cachedNode.get(name);
                    if (temp == null) {
                        temp = getNodeFromBackend(createNew, currentNode, name);
                    }
                }
                currentNode = temp;
                if (null == currentNode) {
                    break;
                }
            }
        }
        return currentNode;
    }
    private AbstractPreferences getNodeFromBackend(boolean createNew,
            AbstractPreferences currentNode, String name)
            throws BackingStoreException {
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(Messages.getString("prefs.8", 
                    name));
        }
        AbstractPreferences temp;
        if (createNew) {
            temp = currentNode.childSpi(name);
            currentNode.cachedNode.put(name, temp);
            if (temp.newNode && currentNode.nodeChangeListeners.size() > 0) {
                currentNode.notifyChildAdded(temp);
            }
        } else {
            temp = currentNode.getChild(name);
        }
        return temp;
    }
    @Override
    public boolean nodeExists(String name) throws BackingStoreException {
        if (null == name) {
            throw new NullPointerException();
        }
        AbstractPreferences startNode = null;
        synchronized (lock) {
            if (isRemoved()) {
                if ("".equals(name)) { 
                    return false;
                }
                throw new IllegalStateException(Messages.getString("prefs.9"));  
            }
            validateName(name);
            if ("".equals(name) || "/".equals(name)) { 
                return true;
            }
            if (name.startsWith("/")) { 
                startNode = root;
                name = name.substring(1);
            } else {
                startNode = this;
            }
        }
        try {
            Preferences result = startNode.nodeImpl(name, false);
            return null == result ? false : true;
        } catch(IllegalArgumentException e) {
            return false;
        }
    }
    @Override
    public Preferences parent() {
        checkState();
        return parentPref;
    }
    private void checkState() {
        if (isRemoved()) {
            throw new IllegalStateException(Messages.getString("prefs.9"));  
        }
    }
    @Override
    public void put(String key, String value) {
        if (null == key || null == value) {
            throw new NullPointerException();
        }
        if (key.length() > MAX_KEY_LENGTH || value.length() > MAX_VALUE_LENGTH) {
            throw new IllegalArgumentException();
        }
        synchronized (lock) {
            checkState();
            putSpi(key, value);
        }
        notifyPreferenceChange(key, value);
    }
    @Override
    public void putBoolean(String key, boolean value) {
        String sval = String.valueOf(value);
        put(key, sval);
    }
    @Override
    public void putByteArray(String key, byte[] value) {
        try {
            put(key, Base64.encode(value, "US-ASCII")); 
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }
    @Override
    public void putDouble(String key, double value) {
        String sval = Double.toString(value);
        put(key, sval);
    }
    @Override
    public void putFloat(String key, float value) {
        String sval = Float.toString(value);
        put(key, sval);
    }
    @Override
    public void putInt(String key, int value) {
        String sval = Integer.toString(value);
        put(key, sval);
    }
    @Override
    public void putLong(String key, long value) {
        String sval = Long.toString(value);
        put(key, sval);
    }
    @Override
    public void remove(String key) {
        synchronized (lock) {
            checkState();
            removeSpi(key);
        }
        notifyPreferenceChange(key, null);
    }
    @Override
    public void removeNode() throws BackingStoreException {
        if (root == this) {
            throw new UnsupportedOperationException(Messages.getString("prefs.A"));  
        }
        synchronized (parentPref.lock) {
            removeNodeImpl();
        }
    }
    private void removeNodeImpl() throws BackingStoreException {
        synchronized (lock) {
            checkState();
            String[] childrenNames = childrenNamesSpi();
            for (int i = 0; i < childrenNames.length; i++) {
                if (null == cachedNode.get(childrenNames[i])) {
                    AbstractPreferences child = childSpi(childrenNames[i]);
                    cachedNode.put(childrenNames[i], child);
                }
            }
            final Collection<AbstractPreferences> values = cachedNode.values();
            final AbstractPreferences[] children = values.toArray(new AbstractPreferences[values.size()]);
            for (AbstractPreferences child : children) {
                child.removeNodeImpl();
            }
            removeNodeSpi();
            isRemoved = true;
            parentPref.cachedNode.remove(nodeName);
        }
        if (parentPref.nodeChangeListeners.size() > 0) {
            parentPref.notifyChildRemoved(this);
        }
    }
    @Override
    public void addNodeChangeListener(NodeChangeListener ncl) {
        if (null == ncl) {
            throw new NullPointerException();
        }
        checkState();
        synchronized (nodeChangeListeners) {
            nodeChangeListeners.add(ncl);
        }
    }
    @Override
    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        if (null == pcl) {
            throw new NullPointerException();
        }
        checkState();
        synchronized (preferenceChangeListeners) {
            preferenceChangeListeners.add(pcl);
        }
    }
    @Override
    public void removeNodeChangeListener(NodeChangeListener ncl) {
        checkState();
        synchronized (nodeChangeListeners) {
            int pos;
            if ((pos = nodeChangeListeners.indexOf(ncl)) == -1) {
                throw new IllegalArgumentException();
            }
            nodeChangeListeners.remove(pos);
        }
    }
    @Override
    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        checkState();
        synchronized (preferenceChangeListeners) {
            int pos;
            if ((pos = preferenceChangeListeners.indexOf(pcl)) == -1) {
                throw new IllegalArgumentException();
            }
            preferenceChangeListeners.remove(pos);
        }
    }
    @Override
    public void sync() throws BackingStoreException {
        synchronized (lock) {
            checkState();
            syncSpi();
        }
        AbstractPreferences[] cc = cachedChildren();
        int i;
        for (i = 0; i < cc.length; i++) {
            cc[i].sync();
        }
    }
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(isUserNode() ? "User" : "System"); 
        sb.append(" Preference Node: "); 
        sb.append(absolutePath());
        return sb.toString();
    }
    private void notifyChildAdded(Preferences child) {
        NodeChangeEvent nce = new NodeAddEvent(this, child);
        synchronized (events) {
            events.add(nce);
            events.notifyAll();
        }
    }
    private void notifyChildRemoved(Preferences child) {
        NodeChangeEvent nce = new NodeRemoveEvent(this, child);
        synchronized (events) {
            events.add(nce);
            events.notifyAll();
        }
    }
    private void notifyPreferenceChange(String key, String newValue) {
        PreferenceChangeEvent pce = new PreferenceChangeEvent(this, key,
                newValue);
        synchronized (events) {
            events.add(pce);
            events.notifyAll();
        }
    }
    private static class EventDispatcher extends Thread {
        EventDispatcher(String name){
            super(name);
        }
        @Override
        public void run() {
            while (true) {
                EventObject event = null;
                try {
                    event = getEventObject();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
                AbstractPreferences pref = (AbstractPreferences) event
                        .getSource();
                if (event instanceof NodeAddEvent) {
                    dispatchNodeAdd((NodeChangeEvent) event,
                            pref.nodeChangeListeners);
                } else if (event instanceof NodeRemoveEvent) {
                    dispatchNodeRemove((NodeChangeEvent) event,
                            pref.nodeChangeListeners);
                } else if (event instanceof PreferenceChangeEvent) {
                    dispatchPrefChange((PreferenceChangeEvent) event,
                            pref.preferenceChangeListeners);
                }
            }
        }
        private EventObject getEventObject() throws InterruptedException {
            synchronized (events) {
                if (events.isEmpty()) {
                    events.wait();
                }
                EventObject event = events.get(0);
                events.remove(0);
                return event;
            }
        }
        private void dispatchPrefChange(PreferenceChangeEvent event,
                List<EventListener> preferenceChangeListeners) {
            synchronized (preferenceChangeListeners) {
                Iterator<EventListener> i = preferenceChangeListeners.iterator();
                while (i.hasNext()) {
                    PreferenceChangeListener pcl = (PreferenceChangeListener) i
                            .next();
                    pcl.preferenceChange(event);
                }
            }
        }
        private void dispatchNodeRemove(NodeChangeEvent event,
                List<EventListener> nodeChangeListeners) {
            synchronized (nodeChangeListeners) {
                Iterator<EventListener> i = nodeChangeListeners.iterator();
                while (i.hasNext()) {
                    NodeChangeListener ncl = (NodeChangeListener) i.next();
                    ncl.childRemoved(event);
                }
            }
        }
        private void dispatchNodeAdd(NodeChangeEvent event,
                List<EventListener> nodeChangeListeners) {
            synchronized (nodeChangeListeners) {
                Iterator<EventListener> i = nodeChangeListeners.iterator();
                while (i.hasNext()) {
                    NodeChangeListener ncl = (NodeChangeListener) i.next();
                    ncl.childAdded(event);
                }
            }
        }
    }
    private static class NodeAddEvent extends NodeChangeEvent {
        private static final long serialVersionUID = 1L;
        public NodeAddEvent(Preferences p, Preferences c) {
            super(p, c);
        }
    }
    private static class NodeRemoveEvent extends NodeChangeEvent {
        private static final long serialVersionUID = 1L;
        public NodeRemoveEvent(Preferences p, Preferences c) {
            super(p, c);
        }
    }
}

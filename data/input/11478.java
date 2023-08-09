public abstract class PerfDataBufferImpl {
    protected ByteBuffer buffer;
    protected Map<String, Monitor> monitors;
    protected int lvmid;
    protected Map<String, ArrayList<String>> aliasMap;
    protected Map aliasCache;
    protected PerfDataBufferImpl(ByteBuffer buffer, int lvmid) {
        this.buffer = buffer;
        this.lvmid = lvmid;
        this.monitors = new TreeMap<String, Monitor>();
        this.aliasMap = new HashMap<String, ArrayList<String>>();
        this.aliasCache = new HashMap();
    }
    public int getLocalVmId() {
        return lvmid;
    }
    public byte[] getBytes() {
        ByteBuffer bb = null;
        synchronized (this) {
            try {
                if (monitors.isEmpty()) {
                    buildMonitorMap(monitors);
                }
            } catch (MonitorException e) {
            }
            bb = buffer.duplicate();
        }
        bb.rewind();
        byte[] bytes = new byte[bb.limit()];
        bb.get(bytes);
        return bytes;
    }
    public int getCapacity() {
        return buffer.capacity();
    }
    ByteBuffer getByteBuffer() {
        return buffer;
    }
    private void buildAliasMap() {
        assert Thread.holdsLock(this);
        URL aliasURL = null;
        String filename = System.getProperty("sun.jvmstat.perfdata.aliasmap");
        if (filename != null) {
            File f = new File(filename);
            try {
                aliasURL = f.toURL();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            aliasURL = getClass().getResource(
                "/sun/jvmstat/perfdata/resources/aliasmap");
        }
        assert aliasURL != null;
        AliasFileParser aliasParser = new AliasFileParser(aliasURL);
        try {
            aliasParser.parse(aliasMap);
        } catch (IOException e) {
            System.err.println("Error processing " + filename + ": "
                               + e.getMessage());
        } catch (SyntaxException e) {
            System.err.println("Syntax error parsing " + filename + ": "
                               + e.getMessage());
        }
    }
    protected Monitor findByAlias(String name) {
        assert Thread.holdsLock(this);
        Monitor  m = (Monitor)aliasCache.get(name);
        if (m == null) {
            ArrayList al = aliasMap.get(name);
            if (al != null) {
                for (Iterator i = al.iterator(); i.hasNext() && m == null; ) {
                    String alias = (String)i.next();
                    m = (Monitor)monitors.get(alias);
                }
            }
        }
        return m;
    }
    public Monitor findByName(String name) throws MonitorException {
        Monitor m = null;
        synchronized (this) {
            if (monitors.isEmpty()) {
                buildMonitorMap(monitors);
                buildAliasMap();
            }
            m = monitors.get(name);
            if (m == null) {
                getNewMonitors(monitors);
                m = monitors.get(name);
            }
            if (m == null) {
                m = findByAlias(name);
            }
        }
        return m;
    }
    public List<Monitor> findByPattern(String patternString)
                throws MonitorException, PatternSyntaxException {
        synchronized(this) {
            if (monitors.isEmpty()) {
                buildMonitorMap(monitors);
            } else {
                getNewMonitors(monitors);
            }
        }
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher("");
        List<Monitor> matches = new ArrayList<Monitor>();
        Set monitorSet = monitors.entrySet();
        for (Iterator i = monitorSet.iterator(); i.hasNext(); ) {
            Map.Entry me = (Map.Entry)i.next();
            String name = (String)me.getKey();
            Monitor m = (Monitor)me.getValue();
            matcher.reset(name);
            if (matcher.lookingAt()) {
                 matches.add((Monitor)me.getValue());
            }
        }
        return matches;
    }
    public MonitorStatus getMonitorStatus() throws MonitorException {
        synchronized(this) {
            if (monitors.isEmpty()) {
                buildMonitorMap(monitors);
            }
            return getMonitorStatus(monitors);
        }
    }
    protected abstract MonitorStatus getMonitorStatus(Map<String, Monitor> m)
                                     throws MonitorException;
    protected abstract void buildMonitorMap(Map<String, Monitor> m) throws MonitorException;
    protected abstract void getNewMonitors(Map<String, Monitor> m) throws MonitorException;
}

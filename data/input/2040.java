abstract class AppletPanel extends Panel implements AppletStub, Runnable {
    Applet applet;
    protected boolean doInit = true;
    protected AppletClassLoader loader;
    public final static int APPLET_DISPOSE = 0;
    public final static int APPLET_LOAD = 1;
    public final static int APPLET_INIT = 2;
    public final static int APPLET_START = 3;
    public final static int APPLET_STOP = 4;
    public final static int APPLET_DESTROY = 5;
    public final static int APPLET_QUIT = 6;
    public final static int APPLET_ERROR = 7;
    public final static int APPLET_RESIZE = 51234;
    public final static int APPLET_LOADING = 51235;
    public final static int APPLET_LOADING_COMPLETED = 51236;
    protected int status;
    protected Thread handler;
    Dimension defaultAppletSize = new Dimension(10, 10);
    Dimension currentAppletSize = new Dimension(10, 10);
    MessageUtils mu = new MessageUtils();
    Thread loaderThread = null;
    boolean loadAbortRequest = false;
    abstract protected String getCode();
    abstract protected String getJarFiles();
    abstract protected String getSerializedObject();
    abstract public int    getWidth();
    abstract public int    getHeight();
    abstract public boolean hasInitialFocus();
    private static int threadGroupNumber = 0;
    protected void setupAppletAppContext() {
    }
    synchronized void createAppletThread() {
        String nm = "applet-" + getCode();
        loader = getClassLoader(getCodeBase(), getClassLoaderCacheKey());
        loader.grab(); 
        String param = getParameter("codebase_lookup");
        if (param != null && param.equals("false"))
            loader.setCodebaseLookup(false);
        else
            loader.setCodebaseLookup(true);
        ThreadGroup appletGroup = loader.getThreadGroup();
        handler = new Thread(appletGroup, this, "thread " + nm);
        AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    handler.setContextClassLoader(loader);
                    return null;
                }
            });
        handler.start();
    }
    void joinAppletThread() throws InterruptedException {
        if (handler != null) {
            handler.join();
            handler = null;
        }
    }
    void release() {
        if (loader != null) {
            loader.release();
            loader = null;
        }
    }
    public void init() {
        try {
            defaultAppletSize.width = getWidth();
            currentAppletSize.width = defaultAppletSize.width;
            defaultAppletSize.height = getHeight();
            currentAppletSize.height = defaultAppletSize.height;
        } catch (NumberFormatException e) {
            status = APPLET_ERROR;
            showAppletStatus("badattribute.exception");
            showAppletLog("badattribute.exception");
            showAppletException(e);
        }
        setLayout(new BorderLayout());
        createAppletThread();
    }
    public Dimension minimumSize() {
        return new Dimension(defaultAppletSize.width,
                             defaultAppletSize.height);
    }
    public Dimension preferredSize() {
        return new Dimension(currentAppletSize.width,
                             currentAppletSize.height);
    }
    private AppletListener listeners;
    private Queue queue = null;
    synchronized public void addAppletListener(AppletListener l) {
        listeners = AppletEventMulticaster.add(listeners, l);
    }
    synchronized public void removeAppletListener(AppletListener l) {
        listeners = AppletEventMulticaster.remove(listeners, l);
    }
    public void dispatchAppletEvent(int id, Object argument) {
        if (listeners != null) {
            AppletEvent evt = new AppletEvent(this, id, argument);
            listeners.appletStateChanged(evt);
        }
    }
    public void sendEvent(int id) {
        synchronized(this) {
            if (queue == null) {
                queue = new Queue();
            }
            Integer eventId = Integer.valueOf(id);
            queue.enqueue(eventId);
            notifyAll();
        }
        if (id == APPLET_QUIT) {
            try {
                joinAppletThread(); 
            } catch (InterruptedException e) {
            }
            if (loader == null)
                loader = getClassLoader(getCodeBase(), getClassLoaderCacheKey());
            release();
        }
    }
    synchronized AppletEvent getNextEvent() throws InterruptedException {
        while (queue == null || queue.isEmpty()) {
            wait();
        }
        Integer eventId = (Integer)queue.dequeue();
        return new AppletEvent(this, eventId.intValue(), null);
    }
    boolean emptyEventQueue() {
        if ((queue == null) || (queue.isEmpty()))
            return true;
        else
            return false;
    }
     private void setExceptionStatus(AccessControlException e) {
     Permission p = e.getPermission();
     if (p instanceof RuntimePermission) {
         if (p.getName().startsWith("modifyThread")) {
             if (loader == null)
                 loader = getClassLoader(getCodeBase(), getClassLoaderCacheKey());
             loader.setExceptionStatus();
         }
     }
     }
    public void run() {
        Thread curThread = Thread.currentThread();
        if (curThread == loaderThread) {
            runLoader();
            return;
        }
        boolean disposed = false;
        while (!disposed && !curThread.isInterrupted()) {
            AppletEvent evt;
            try {
                evt = getNextEvent();
            } catch (InterruptedException e) {
                showAppletStatus("bail");
                return;
            }
            try {
                switch (evt.getID()) {
                  case APPLET_LOAD:
                      if (!okToLoad()) {
                          break;
                      }
                      if (loaderThread == null) {
                          setLoaderThread(new Thread(this));
                          loaderThread.start();
                          loaderThread.join();
                          setLoaderThread(null);
                      } else {
                      }
                      break;
                  case APPLET_INIT:
                      if (status != APPLET_LOAD && status != APPLET_DESTROY) {
                          showAppletStatus("notloaded");
                          break;
                      }
                      applet.resize(defaultAppletSize);
                      if (doInit) {
                          if (PerformanceLogger.loggingEnabled()) {
                              PerformanceLogger.setTime("Applet Init");
                              PerformanceLogger.outputLog();
                          }
                          applet.init();
                      }
                      Font f = getFont();
                      if (f == null ||
                          "dialog".equals(f.getFamily().toLowerCase(Locale.ENGLISH)) &&
                          f.getSize() == 12 && f.getStyle() == Font.PLAIN) {
                          setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
                      }
                      doInit = true;    
                      try {
                          final AppletPanel p = this;
                          EventQueue.invokeAndWait(new Runnable() {
                                  public void run() {
                                      p.validate();
                                  }
                              });
                      }
                      catch(InterruptedException ie) {
                      }
                      catch(InvocationTargetException ite) {
                      }
                      status = APPLET_INIT;
                      showAppletStatus("inited");
                      break;
                  case APPLET_START:
                  {
                      if (status != APPLET_INIT && status != APPLET_STOP) {
                          showAppletStatus("notinited");
                          break;
                      }
                      applet.resize(currentAppletSize);
                      applet.start();
                      try {
                          final AppletPanel p = this;
                          final Applet a = applet;
                          EventQueue.invokeAndWait(new Runnable() {
                                  public void run() {
                                      p.validate();
                                      a.setVisible(true);
                                      if (hasInitialFocus())
                                        setDefaultFocus();
                                  }
                              });
                      }
                      catch(InterruptedException ie) {
                      }
                      catch(InvocationTargetException ite) {
                      }
                      status = APPLET_START;
                      showAppletStatus("started");
                      break;
                  }
                case APPLET_STOP:
                    if (status != APPLET_START) {
                        showAppletStatus("notstarted");
                        break;
                    }
                    status = APPLET_STOP;
                    try {
                        final Applet a = applet;
                        EventQueue.invokeAndWait(new Runnable() {
                                public void run()
                                {
                                    a.setVisible(false);
                                }
                            });
                    }
                    catch(InterruptedException ie) {
                    }
                    catch(InvocationTargetException ite) {
                    }
                    try {
                        applet.stop();
                    } catch (java.security.AccessControlException e) {
                        setExceptionStatus(e);
                        throw e;
                    }
                    showAppletStatus("stopped");
                    break;
                case APPLET_DESTROY:
                    if (status != APPLET_STOP && status != APPLET_INIT) {
                        showAppletStatus("notstopped");
                        break;
                    }
                    status = APPLET_DESTROY;
                    try {
                        applet.destroy();
                    } catch (java.security.AccessControlException e) {
                        setExceptionStatus(e);
                        throw e;
                    }
                    showAppletStatus("destroyed");
                    break;
                case APPLET_DISPOSE:
                    if (status != APPLET_DESTROY && status != APPLET_LOAD) {
                        showAppletStatus("notdestroyed");
                        break;
                    }
                    status = APPLET_DISPOSE;
                    try
                    {
                        final Applet a = applet;
                        EventQueue.invokeAndWait(new Runnable()
                        {
                            public void run()
                            {
                                remove(a);
                            }
                        });
                    }
                    catch(InterruptedException ie)
                    {
                    }
                    catch(InvocationTargetException ite)
                    {
                    }
                    applet = null;
                    showAppletStatus("disposed");
                    disposed = true;
                    break;
                case APPLET_QUIT:
                    return;
                }
            } catch (Exception e) {
                status = APPLET_ERROR;
                if (e.getMessage() != null) {
                    showAppletStatus("exception2", e.getClass().getName(),
                                     e.getMessage());
                } else {
                    showAppletStatus("exception", e.getClass().getName());
                }
                showAppletException(e);
            } catch (ThreadDeath e) {
                showAppletStatus("death");
                return;
            } catch (Error e) {
                status = APPLET_ERROR;
                if (e.getMessage() != null) {
                    showAppletStatus("error2", e.getClass().getName(),
                                     e.getMessage());
                } else {
                    showAppletStatus("error", e.getClass().getName());
                }
                showAppletException(e);
            }
            clearLoadAbortRequest();
        }
    }
    private Component getMostRecentFocusOwnerForWindow(Window w) {
        Method meth = (Method)AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    Method meth = null;
                    try {
                        meth = KeyboardFocusManager.class.getDeclaredMethod("getMostRecentFocusOwner", new Class[] {Window.class});
                        meth.setAccessible(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return meth;
                }
            });
        if (meth != null) {
            try {
                return (Component)meth.invoke(null, new Object[] {w});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return w.getMostRecentFocusOwner();
    }
    private void setDefaultFocus() {
        Component toFocus = null;
        Container parent = getParent();
        if(parent != null) {
            if (parent instanceof Window) {
                toFocus = getMostRecentFocusOwnerForWindow((Window)parent);
                if (toFocus == parent || toFocus == null) {
                    toFocus = parent.getFocusTraversalPolicy().
                        getInitialComponent((Window)parent);
                }
            } else if (parent.isFocusCycleRoot()) {
                toFocus = parent.getFocusTraversalPolicy().
                    getDefaultComponent(parent);
            }
        }
        if (toFocus != null) {
            if (parent instanceof EmbeddedFrame) {
                ((EmbeddedFrame)parent).synthesizeWindowActivation(true);
            }
            toFocus.requestFocusInWindow();
        }
    }
    private void runLoader() {
        if (status != APPLET_DISPOSE) {
            showAppletStatus("notdisposed");
            return;
        }
        dispatchAppletEvent(APPLET_LOADING, null);
        status = APPLET_LOAD;
        loader = getClassLoader(getCodeBase(), getClassLoaderCacheKey());
        String code = getCode();
        setupAppletAppContext();
        try {
            loadJarFiles(loader);
            applet = createApplet(loader);
        } catch (ClassNotFoundException e) {
            status = APPLET_ERROR;
            showAppletStatus("notfound", code);
            showAppletLog("notfound", code);
            showAppletException(e);
            return;
        } catch (InstantiationException e) {
            status = APPLET_ERROR;
            showAppletStatus("nocreate", code);
            showAppletLog("nocreate", code);
            showAppletException(e);
            return;
        } catch (IllegalAccessException e) {
            status = APPLET_ERROR;
            showAppletStatus("noconstruct", code);
            showAppletLog("noconstruct", code);
            showAppletException(e);
            return;
        } catch (Exception e) {
            status = APPLET_ERROR;
            showAppletStatus("exception", e.getMessage());
            showAppletException(e);
            return;
        } catch (ThreadDeath e) {
            status = APPLET_ERROR;
            showAppletStatus("death");
            return;
        } catch (Error e) {
            status = APPLET_ERROR;
            showAppletStatus("error", e.getMessage());
            showAppletException(e);
            return;
        } finally {
            dispatchAppletEvent(APPLET_LOADING_COMPLETED, null);
        }
        if (applet != null)
        {
            applet.setStub(this);
            applet.hide();
            add("Center", applet);
            showAppletStatus("loaded");
            validate();
        }
    }
    protected Applet createApplet(final AppletClassLoader loader) throws ClassNotFoundException,
                                                                         IllegalAccessException, IOException, InstantiationException, InterruptedException {
        final String serName = getSerializedObject();
        String code = getCode();
        if (code != null && serName != null) {
            System.err.println(amh.getMessage("runloader.err"));
            throw new InstantiationException("Either \"code\" or \"object\" should be specified, but not both.");
        }
        if (code == null && serName == null) {
            String msg = "nocode";
            status = APPLET_ERROR;
            showAppletStatus(msg);
            showAppletLog(msg);
            repaint();
        }
        if (code != null) {
            applet = (Applet)loader.loadCode(code).newInstance();
            doInit = true;
        } else {
            InputStream is = (InputStream)
                java.security.AccessController.doPrivileged(
                                                            new java.security.PrivilegedAction() {
                                                                public Object run() {
                                                                    return loader.getResourceAsStream(serName);
                                                                }
                                                            });
            ObjectInputStream ois =
                new AppletObjectInputStream(is, loader);
            Object serObject = ois.readObject();
            applet = (Applet) serObject;
            doInit = false; 
        }
        findAppletJDKLevel(applet);
        if (Thread.interrupted()) {
            try {
                status = APPLET_DISPOSE; 
                applet = null;
                showAppletStatus("death");
            } finally {
                Thread.currentThread().interrupt(); 
            }
            return null;
        }
        return applet;
    }
    protected void loadJarFiles(AppletClassLoader loader) throws IOException,
                                                                 InterruptedException {
        String jarFiles = getJarFiles();
        if (jarFiles != null) {
            StringTokenizer st = new StringTokenizer(jarFiles, ",", false);
            while(st.hasMoreTokens()) {
                String tok = st.nextToken().trim();
                try {
                    loader.addJar(tok);
                } catch (IllegalArgumentException e) {
                    continue;
                }
            }
        }
    }
    protected synchronized void stopLoading() {
        if (loaderThread != null) {
            loaderThread.interrupt();
        } else {
            setLoadAbortRequest();
        }
    }
    protected synchronized boolean okToLoad() {
        return !loadAbortRequest;
    }
    protected synchronized void clearLoadAbortRequest() {
        loadAbortRequest = false;
    }
    protected synchronized void setLoadAbortRequest() {
        loadAbortRequest = true;
    }
    private synchronized void setLoaderThread(Thread loaderThread) {
        this.loaderThread = loaderThread;
    }
    public boolean isActive() {
        return status == APPLET_START;
    }
    private EventQueue appEvtQ = null;
    public void appletResize(int width, int height) {
        currentAppletSize.width = width;
        currentAppletSize.height = height;
        final Dimension currentSize = new Dimension(currentAppletSize.width,
                                                    currentAppletSize.height);
        if(loader != null) {
            AppContext appCtxt = loader.getAppContext();
            if(appCtxt != null)
                appEvtQ = (java.awt.EventQueue)appCtxt.get(AppContext.EVENT_QUEUE_KEY);
        }
        final AppletPanel ap = this;
        if (appEvtQ != null){
            appEvtQ.postEvent(new InvocationEvent(Toolkit.getDefaultToolkit(),
                                                  new Runnable(){
                                                      public void run(){
                                                          if(ap != null)
                                                          {
                                                              ap.dispatchAppletEvent(APPLET_RESIZE, currentSize);
                                                          }
                                                      }
                                                  }));
        }
    }
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        currentAppletSize.width = width;
        currentAppletSize.height = height;
    }
    public Applet getApplet() {
        return applet;
    }
    protected void showAppletStatus(String status) {
        getAppletContext().showStatus(amh.getMessage(status));
    }
    protected void showAppletStatus(String status, Object arg) {
        getAppletContext().showStatus(amh.getMessage(status, arg));
    }
    protected void showAppletStatus(String status, Object arg1, Object arg2) {
        getAppletContext().showStatus(amh.getMessage(status, arg1, arg2));
    }
    protected void showAppletLog(String msg) {
        System.out.println(amh.getMessage(msg));
    }
    protected void showAppletLog(String msg, Object arg) {
        System.out.println(amh.getMessage(msg, arg));
    }
    protected void showAppletException(Throwable t) {
        t.printStackTrace();
        repaint();
    }
    public String getClassLoaderCacheKey()
    {
        return getCodeBase().toString();
    }
    private static HashMap classloaders = new HashMap();
    public static synchronized void flushClassLoader(String key) {
        classloaders.remove(key);
    }
    public static synchronized void flushClassLoaders() {
        classloaders = new HashMap();
    }
    protected AppletClassLoader createClassLoader(final URL codebase) {
        return new AppletClassLoader(codebase);
    }
    synchronized AppletClassLoader getClassLoader(final URL codebase, final String key) {
        AppletClassLoader c = (AppletClassLoader)classloaders.get(key);
        if (c == null) {
            AccessControlContext acc =
                getAccessControlContext(codebase);
            c = (AppletClassLoader)
                AccessController.doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            AppletClassLoader ac = createClassLoader(codebase);
                            synchronized (getClass()) {
                                AppletClassLoader res =
                                    (AppletClassLoader)classloaders.get(key);
                                if (res == null) {
                                    classloaders.put(key, ac);
                                    return ac;
                                } else {
                                    return res;
                                }
                            }
                        }
                    },acc);
        }
        return c;
    }
    private AccessControlContext getAccessControlContext(final URL codebase) {
        PermissionCollection perms = (PermissionCollection)
            AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        Policy p = java.security.Policy.getPolicy();
                        if (p != null) {
                            return p.getPermissions(new CodeSource(null,
                                                                   (java.security.cert.Certificate[]) null));
                        } else {
                            return null;
                        }
                    }
                });
        if (perms == null)
            perms = new Permissions();
        perms.add(SecurityConstants.CREATE_CLASSLOADER_PERMISSION);
        Permission p;
        java.net.URLConnection urlConnection = null;
        try {
            urlConnection = codebase.openConnection();
            p = urlConnection.getPermission();
        } catch (java.io.IOException ioe) {
            p = null;
        }
        if (p != null)
            perms.add(p);
        if (p instanceof FilePermission) {
            String path = p.getName();
            int endIndex = path.lastIndexOf(File.separatorChar);
            if (endIndex != -1) {
                path = path.substring(0, endIndex+1);
                if (path.endsWith(File.separator)) {
                    path += "-";
                }
                perms.add(new FilePermission(path,
                                             SecurityConstants.FILE_READ_ACTION));
            }
        } else {
            URL locUrl = codebase;
            if (urlConnection instanceof JarURLConnection) {
                locUrl = ((JarURLConnection)urlConnection).getJarFileURL();
            }
            String host = locUrl.getHost();
            if (host != null && (host.length() > 0))
                perms.add(new SocketPermission(host,
                                               SecurityConstants.SOCKET_CONNECT_ACCEPT_ACTION));
        }
        ProtectionDomain domain =
            new ProtectionDomain(new CodeSource(codebase,
                                                (java.security.cert.Certificate[]) null), perms);
        AccessControlContext acc =
            new AccessControlContext(new ProtectionDomain[] { domain });
        return acc;
    }
    public Thread getAppletHandlerThread() {
        return handler;
    }
    public int getAppletWidth() {
        return currentAppletSize.width;
    }
    public int getAppletHeight() {
        return currentAppletSize.height;
    }
    public static void changeFrameAppContext(Frame frame, AppContext newAppContext)
    {
        AppContext oldAppContext = SunToolkit.targetToAppContext(frame);
        if (oldAppContext == newAppContext)
            return;
        synchronized (Window.class)
        {
            WeakReference weakRef = null;
            {
                Vector<WeakReference<Window>> windowList = (Vector<WeakReference<Window>>)oldAppContext.get(Window.class);
                if (windowList != null) {
                    for (WeakReference ref : windowList) {
                        if (ref.get() == frame) {
                            weakRef = ref;
                            break;
                        }
                    }
                    if (weakRef != null)
                        windowList.remove(weakRef);
                }
            }
            SunToolkit.insertTargetMapping(frame, newAppContext);
            {
                Vector<WeakReference<Window>> windowList = (Vector)newAppContext.get(Window.class);
                if (windowList == null) {
                    windowList = new Vector<WeakReference<Window>>();
                    newAppContext.put(Window.class, windowList);
                }
                windowList.add(weakRef);
            }
        }
    }
    private boolean jdk11Applet = false;
    private boolean jdk12Applet = false;
    private void findAppletJDKLevel(Applet applet)
    {
        Class appletClass = applet.getClass();
        synchronized(appletClass)  {
            Boolean jdk11Target = (Boolean) loader.isJDK11Target(appletClass);
            Boolean jdk12Target = (Boolean) loader.isJDK12Target(appletClass);
            if (jdk11Target != null || jdk12Target != null) {
                jdk11Applet = (jdk11Target == null) ? false : jdk11Target.booleanValue();
                jdk12Applet = (jdk12Target == null) ? false : jdk12Target.booleanValue();
                return;
            }
            String name = appletClass.getName();
            name = name.replace('.', '/');
            final String resourceName = name + ".class";
            InputStream is = null;
            byte[] classHeader = new byte[8];
            try {
                is = (InputStream) java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction() {
                        public Object run() {
                            return loader.getResourceAsStream(resourceName);
                        }
                    });
                int byteRead = is.read(classHeader, 0, 8);
                is.close();
                if (byteRead != 8)
                    return;
            }
            catch (IOException e)   {
                return;
            }
            int major_version = readShort(classHeader, 6);
            if (major_version < 46)
                jdk11Applet = true;
            else if (major_version == 46)
                jdk12Applet = true;
            loader.setJDK11Target(appletClass, jdk11Applet);
            loader.setJDK12Target(appletClass, jdk12Applet);
        }
    }
    protected boolean isJDK11Applet()   {
        return jdk11Applet;
    }
    protected boolean isJDK12Applet()   {
        return jdk12Applet;
    }
    private int readShort(byte[] b, int off)    {
        int hi = readByte(b[off]);
        int lo = readByte(b[off + 1]);
        return (hi << 8) | lo;
    }
    private int readByte(byte b) {
        return ((int)b) & 0xFF;
    }
    private static AppletMessageHandler amh = new AppletMessageHandler("appletpanel");
}

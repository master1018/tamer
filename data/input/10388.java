public class Beans {
    private static final Object DESIGN_TIME = new Object();
    private static final Object GUI_AVAILABLE = new Object();
    public static Object instantiate(ClassLoader cls, String beanName) throws IOException, ClassNotFoundException {
        return Beans.instantiate(cls, beanName, null, null);
    }
    public static Object instantiate(ClassLoader cls, String beanName, BeanContext beanContext) throws IOException, ClassNotFoundException {
        return Beans.instantiate(cls, beanName, beanContext, null);
    }
    public static Object instantiate(ClassLoader cls, String beanName, BeanContext beanContext, AppletInitializer initializer)
                        throws IOException, ClassNotFoundException {
        InputStream ins;
        ObjectInputStream oins = null;
        Object result = null;
        boolean serialized = false;
        IOException serex = null;
        if (cls == null) {
            try {
                cls = ClassLoader.getSystemClassLoader();
            } catch (SecurityException ex) {
            }
        }
        final String serName = beanName.replace('.','/').concat(".ser");
        final ClassLoader loader = cls;
        ins = (InputStream)AccessController.doPrivileged
            (new PrivilegedAction() {
                public Object run() {
                    if (loader == null)
                        return ClassLoader.getSystemResourceAsStream(serName);
                    else
                        return loader.getResourceAsStream(serName);
                }
        });
        if (ins != null) {
            try {
                if (cls == null) {
                    oins = new ObjectInputStream(ins);
                } else {
                    oins = new ObjectInputStreamWithLoader(ins, cls);
                }
                result = oins.readObject();
                serialized = true;
                oins.close();
            } catch (IOException ex) {
                ins.close();
                serex = ex;
            } catch (ClassNotFoundException ex) {
                ins.close();
                throw ex;
            }
        }
        if (result == null) {
            Class cl;
            try {
                cl = ClassFinder.findClass(beanName, cls);
            } catch (ClassNotFoundException ex) {
                if (serex != null) {
                    throw serex;
                }
                throw ex;
            }
            try {
                result = cl.newInstance();
            } catch (Exception ex) {
                throw new ClassNotFoundException("" + cl + " : " + ex, ex);
            }
        }
        if (result != null) {
            AppletStub stub = null;
            if (result instanceof Applet) {
                Applet  applet      = (Applet) result;
                boolean needDummies = initializer == null;
                if (needDummies) {
                    final String resourceName;
                    if (serialized) {
                        resourceName = beanName.replace('.','/').concat(".ser");
                    } else {
                        resourceName = beanName.replace('.','/').concat(".class");
                    }
                    URL objectUrl = null;
                    URL codeBase  = null;
                    URL docBase   = null;
                    final ClassLoader cloader = cls;
                    objectUrl = (URL)
                        AccessController.doPrivileged
                        (new PrivilegedAction() {
                            public Object run() {
                                if (cloader == null)
                                    return ClassLoader.getSystemResource
                                                                (resourceName);
                                else
                                    return cloader.getResource(resourceName);
                            }
                    });
                    if (objectUrl != null) {
                        String s = objectUrl.toExternalForm();
                        if (s.endsWith(resourceName)) {
                            int ix   = s.length() - resourceName.length();
                            codeBase = new URL(s.substring(0,ix));
                            docBase  = codeBase;
                            ix = s.lastIndexOf('/');
                            if (ix >= 0) {
                                docBase = new URL(s.substring(0,ix+1));
                            }
                        }
                    }
                    BeansAppletContext context = new BeansAppletContext(applet);
                    stub = (AppletStub)new BeansAppletStub(applet, context, codeBase, docBase);
                    applet.setStub(stub);
                } else {
                    initializer.initialize(applet, beanContext);
                }
                if (beanContext != null) {
                    beanContext.add(result);
                }
                if (!serialized) {
                    applet.setSize(100,100);
                    applet.init();
                }
                if (needDummies) {
                  ((BeansAppletStub)stub).active = true;
                } else initializer.activate(applet);
            } else if (beanContext != null) beanContext.add(result);
        }
        return result;
    }
    public static Object getInstanceOf(Object bean, Class<?> targetType) {
        return bean;
    }
    public static boolean isInstanceOf(Object bean, Class<?> targetType) {
        return Introspector.isSubclass(bean.getClass(), targetType);
    }
    public static boolean isDesignTime() {
        Object value = AppContext.getAppContext().get(DESIGN_TIME);
        return (value instanceof Boolean) && (Boolean) value;
    }
    public static boolean isGuiAvailable() {
        Object value = AppContext.getAppContext().get(GUI_AVAILABLE);
        return (value instanceof Boolean) ? (Boolean) value : !GraphicsEnvironment.isHeadless();
    }
    public static void setDesignTime(boolean isDesignTime)
                        throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPropertiesAccess();
        }
        AppContext.getAppContext().put(DESIGN_TIME, Boolean.valueOf(isDesignTime));
    }
    public static void setGuiAvailable(boolean isGuiAvailable)
                        throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPropertiesAccess();
        }
        AppContext.getAppContext().put(GUI_AVAILABLE, Boolean.valueOf(isGuiAvailable));
    }
}
class ObjectInputStreamWithLoader extends ObjectInputStream
{
    private ClassLoader loader;
    public ObjectInputStreamWithLoader(InputStream in, ClassLoader loader)
            throws IOException, StreamCorruptedException {
        super(in);
        if (loader == null) {
            throw new IllegalArgumentException("Illegal null argument to ObjectInputStreamWithLoader");
        }
        this.loader = loader;
    }
    protected Class resolveClass(ObjectStreamClass classDesc)
        throws IOException, ClassNotFoundException {
        String cname = classDesc.getName();
        return ClassFinder.resolveClass(cname, this.loader);
    }
}
class BeansAppletContext implements AppletContext {
    Applet target;
    Hashtable imageCache = new Hashtable();
    BeansAppletContext(Applet target) {
        this.target = target;
    }
    public AudioClip getAudioClip(URL url) {
        try {
            return (AudioClip) url.getContent();
        } catch (Exception ex) {
            return null;
        }
    }
    public synchronized Image getImage(URL url) {
        Object o = imageCache.get(url);
        if (o != null) {
            return (Image)o;
        }
        try {
            o = url.getContent();
            if (o == null) {
                return null;
            }
            if (o instanceof Image) {
                imageCache.put(url, o);
                return (Image) o;
            }
            Image img = target.createImage((java.awt.image.ImageProducer)o);
            imageCache.put(url, img);
            return img;
        } catch (Exception ex) {
            return null;
        }
    }
    public Applet getApplet(String name) {
        return null;
    }
    public Enumeration getApplets() {
        Vector applets = new Vector();
        applets.addElement(target);
        return applets.elements();
    }
    public void showDocument(URL url) {
    }
    public void showDocument(URL url, String target) {
    }
    public void showStatus(String status) {
    }
    public void setStream(String key, InputStream stream)throws IOException{
    }
    public InputStream getStream(String key){
        return null;
    }
    public Iterator getStreamKeys(){
        return null;
    }
}
class BeansAppletStub implements AppletStub {
    transient boolean active;
    transient Applet target;
    transient AppletContext context;
    transient URL codeBase;
    transient URL docBase;
    BeansAppletStub(Applet target,
                AppletContext context, URL codeBase,
                                URL docBase) {
        this.target = target;
        this.context = context;
        this.codeBase = codeBase;
        this.docBase = docBase;
    }
    public boolean isActive() {
        return active;
    }
    public URL getDocumentBase() {
        return docBase;
    }
    public URL getCodeBase() {
        return codeBase;
    }
    public String getParameter(String name) {
        return null;
    }
    public AppletContext getAppletContext() {
        return context;
    }
    public void appletResize(int width, int height) {
    }
}

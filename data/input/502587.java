final class XPathFactoryFinder {
    private static boolean debug = false;
    private static final int DEFAULT_LINE_LENGTH = 80;
    static {
        try {
            String val = SecuritySupport.getSystemProperty("jaxp.debug");
            debug = val != null && (! "false".equals(val));
        } catch (Exception _) {
            debug = false;
        }
    }
	private static Properties cacheProps = new Properties();
	private static boolean firstTime = true;
    private static void debugPrintln(String msg) {
        if (debug) {
            System.err.println("JAXP: " + msg);
        }
    }
    private final ClassLoader classLoader;
    public XPathFactoryFinder(ClassLoader loader) {
        this.classLoader = loader;
        if( debug ) {
            debugDisplayClassLoader();
        }
    }
    private void debugDisplayClassLoader() {
        try {
            if( classLoader == SecuritySupport.getContextClassLoader() ) {
                debugPrintln("using thread context class loader ("+classLoader+") for search");
                return;
            }
        }
        catch (VirtualMachineError vme) {
            throw vme;
        }
        catch (ThreadDeath td) {
            throw td;
        }
        catch (Throwable _) {
            ; 
        }
        if( classLoader==ClassLoader.getSystemClassLoader() ) {
            debugPrintln("using system class loader ("+classLoader+") for search");
            return;
        }
        debugPrintln("using class loader ("+classLoader+") for search");
    }
    public XPathFactory newFactory(String uri) {
        if(uri==null)        throw new NullPointerException();
        XPathFactory f = _newFactory(uri);
        if (debug) {
            if (f != null) {
                debugPrintln("factory '" + f.getClass().getName() + "' was found for " + uri);
            } else {
                debugPrintln("unable to find a factory for " + uri);
            }
        }
        return f;
    }
    private XPathFactory _newFactory(String uri) {
        XPathFactory xpf;
        String propertyName = SERVICE_CLASS.getName() + ":" + uri;
        try {
            if (debug) debugPrintln("Looking up system property '"+propertyName+"'" );
            String r = SecuritySupport.getSystemProperty(propertyName);
            if (r != null && r.length() > 0) {
                if (debug) debugPrintln("The value is '"+r+"'");
                xpf = createInstance(r);
                if(xpf!=null)    return xpf;
            } 
            else if (debug) {
                debugPrintln("The property is undefined.");
            }
        }
        catch (VirtualMachineError vme) {
            throw vme;
        }
        catch (ThreadDeath td) {
            throw td;
        }
        catch (Throwable t) {
            if( debug ) {
                debugPrintln("failed to look up system property '"+propertyName+"'" );
                t.printStackTrace();
            }
        }
        String javah = SecuritySupport.getSystemProperty( "java.home" );
        String configFile = javah + File.separator +
        "lib" + File.separator + "jaxp.properties";
        String factoryClassName = null ;
        try {
            if(firstTime){
                synchronized(cacheProps){
                    if(firstTime){
                        File f=new File( configFile );
                        firstTime = false;
                        if(SecuritySupport.doesFileExist(f)){
                            if (debug) debugPrintln("Read properties file " + f);                                
                            cacheProps.load(SecuritySupport.getFileInputStream(f));
                        }
                    }
                }
            }
            factoryClassName = cacheProps.getProperty(propertyName);            
            if (debug) debugPrintln("found " + factoryClassName + " in $java.home/jaxp.properties"); 
            if (factoryClassName != null) {
                xpf = createInstance(factoryClassName);
                if(xpf != null){
                    return xpf;
                }
            }
        } catch (Exception ex) {
            if (debug) {
                ex.printStackTrace();
            } 
        }
        Iterator sitr = createServiceFileIterator();
        while(sitr.hasNext()) {
            URL resource = (URL)sitr.next();
            if (debug) debugPrintln("looking into " + resource);
            try {
                xpf = loadFromServicesFile(uri, resource.toExternalForm(), SecuritySupport.getURLInputStream(resource));
                if(xpf!=null)    return xpf;
            } catch(IOException e) {
                if( debug ) {
                    debugPrintln("failed to read "+resource);
                    e.printStackTrace();
                }
            }
        }
        if(uri.equals(XPathFactory.DEFAULT_OBJECT_MODEL_URI)) {
            if (debug) debugPrintln("attempting to use the platform default W3C DOM XPath lib");
            return createInstance("org.apache.xpath.jaxp.XPathFactoryImpl");
        }
        if (debug) debugPrintln("all things were tried, but none was found. bailing out.");
        return null;
    }
    XPathFactory createInstance( String className ) {
        try {
            if (debug) debugPrintln("instanciating "+className);
            Class clazz;
            if( classLoader!=null )
                clazz = classLoader.loadClass(className);
            else
                clazz = Class.forName(className);
            if(debug)       debugPrintln("loaded it from "+which(clazz));
            Object o = clazz.newInstance();
            if( o instanceof XPathFactory )
                return (XPathFactory)o;
            if (debug) debugPrintln(className+" is not assignable to "+SERVICE_CLASS.getName());
        }
        catch (VirtualMachineError vme) {
            throw vme;
        }
        catch (ThreadDeath td) {
            throw td;
        }
        catch (Throwable t) {
            if (debug) {
                debugPrintln("failed to instanciate "+className);
                t.printStackTrace();
            }
        }
        return null;
    }
    private static abstract class SingleIterator implements Iterator {
        private boolean seen = false;
        public final void remove() { throw new UnsupportedOperationException(); }
        public final boolean hasNext() { return !seen; }
        public final Object next() {
            if(seen)    throw new NoSuchElementException();
            seen = true;
            return value();
        }
        protected abstract Object value();
    }
    private XPathFactory loadFromServicesFile(String uri, String resourceName, InputStream in) {
        if (debug) debugPrintln("Reading " + resourceName );
        BufferedReader rd;
        try {
            rd = new BufferedReader(new InputStreamReader(in, "UTF-8"), DEFAULT_LINE_LENGTH);
        } catch (java.io.UnsupportedEncodingException e) {
            rd = new BufferedReader(new InputStreamReader(in), DEFAULT_LINE_LENGTH);
        }
        String factoryClassName = null;
        XPathFactory resultFactory = null;
        while (true) {
            try {
                factoryClassName = rd.readLine();   
            } catch (IOException x) {
                break;
            }
            if (factoryClassName != null) {
                int hashIndex = factoryClassName.indexOf('#');
                if (hashIndex != -1) {
                    factoryClassName = factoryClassName.substring(0, hashIndex);
                }
                factoryClassName = factoryClassName.trim();
                if (factoryClassName.length() == 0) {
                    continue;
                }
                try {
                    XPathFactory foundFactory = (XPathFactory) createInstance(factoryClassName);
                    if (foundFactory.isObjectModelSupported(uri)) {
                        resultFactory = foundFactory;
                        break;
                    }
                }
                catch (Exception e) {}
            }
            else {
                break;
            }
        }
        try {
            rd.close();
        }
        catch (IOException exc) {}
        return resultFactory;
    }
    private Iterator createServiceFileIterator() {
        if (classLoader == null) {
            return new SingleIterator() {
                protected Object value() {
                    ClassLoader classLoader = XPathFactoryFinder.class.getClassLoader();
                    return SecuritySupport.getResourceAsURL(classLoader, SERVICE_ID);
                }
            };
        } else {
            try {
                final Enumeration e = SecuritySupport.getResources(classLoader, SERVICE_ID);
                if (debug && !e.hasMoreElements()) {
                    debugPrintln("no "+SERVICE_ID+" file was found");
                }
                return new Iterator() {
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                    public boolean hasNext() {
                        return e.hasMoreElements();
                    }
                    public Object next() {
                        return e.nextElement();
                    }
                };
            } catch (IOException e) {
                if (debug) {
                    debugPrintln("failed to enumerate resources "+SERVICE_ID);
                    e.printStackTrace();
                }
                return new ArrayList().iterator();  
            }
        }
    }
    private static final Class SERVICE_CLASS = XPathFactory.class;
    private static final String SERVICE_ID = "META-INF/services/" + SERVICE_CLASS.getName();
    private static String which( Class clazz ) {
        return which( clazz.getName(), clazz.getClassLoader() );
    }
    private static String which(String classname, ClassLoader loader) {
        String classnameAsResource = classname.replace('.', '/') + ".class";
        if( loader==null )  loader = ClassLoader.getSystemClassLoader();
        URL it = SecuritySupport.getResourceAsURL(loader, classnameAsResource);
        if (it != null) {
            return it.toString();
        } else {
            return null;
        }
    }
}

final class FactoryFinder {
	private static final String CLASS_NAME = "javax.xml.datatype.FactoryFinder";
    private static boolean debug = false;
	private static Properties cacheProps = new Properties();
	private static boolean firstTime = true;
    private static final int DEFAULT_LINE_LENGTH = 80;
    static {
        try {
            String val = SecuritySupport.getSystemProperty("jaxp.debug");
            debug = val != null && (! "false".equals(val));
        } catch (Exception x) {
            debug = false;
        }
    }
    private FactoryFinder() {}
    private static void debugPrintln(String msg) {
        if (debug) {
            System.err.println(
            	CLASS_NAME
            	+ ":"
            	+ msg);
        }
    }
    private static ClassLoader findClassLoader()
        throws ConfigurationError {
        ClassLoader classLoader;
        classLoader = SecuritySupport.getContextClassLoader();            
        if (debug) debugPrintln(
            "Using context class loader: "
            + classLoader);
        if (classLoader == null) {
            classLoader = FactoryFinder.class.getClassLoader();
            if (debug) debugPrintln(
                "Using the class loader of FactoryFinder: "
                + classLoader);                
        }
        return classLoader;
    }
    static Object newInstance(
    	String className,
        ClassLoader classLoader)
        throws ConfigurationError {
        try {
            Class spiClass;
            if (classLoader == null) {
                spiClass = Class.forName(className);
            } else {
                spiClass = classLoader.loadClass(className);
            }
            if (debug) {
            	debugPrintln("Loaded " + className + " from " + which(spiClass));
            }
            return spiClass.newInstance();
        } catch (ClassNotFoundException x) {
            throw new ConfigurationError(
                "Provider " + className + " not found", x);
        } catch (Exception x) {
            throw new ConfigurationError(
                "Provider " + className + " could not be instantiated: " + x,
                x);
        }
    }
    static Object find(String factoryId, String fallbackClassName)
        throws ConfigurationError {
        ClassLoader classLoader = findClassLoader();
        try {
            String systemProp = SecuritySupport.getSystemProperty(factoryId);
            if (systemProp != null && systemProp.length() > 0) {
                if (debug) debugPrintln("found " + systemProp + " in the system property " + factoryId);
                return newInstance(systemProp, classLoader);
            }
        } catch (SecurityException se) {
        	; 
        }
        try {
            String javah = SecuritySupport.getSystemProperty("java.home");
            String configFile = javah + File.separator + "lib" + File.separator + "jaxp.properties";
			String factoryClassName = null;
			if (firstTime) {
				synchronized (cacheProps) {
					if (firstTime) {
						File f = new File(configFile);
						firstTime = false;
						if (SecuritySupport.doesFileExist(f)) {
							if (debug) debugPrintln("Read properties file " + f);
							cacheProps.load(SecuritySupport.getFileInputStream(f));
						}
					}
				}
			}
			factoryClassName = cacheProps.getProperty(factoryId);
            if (debug) debugPrintln("found " + factoryClassName + " in $java.home/jaxp.properties"); 
			if (factoryClassName != null) {
				return newInstance(factoryClassName, classLoader);
			}
        } catch (Exception ex) {
            if (debug) {
            	ex.printStackTrace();
            } 
        }
        Object provider = findJarServiceProvider(factoryId);
        if (provider != null) {
            return provider;
        }
        if (fallbackClassName == null) {
            throw new ConfigurationError(
                "Provider for " + factoryId + " cannot be found", null);
        }
        if (debug) debugPrintln("loaded from fallback value: " + fallbackClassName);
        return newInstance(fallbackClassName, classLoader);
    }
    private static Object findJarServiceProvider(String factoryId)
        throws ConfigurationError
    {
        String serviceId = "META-INF/services/" + factoryId;
        InputStream is = null;
        ClassLoader cl = SecuritySupport.getContextClassLoader();
        if (cl != null) {
            is = SecuritySupport.getResourceAsStream(cl, serviceId);
            if (is == null) {
                cl = FactoryFinder.class.getClassLoader();
                is = SecuritySupport.getResourceAsStream(cl, serviceId);
            }
        } else {
            cl = FactoryFinder.class.getClassLoader();
            is = SecuritySupport.getResourceAsStream(cl, serviceId);
        }
        if (is == null) {
            return null;
        }
        if (debug) debugPrintln("found jar resource=" + serviceId +
               " using ClassLoader: " + cl);
        BufferedReader rd;
        try {
            rd = new BufferedReader(new InputStreamReader(is, "UTF-8"), DEFAULT_LINE_LENGTH);
        } catch (java.io.UnsupportedEncodingException e) {
            rd = new BufferedReader(new InputStreamReader(is), DEFAULT_LINE_LENGTH);
        }
        String factoryClassName = null;
        try {
            factoryClassName = rd.readLine();
        } 
        catch (IOException x) {
            return null;
        }
        finally {
            try { 
                rd.close(); 
            } 
            catch (IOException exc) {}
        }
        if (factoryClassName != null &&
            ! "".equals(factoryClassName)) {
            if (debug) debugPrintln("found in resource, value="
                   + factoryClassName);
            return newInstance(factoryClassName, cl);
        }
        return null;
    }
    static class ConfigurationError extends Error {
        private static final long serialVersionUID = -3644413026244211347L;
        private Exception exception;
        ConfigurationError(String msg, Exception x) {
            super(msg);
            this.exception = x;
        }
        Exception getException() {
            return exception;
        }
    }
    private static String which(Class clazz) {
        try {
            String classnameAsResource = clazz.getName().replace('.', '/') + ".class";
            ClassLoader loader = clazz.getClassLoader();
            URL it;
            if (loader != null) {
            	it = loader.getResource(classnameAsResource);
            } else {
            	it = ClassLoader.getSystemResource(classnameAsResource);
            } 
            if (it != null) {
            	return it.toString();
            } 
        }
        catch (VirtualMachineError vme) {
            throw vme;
        }
        catch (ThreadDeath td) {
            throw td;
        }
        catch (Throwable t) {
            if (debug) {
            	t.printStackTrace();
            } 
        }
        return "unknown location";
    }
}

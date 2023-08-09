abstract class Property
{
    public static boolean toBoolean (final String value)
    {
        if (value == null)
            return false;
        else
            return value.startsWith ("t") || value.startsWith ("y");
    }
    public static Properties combine (final Properties overrides, final Properties base)
    {
        if (base == null)
        {
            if (overrides == null)
                return new XProperties ();
            else
                return overrides;
        }
        if (overrides == null) return base;
        final Properties result = new XProperties (base);
        for (Enumeration overrideNames = overrides.propertyNames (); overrideNames.hasMoreElements (); )
        {
            final String n = (String) overrideNames.nextElement ();
            final String v = overrides.getProperty (n);
            result.setProperty (n, v);
        }
        return result;
    }
    public static Properties getAppProperties (final String namespace, final ClassLoader loader)
    {
        if (namespace == null)
            throw new IllegalArgumentException ("null properties: appNameLC");
        final Properties appDefaults = Property.getProperties (namespace + "_default.properties", loader);
        final Properties systemFileOverrides;
        {
            final String fileName = Property.getSystemProperty (namespace + ".properties");
            final File file = fileName != null
                ? new File (fileName)
                : null;
            systemFileOverrides = Property.getLazyPropertiesFromFile (file);
        }
        final Properties systemOverrides = Property.getSystemProperties (namespace);
        final Properties resOverrides = Property.getProperties (namespace + ".properties", loader);
        return combine (resOverrides,
               combine (systemOverrides,
               combine (systemFileOverrides,
                        appDefaults)));
    }
    public static Properties getSystemProperties (final String systemPrefix)
    {
        Properties result = s_systemProperties;
        if (result == null)
        {
            result = new SystemPropertyLookup (systemPrefix);
            s_systemProperties = result;
            return result;
        }
        return result;
    }
    public static Properties getSystemPropertyRedirects (final Map systemRedirects)
    {
        Properties result = s_systemRedirects;
        if (result == null)
        {
            result = new SystemRedirectsLookup (systemRedirects);
            s_systemRedirects = result;
            return result;
        }
        return result;
    }
    public static String getSystemFingerprint ()
    {
        if (s_systemFingerprint != null)
            return s_systemFingerprint;
        else
        {
            final StringBuffer s = new StringBuffer ();
            final char delimiter = ':';
            s.append (getSystemProperty ("java.vm.name", ""));
            s.append (delimiter);
            s.append (getSystemProperty ("java.vm.version", ""));
            s.append (delimiter);
            s.append (getSystemProperty ("java.vm.vendor", ""));
            s.append (delimiter);
            s.append (getSystemProperty ("os.name", ""));
            s.append (delimiter);
            s.append (getSystemProperty ("os.version", ""));
            s.append (delimiter);
            s.append (getSystemProperty ("os.arch", ""));
            s_systemFingerprint = s.toString ();
            return s_systemFingerprint;
        }
    }    
    public static String getSystemProperty (final String key)
    {
        try
        {
            return System.getProperty (key);
        }
        catch (SecurityException se)
        {
            return null;
        }
    }
    public static String getSystemProperty (final String key, final String def)
    {
        try
        {
            return System.getProperty (key, def);
        }
        catch (SecurityException se)
        {
            return def;
        }
    }
    public static Properties getProperties (final String name)
    {
        Properties result = null;
        InputStream in = null;
        try
        {
            in = ResourceLoader.getResourceAsStream (name);
            if (in != null)
            {
                result = new XProperties ();
                result.load (in);
            }
        }
        catch (Throwable t)
        {
            result = null;
        }
        finally
        {
            if (in != null) try { in.close (); } catch (Throwable ignore) {}
            in = null;
        }
        return result;
    }
    public static Properties getProperties (final String name, final ClassLoader loader)
    {
        Properties result = null;
        InputStream in = null;
        try
        {
            in = ResourceLoader.getResourceAsStream (name, loader);
            if (in != null)
            {
                result = new XProperties ();
                result.load (in);
            }
        }
        catch (Throwable t)
        {
            result = null;
        }
        finally
        {
            if (in != null) try { in.close (); } catch (Throwable ignore) {}
            in = null;
        }
        return result;
    }
    public static Properties getPropertiesFromFile (final File file)
        throws IOException
    {
        if (file == null)
            throw new IllegalArgumentException ("null input: file");
        Properties result = null;
        InputStream in = null;
        try
        {
            in = new BufferedInputStream (new FileInputStream (file), 8 * 1024);
            result = new XProperties ();
            result.load (in);
        }
        finally
        {
            if (in != null) try { in.close (); } catch (Throwable ignore) {}
            in = null;
        }
        return result;
    }    
    public static Properties getLazyPropertiesFromFile (final File file)
    {
        return new FilePropertyLookup (file);
    }
    private static final class FilePropertyLookup extends XProperties
    {
        public String getProperty (final String key)
        {
            faultContents ();
            return m_contents.getProperty (key);
        }
        public Object get (final Object key)
        {
            faultContents ();
            return m_contents.get (key);
        }
        public Enumeration keys ()
        {
            faultContents ();
            return m_contents.keys ();
        }
        FilePropertyLookup (final File src)
        {
            m_src = src;
        }
        private synchronized void faultContents ()
        {
            Properties contents = m_contents;
            if ((contents == null) && (m_src != null))
            {
                try
                {
                    contents = getPropertiesFromFile (m_src);
                }
                catch (RuntimeException re)
                {
                    throw re; 
                }
                catch (Exception e)
                {
                    throw new RuntimeException ("exception while processing properties file [" + m_src.getAbsolutePath () + "]: " + e);
                }
            }
            if (contents == null)
            {
                contents = new XProperties (); 
            }
            m_contents = contents;
        }
        private final File m_src; 
        private Properties m_contents; 
    } 
    private static final class SystemPropertyLookup extends XProperties
    {
        public String getProperty (final String key)
        {
            return (String) get (key);
        }
        public Object get (final Object key)
        {
            if (! (key instanceof String)) return null;
            String result = (String) super.get (key);
            if (result != null) return result;
            if (m_systemPrefix != null)
            {
                result = getSystemProperty (m_systemPrefix.concat ((String) key), null);
                if (result != null) return result;
            }
            return result;
        }
        public synchronized Enumeration keys ()
        {
            final Hashtable _propertyNames = new Hashtable ();
            if (m_systemPrefix != null)
            {
                try
                {
                    final int systemPrefixLength = m_systemPrefix.length ();
                    for (Enumeration e = System.getProperties ().propertyNames ();
                         e.hasMoreElements (); )
                    {
                        final String n = (String) e.nextElement ();
                        if (n.startsWith (m_systemPrefix))
                        {
                            final String yn = n.substring (systemPrefixLength);
                            _propertyNames.put (yn, yn);
                        }
                    } 
                }
                catch (SecurityException ignore)
                {
                    ignore.printStackTrace (System.out);
                }
            }
            return _propertyNames.keys ();
        }
        SystemPropertyLookup (String systemPrefix)
        {
            if ((systemPrefix != null) && ! systemPrefix.endsWith ("."))
                systemPrefix = systemPrefix.concat (".");
            m_systemPrefix = systemPrefix;
        }
        private final String m_systemPrefix; 
    } 
    private static final class SystemRedirectsLookup extends XProperties
    {
        public String getProperty (final String key)
        {
            return (String) get (key);
        }
        public Object get (final Object key)
        {
            if (! (key instanceof String)) return null;
            String result = (String) super.get (key);
            if (result != null) return result;
            if (m_systemRedirects != null)
            {
                final String redirect = (String) m_systemRedirects.get (key);
                if (redirect != null)
                {
                    result = getSystemProperty (redirect, null);
                    if (result != null) return result;
                }
            }
            return result;
        }
        public synchronized Enumeration keys ()
        {
            final Hashtable _propertyNames = new Hashtable ();
            if (m_systemRedirects != null)
            {
                for (Iterator i = m_systemRedirects.keySet ().iterator ();
                     i.hasNext (); )
                {
                    final Object key = i.next ();                    
                    if (key != null) _propertyNames.put (key , key);
                }
            }
            return _propertyNames.keys ();
        }
        SystemRedirectsLookup (final Map systemRedirects)
        {
            m_systemRedirects = systemRedirects; 
        }
        private final Map m_systemRedirects; 
    } 
    private static String s_systemFingerprint;
    private static Properties s_systemProperties, s_systemRedirects;
} 

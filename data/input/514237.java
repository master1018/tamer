    abstract class Factory
    {
        public static IProperties create (final IMapper mapper)
        {
            return new PropertiesImpl (null, mapper);
        }
        public static IProperties wrap (final Properties properties, final IMapper mapper)
        {
            final HashMap map = new HashMap ();
            for (Enumeration names = properties.propertyNames (); names.hasMoreElements (); )
            {
                final String n = (String) names.nextElement ();
                final String v = properties.getProperty (n);
                map.put (n, v);
            }
            return new PropertiesImpl (map, mapper); 
        }
        public static IProperties combine (final IProperties overrides, final IProperties base)
        {
            final IProperties result = overrides != null
                ? overrides.copy ()
                : create (null);
            ((PropertiesImpl) result).getLastProperties ().setDelegate ((PropertiesImpl) base);
            return result;
        }
        private static final class PropertiesImpl implements IProperties, Cloneable
        {
            public String getProperty (final String key)
            {
                return getProperty (key, null);
            }
            public String getProperty (final String key, final String dflt)
            {
                String value = (String) m_valueMap.get (key);
                if ((value == null) && (m_mapper != null))
                {
                    final String mappedKey = m_mapper.getMappedKey (key);
                    if (mappedKey != null)
                        value = (String) m_valueMap.get (mappedKey); 
                }
                if ((value == null) && (m_delegate != null))
                {
                    value = m_delegate.getProperty (key, null); 
                }
                return value != null ? value : dflt;
            }
            public boolean isOverridden (final String key)
            {
                return m_valueMap.containsKey (key);
            }
            public IProperties copy ()
            {
                final PropertiesImpl _clone;
                try
                {
                    _clone = (PropertiesImpl) super.clone ();
                }
                catch (CloneNotSupportedException cnse)
                {
                    throw new Error (cnse.toString ()); 
                }
                _clone.m_valueMap = (HashMap) m_valueMap.clone ();
                _clone.m_unmappedKeySet = null;
                PropertiesImpl scan = _clone;
                for (PropertiesImpl delegate = m_delegate; delegate != null; delegate = delegate.m_delegate)
                {
                    final PropertiesImpl _delegateClone;
                    try
                    {
                        _delegateClone = (PropertiesImpl) delegate.clone ();
                    }
                    catch (CloneNotSupportedException cnse)
                    {
                        throw new Error (cnse.toString ()); 
                    }
                    _delegateClone.m_valueMap = (HashMap) delegate.m_valueMap.clone ();
                    _delegateClone.m_unmappedKeySet = null; 
                    scan.setDelegate (_delegateClone);
                    scan = _delegateClone;
                }
                return _clone;
            }
            public Iterator  properties ()
            {
                return unmappedKeySet ().iterator ();
            }
            public Properties toProperties ()
            {
                final Properties result = new Properties ();
                for (Iterator i = properties (); i.hasNext (); )
                {
                    final String n = (String) i.next ();
                    final String v = getProperty (n);
                    result.setProperty (n, v);
                }
                return result;
            }
            public boolean isEmpty ()
            {
                return m_valueMap.isEmpty () && ((m_delegate == null) || ((m_delegate != null) && m_delegate.isEmpty ()));
            }
            public String [] toAppArgsForm (final String prefix)
            {
                if (isEmpty ())
                    return IConstants.EMPTY_STRING_ARRAY;
                else
                {
                    if (prefix == null)
                        throw new IllegalArgumentException ("null input: prefix");
                    final List _result = new ArrayList ();
                    for (Iterator names = properties (); names.hasNext (); )
                    {
                        final String name = (String) names.next ();
                        final String value = getProperty (name, "");
                        _result.add (prefix.concat (name).concat ("=").concat (value)); 
                    }
                    final String [] result = new String [_result.size ()];
                    _result.toArray (result);
                    return result;
                }
            }    
            public void list (final PrintStream out)
            {
                if (out != null)
                {
                    for (Iterator i = properties (); i.hasNext (); )
                    {
                        final String n = (String) i.next ();
                        final String v = getProperty (n);
                        out.println (n + ":\t[" + v + "]");
                    }
                }
            }
            public void list (final PrintWriter out)
            {
                if (out != null)
                {
                    for (Iterator i = properties (); i.hasNext (); )
                    {
                        final String n = (String) i.next ();
                        final String v = getProperty (n);
                        out.println (n + ":\t[" + v + "]");
                    }
                }
            }
            public String setProperty (final String key, final String value)
            {
                if (value == null)
                    throw new IllegalArgumentException ("null input: value");
                m_unmappedKeySet = null;
                return (String) m_valueMap.put (key, value); 
            }
            PropertiesImpl (final HashMap values, final IMapper mapper)
            {
                m_mapper = mapper;
                m_valueMap = values != null ? values : new HashMap ();
                m_delegate = null;
            }
            Set unmappedKeySet ()
            {
                Set result = m_unmappedKeySet;
                if (result == null)
                {
                    result = new TreeSet ();
                    result.addAll (m_valueMap.keySet ());
                    if (m_delegate != null)
                        result.addAll (m_delegate.unmappedKeySet ());
                    m_unmappedKeySet = result;
                    return result;
                }
                return result;
            }
            PropertiesImpl getLastProperties ()
            {
                PropertiesImpl result = this;
                for (PropertiesImpl delegate = m_delegate; delegate != null; delegate = delegate.m_delegate)
                {
                    if (delegate == this)
                        throw new IllegalStateException ("cyclic delegation detected");
                    result = delegate;
                }
                return result;
            }
            void setDelegate (final PropertiesImpl delegate)
            {
                m_delegate = delegate;
                m_unmappedKeySet = null;
            }
            private final IMapper m_mapper;
            private  HashMap m_valueMap; 
            private PropertiesImpl m_delegate;
            private transient Set m_unmappedKeySet;
        } 
    } 
} 

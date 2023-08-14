class GenericCfg
{
    public GenericCfg (final Task task)
    {
        if (task == null) throw new IllegalArgumentException ("null input: task");
        m_task = task;
        m_genericPropertyElements = new ArrayList ();
    }
    public void setProperties (final File file)
    {
        m_settingsFile = file; 
    }
    public PropertyElement createProperty ()
    {
        m_genericSettings = null;
        final PropertyElement property = new PropertyElement ();
        m_genericPropertyElements.add (property);
        return property;
    }
    public IProperties getFileSettings ()
    {
        IProperties fileSettings = m_fileSettings;
        if ((fileSettings == null) && (m_settingsFile != null))
        {
            try
            {
                fileSettings = EMMAProperties.wrap (Property.getPropertiesFromFile (m_settingsFile));
            }
            catch (IOException ioe)
            {
                throw (BuildException) SuppressableTask.newBuildException (m_task.getTaskName ()
                    + ": property file [" + m_settingsFile.getAbsolutePath () + "] could not be read" , ioe, m_task.getLocation ()).fillInStackTrace ();
            }
            m_fileSettings = fileSettings;
            return fileSettings;
        }
        return fileSettings;
    }
    public IProperties getGenericSettings ()
    {
        IProperties genericSettings = m_genericSettings;
        if (genericSettings == null)
        {
            genericSettings = EMMAProperties.wrap (new Properties ());
            for (Iterator i = m_genericPropertyElements.iterator (); i.hasNext (); )
            {
                final PropertyElement property = (PropertyElement) i.next ();
                final String name = property.getName ();
                String value = property.getValue ();
                if (value == null) value = "";
                if (name != null)
                {
                    final String currentValue = genericSettings.getProperty (name);  
                    if ((currentValue != null) && ! value.equals (currentValue))
                    {
                        throw (BuildException) SuppressableTask.newBuildException (m_task.getTaskName ()
                            + ": conflicting settings for property [" + name + "]: [" + value + "]" , m_task.getLocation ()).fillInStackTrace ();
                    }
                    else
                    {
                        genericSettings.setProperty (name, value);
                    }
                }
            }
            m_genericSettings = genericSettings;
            return genericSettings;
        }
        return genericSettings;
    }
    private final Task m_task;
    private final List  m_genericPropertyElements; 
    private File m_settingsFile; 
    private transient IProperties m_fileSettings; 
    private transient IProperties m_genericSettings; 
} 

abstract class NestedTask extends SuppressableTask
{
    protected NestedTask (final SuppressableTask parent)
    {
        if (parent == null)
            throw new IllegalArgumentException ("null input: parent");
        m_parent = parent;
    }
    protected final IProperties getTaskSettings ()
    {
        final IProperties parentSettings = m_parent != null
            ? m_parent.getTaskSettings ()
            : null;
        final IProperties taskOverrides = super.getTaskSettings ();
        if (parentSettings == null)
            return taskOverrides;
        else
        {
            final IProperties settings = IProperties.Factory.combine (taskOverrides, parentSettings);
            return settings;
        }
    }
    protected final SuppressableTask m_parent;
} 

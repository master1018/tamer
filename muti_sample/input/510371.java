abstract class StringValue
{
    public void appendValue (final String value, final String separator)
    {
        if ((value != null) && (value.length () > 0))
        {
            if (m_value == null)
            {
                m_value = new StringBuffer (value); 
            }
            else
            {
                m_value.append (separator);
                m_value.append (value); 
            }
        }
    }
    public String getValue ()
    {
        return m_value != null ? m_value.toString () : null;  
    }
    protected StringValue (final Task task)
    {
        if (task == null) throw new IllegalArgumentException ("null input: task");
        m_task = task;
    }
    protected final Task m_task;
    private StringBuffer m_value;
} 

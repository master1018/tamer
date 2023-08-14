final class PropertyElement
{
    public PropertyElement ()
    {
    }
    public String getName ()
    {
        return m_name;
    }
    public String getValue ()
    {
        return m_value;
    }
    public void setName (final String name)
    {
        m_name = name;
    }
    public void setValue (final String value)
    {
        m_value = value;
    }
    private String m_name, m_value;
} 

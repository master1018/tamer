public class NameSpace implements Serializable
{
    static final long serialVersionUID = 1471232939184881839L;
  public NameSpace m_next = null;
  public String m_prefix;
  public String m_uri;  
  public NameSpace(String prefix, String uri)
  {
    m_prefix = prefix;
    m_uri = uri;
  }
}

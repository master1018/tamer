public class XBoolean extends XObject
{
    static final long serialVersionUID = -2964933058866100881L;
  public static final XBoolean S_TRUE = new XBooleanStatic(true);
  public static final XBoolean S_FALSE = new XBooleanStatic(false);
  private final boolean m_val;
  public XBoolean(boolean b)
  {
    super();
    m_val = b;
  }
  public XBoolean(Boolean b)
  {
    super();
    m_val = b.booleanValue();
    setObject(b);
  }
  public int getType()
  {
    return CLASS_BOOLEAN;
  }
  public String getTypeString()
  {
    return "#BOOLEAN";
  }
  public double num()
  {
    return m_val ? 1.0 : 0.0;
  }
  public boolean bool()
  {
    return m_val;
  }
  public String str()
  {
    return m_val ? "true" : "false";
  }
  public Object object()
  {
    if(null == m_obj)
      setObject(new Boolean(m_val));
    return m_obj;
  }
  public boolean equals(XObject obj2)
  {
    if (obj2.getType() == XObject.CLASS_NODESET)
      return obj2.equals(this);
    try
    {
      return m_val == obj2.bool();
    }
    catch(javax.xml.transform.TransformerException te)
    {
      throw new org.apache.xml.utils.WrappedRuntimeException(te);
    }
  }
}

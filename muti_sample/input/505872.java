public class ExpandedNameTable
{
  private ExtendedType[] m_extendedTypes;
  private static int m_initialSize = 128;
  private int m_nextType;
  public static final int ELEMENT = ((int)DTM.ELEMENT_NODE) ;
  public static final int ATTRIBUTE = ((int)DTM.ATTRIBUTE_NODE) ;
  public static final int TEXT = ((int)DTM.TEXT_NODE) ;
  public static final int CDATA_SECTION = ((int)DTM.CDATA_SECTION_NODE) ;
  public static final int ENTITY_REFERENCE = ((int)DTM.ENTITY_REFERENCE_NODE) ;
  public static final int ENTITY = ((int)DTM.ENTITY_NODE) ;
  public static final int PROCESSING_INSTRUCTION = ((int)DTM.PROCESSING_INSTRUCTION_NODE) ;
  public static final int COMMENT = ((int)DTM.COMMENT_NODE) ;
  public static final int DOCUMENT = ((int)DTM.DOCUMENT_NODE) ;
  public static final int DOCUMENT_TYPE = ((int)DTM.DOCUMENT_TYPE_NODE) ;
  public static final int DOCUMENT_FRAGMENT =((int)DTM.DOCUMENT_FRAGMENT_NODE) ;
  public static final int NOTATION = ((int)DTM.NOTATION_NODE) ;
  public static final int NAMESPACE = ((int)DTM.NAMESPACE_NODE) ;
  ExtendedType hashET = new ExtendedType(-1, "", "");
  private static ExtendedType[] m_defaultExtendedTypes;
  private static float m_loadFactor = 0.75f;
  private static int m_initialCapacity = 203;
  private int m_capacity;
  private int m_threshold;
  private HashEntry[] m_table;
  static {
    m_defaultExtendedTypes = new ExtendedType[DTM.NTYPES];
    for (int i = 0; i < DTM.NTYPES; i++)
    {
      m_defaultExtendedTypes[i] = new ExtendedType(i, "", "");
    }
  }
  public ExpandedNameTable()
  {
    m_capacity = m_initialCapacity;
    m_threshold = (int)(m_capacity * m_loadFactor);
    m_table = new HashEntry[m_capacity];
    initExtendedTypes();
  }
  private void initExtendedTypes()
  {    
    m_extendedTypes = new ExtendedType[m_initialSize];
    for (int i = 0; i < DTM.NTYPES; i++) {
        m_extendedTypes[i] = m_defaultExtendedTypes[i];
        m_table[i] = new HashEntry(m_defaultExtendedTypes[i], i, i, null);
    }
    m_nextType = DTM.NTYPES;
  }
  public int getExpandedTypeID(String namespace, String localName, int type)
  {
    return getExpandedTypeID(namespace, localName, type, false);
  }
  public int getExpandedTypeID(String namespace, String localName, int type, boolean searchOnly)
  {
    if (null == namespace)
      namespace = "";
    if (null == localName)
      localName = "";
    int hash = type + namespace.hashCode() + localName.hashCode();
    hashET.redefine(type, namespace, localName, hash);
    int index = hash % m_capacity;
    if (index < 0)
      index = -index;
    for (HashEntry e = m_table[index]; e != null; e = e.next)
    {
      if (e.hash == hash && e.key.equals(hashET))
        return e.value;
    }
    if (searchOnly)
    {
      return DTM.NULL;
    }
    if (m_nextType > m_threshold) {
      rehash();
      index = hash % m_capacity;
      if (index < 0)
        index = -index;
    }
    ExtendedType newET = new ExtendedType(type, namespace, localName, hash);
    if (m_extendedTypes.length == m_nextType) {
        ExtendedType[] newArray = new ExtendedType[m_extendedTypes.length * 2];
        System.arraycopy(m_extendedTypes, 0, newArray, 0,
                         m_extendedTypes.length);
        m_extendedTypes = newArray;
    }
    m_extendedTypes[m_nextType] = newET;
    HashEntry entry = new HashEntry(newET, m_nextType, hash, m_table[index]);
    m_table[index] = entry;
    return m_nextType++;
  }
  private void rehash()
  {
    int oldCapacity = m_capacity;
    HashEntry[] oldTable = m_table;
    int newCapacity = 2 * oldCapacity + 1;
    m_capacity = newCapacity;
    m_threshold = (int)(newCapacity * m_loadFactor);
    m_table = new HashEntry[newCapacity];
    for (int i = oldCapacity-1; i >=0 ; i--)
    {
      for (HashEntry old = oldTable[i]; old != null; )
      {
        HashEntry e = old;
        old = old.next;
        int newIndex = e.hash % newCapacity;
        if (newIndex < 0)
          newIndex = -newIndex;
        e.next = m_table[newIndex];
        m_table[newIndex] = e;
      }
    }
  }
  public int getExpandedTypeID(int type)
  {
    return type;
  }
  public String getLocalName(int ExpandedNameID)
  {
    return m_extendedTypes[ExpandedNameID].getLocalName();
  }
  public final int getLocalNameID(int ExpandedNameID)
  {
    if (m_extendedTypes[ExpandedNameID].getLocalName().equals(""))
      return 0;
    else
    return ExpandedNameID;
  }
  public String getNamespace(int ExpandedNameID)
  {
    String namespace = m_extendedTypes[ExpandedNameID].getNamespace();
    return (namespace.equals("") ? null : namespace);
  }
  public final int getNamespaceID(int ExpandedNameID)
  {
    if (m_extendedTypes[ExpandedNameID].getNamespace().equals(""))
      return 0;
    else
    return ExpandedNameID;
  }
  public final short getType(int ExpandedNameID)
  {
    return (short)m_extendedTypes[ExpandedNameID].getNodeType();
  }
  public int getSize()
  {
    return m_nextType;
  }
  public ExtendedType[] getExtendedTypes()
  {
    return m_extendedTypes;
  }
  private static final class HashEntry
  {
    ExtendedType key;
    int value;
    int hash;
    HashEntry next;
    protected HashEntry(ExtendedType key, int value, int hash, HashEntry next)
    {
      this.key = key;
      this.value = value;
      this.hash = hash;
      this.next = next;
    }
  }
}

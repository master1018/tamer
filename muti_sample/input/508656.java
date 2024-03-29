public class StringToIntTable
{
  public static final int INVALID_KEY = -10000;
  private int m_blocksize;
  private String m_map[];
  private int m_values[];
  private int m_firstFree = 0;
  private int m_mapSize;
  public StringToIntTable()
  {
    m_blocksize = 8;
    m_mapSize = m_blocksize;
    m_map = new String[m_blocksize];
    m_values = new int[m_blocksize];
  }
  public StringToIntTable(int blocksize)
  {
    m_blocksize = blocksize;
    m_mapSize = blocksize;
    m_map = new String[blocksize];
    m_values = new int[m_blocksize];
  }
  public final int getLength()
  {
    return m_firstFree;
  }
  public final void put(String key, int value)
  {
    if ((m_firstFree + 1) >= m_mapSize)
    {
      m_mapSize += m_blocksize;
      String newMap[] = new String[m_mapSize];
      System.arraycopy(m_map, 0, newMap, 0, m_firstFree + 1);
      m_map = newMap;
      int newValues[] = new int[m_mapSize];
      System.arraycopy(m_values, 0, newValues, 0, m_firstFree + 1);
      m_values = newValues;
    }
    m_map[m_firstFree] = key;
    m_values[m_firstFree] = value;
    m_firstFree++;
  }
  public final int get(String key)
  {
    for (int i = 0; i < m_firstFree; i++)
    {
      if (m_map[i].equals(key))
        return m_values[i];
    }
	return INVALID_KEY;
  }
  public final int getIgnoreCase(String key)
  {
    if (null == key)
        return INVALID_KEY;
    for (int i = 0; i < m_firstFree; i++)
    {
      if (m_map[i].equalsIgnoreCase(key))
        return m_values[i];
    }
    return INVALID_KEY;
  }
  public final boolean contains(String key)
  {
    for (int i = 0; i < m_firstFree; i++)
    {
      if (m_map[i].equals(key))
        return true;
    }
    return false;
  }
  public final String[] keys()
  {
    String [] keysArr = new String[m_firstFree];
    for (int i = 0; i < m_firstFree; i++)
    {
      keysArr[i] = m_map[i];
    }
    return keysArr;
  }  
}

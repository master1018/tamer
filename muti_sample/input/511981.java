public final class CollationKey implements Comparable
{ 
  public int compareTo(CollationKey target)
  {
    byte tgtbytes[] = target.m_bytes_;
    if (m_bytes_ == null || m_bytes_.length == 0) {
      if (tgtbytes == null || tgtbytes.length == 0) {
        return Collator.RESULT_EQUAL;
      }
      return Collator.RESULT_LESS;
    }
    else {
      if (tgtbytes == null || tgtbytes.length == 0) {
        return Collator.RESULT_GREATER;
      }
    }
    int count = m_bytes_.length;
    if (tgtbytes.length < count) {
      count = tgtbytes.length;
    }
    int s,
        t;
    for (int i = 0; i < count; i ++)
    {
      s = m_bytes_[i] & UNSIGNED_BYTE_MASK_;
      t = tgtbytes[i] & UNSIGNED_BYTE_MASK_;
      if (s < t) {
        return Collator.RESULT_LESS;
      }
      if (s > t) {
        return Collator.RESULT_GREATER;
      }
    }
    if (m_bytes_.length < target.m_bytes_.length) {
      return Collator.RESULT_LESS;
    }
    if (m_bytes_.length > target.m_bytes_.length) {
      return Collator.RESULT_GREATER;
    }
    return Collator.RESULT_EQUAL;
  }
  public int compareTo(Object target)
  {
    return compareTo((CollationKey)target);
  }
  public boolean equals(Object target)
  {
    if (this == target) {
      return true;
    }
    if (target == null || target.getClass() != getClass()) {
      return false;
    }
    return compareTo((CollationKey)target) == Collator.RESULT_EQUAL;
  }
  public int hashCode()
  {
    if (m_hash_ == 0)
    {
      if (m_bytes_ != null && m_bytes_.length != 0)
      {                        
        int len = m_bytes_.length;
        int inc = ((len - 32) / 32) + 1;  
        for (int i = 0; i < len;)
        {
          m_hash_ = (m_hash_ * 37) + m_bytes_[i];
          i += inc;                         
        }                                     
      }             
      if (m_hash_ == 0)
        m_hash_ = 1;
    }
    return m_hash_;
  }
  public byte[] toByteArray()
  {
    if (m_bytes_ == null || m_bytes_.length == 0)
      return null;
    return (byte[])m_bytes_.clone(); 
  }
  CollationKey()
  {
    m_hash_ = 0;
  }
  CollationKey(byte[] bytes)
  {
    m_bytes_ = bytes;
    m_hash_ = 0;
  }
  private byte m_bytes_[];
  private static final int UNSIGNED_BYTE_MASK_ = 0x00FF;
  private int m_hash_;
}

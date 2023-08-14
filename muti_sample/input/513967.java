public final class CollationElementIterator
{
  public static final int NULLORDER = 0xFFFFFFFF;
  public void reset()
  {
    NativeCollation.reset(m_collelemiterator_);
  }
  public int next()
  {
    return NativeCollation.next(m_collelemiterator_);
  }
  public int previous()
  {
    return NativeCollation.previous(m_collelemiterator_);
  }
  public int getMaxExpansion(int order)
  {
    return NativeCollation.getMaxExpansion(m_collelemiterator_, order);
  }
  public void setText(String source)
  {
    NativeCollation.setText(m_collelemiterator_, source);
  }
  public void setText(CharacterIterator source)
  {
    NativeCollation.setText(m_collelemiterator_, source.toString());
  }
  public int getOffset()
  {
    return NativeCollation.getOffset(m_collelemiterator_);
  }
  public void setOffset(int offset)
  {
    NativeCollation.setOffset(m_collelemiterator_, offset);
  }
  public static int primaryOrder(int order) 
  {
    return ((order & PRIMARY_ORDER_MASK_) >> PRIMARY_ORDER_SHIFT_) &
                                                       UNSIGNED_16_BIT_MASK_;
  }
  public static int secondaryOrder(int order)
  {
    return (order & SECONDARY_ORDER_MASK_) >> SECONDARY_ORDER_SHIFT_;
  }
  public static int tertiaryOrder(int order)
  {
    return order & TERTIARY_ORDER_MASK_;
  }
  CollationElementIterator(int collelemiteratoraddress)
  {
    m_collelemiterator_ = collelemiteratoraddress;
  }
  protected void finalize()
  {
    NativeCollation.closeElements(m_collelemiterator_);
  }
  private int m_collelemiterator_;
  private static final int PRIMARY_ORDER_MASK_ = 0xffff0000;
  private static final int SECONDARY_ORDER_MASK_ = 0x0000ff00;
  private static final int TERTIARY_ORDER_MASK_ = 0x000000ff;
  private static final int PRIMARY_ORDER_SHIFT_ = 16;
  private static final int SECONDARY_ORDER_SHIFT_ = 8;
  private static final int UNSIGNED_16_BIT_MASK_ = 0x0000FFFF;
}

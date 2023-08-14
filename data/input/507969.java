public final class RuleBasedCollator extends Collator 
{
  public RuleBasedCollator(String rules) throws ParseException
  {
    if (rules == null) {
      throw new NullPointerException();
    }
    m_collator_ = NativeCollation.openCollatorFromRules(rules,
                              CollationAttribute.VALUE_OFF,
                              CollationAttribute.VALUE_DEFAULT_STRENGTH);
  }
  public RuleBasedCollator(String rules, int strength) throws ParseException
  {
    if (rules == null) {
      throw new NullPointerException();
    }
    if (!CollationAttribute.checkStrength(strength))
      throw ErrorCode.getException(ErrorCode.U_ILLEGAL_ARGUMENT_ERROR);
    m_collator_ = NativeCollation.openCollatorFromRules(rules,
                                CollationAttribute.VALUE_OFF,
                                strength);
  }
  public RuleBasedCollator(String rules, int normalizationmode, int strength)
  {
    if (rules == null) {
      throw new NullPointerException();
    }
    if (!CollationAttribute.checkStrength(strength) || 
        !CollationAttribute.checkNormalization(normalizationmode)) {
      throw ErrorCode.getException(ErrorCode.U_ILLEGAL_ARGUMENT_ERROR);
    }
    m_collator_ = NativeCollation.openCollatorFromRules(rules,
                                          normalizationmode, strength);
  }
  public Object clone() 
  {
    RuleBasedCollator result = null;
    int collatoraddress = NativeCollation.safeClone(m_collator_);
    result = new RuleBasedCollator(collatoraddress);
    return (Collator)result;
  }
  public int compare(String source, String target)
  {
    return NativeCollation.compare(m_collator_, source, target);
  }
  public int getDecomposition()
  {
    return NativeCollation.getNormalization(m_collator_);
  }
  public void setDecomposition(int decompositionmode)
  {
    if (!CollationAttribute.checkNormalization(decompositionmode)) 
      throw ErrorCode.getException(ErrorCode.U_ILLEGAL_ARGUMENT_ERROR);
    NativeCollation.setAttribute(m_collator_, 
                                 CollationAttribute.NORMALIZATION_MODE,
                                 decompositionmode);
  }
  public int getStrength()
  {
    return NativeCollation.getAttribute(m_collator_, 
                                        CollationAttribute.STRENGTH);
  }
  public void setStrength(int strength)
  {
    if (!CollationAttribute.checkStrength(strength)) 
      throw ErrorCode.getException(ErrorCode.U_ILLEGAL_ARGUMENT_ERROR);
    NativeCollation.setAttribute(m_collator_, CollationAttribute.STRENGTH, 
                                 strength);
  }
  public void setAttribute(int type, int value)
  {
    if (!CollationAttribute.checkAttribute(type, value))
      throw ErrorCode.getException(ErrorCode.U_ILLEGAL_ARGUMENT_ERROR);
    NativeCollation.setAttribute(m_collator_, type, value);
  }
  public int getAttribute(int type)
  {
    if (!CollationAttribute.checkType(type))
      throw ErrorCode.getException(ErrorCode.U_ILLEGAL_ARGUMENT_ERROR);
    return NativeCollation.getAttribute(m_collator_, type);
  }
  public CollationKey getCollationKey(String source)
  {
    if(source == null) {
        return null;
    }
    byte[] key = NativeCollation.getSortKey(m_collator_, source);
    if(key == null) {
      return null;
    }
    return new CollationKey(key);
  }
  public byte[] getSortKey(String source)
  {
    return NativeCollation.getSortKey(m_collator_, source);
  }
  public String getRules()
  {
    return NativeCollation.getRules(m_collator_);
  }
  public CollationElementIterator getCollationElementIterator(String source)
  {
    CollationElementIterator result = new CollationElementIterator(
         NativeCollation.getCollationElementIterator(m_collator_, source));
    return result;
  }
  public CollationElementIterator getCollationElementIterator(
          CharacterIterator source)
  {
    CollationElementIterator result = new CollationElementIterator(
         NativeCollation.getCollationElementIterator(m_collator_, 
                 source.toString()));
    return result;
  }
  public int hashCode()
  {
    if (m_hashcode_ == 0) {
      m_hashcode_ = NativeCollation.hashCode(m_collator_);
      if (m_hashcode_ == 0)
        m_hashcode_ = 1;
    }
    return m_hashcode_;
  }
  public boolean equals(Object target)
  {
    if (this == target) 
      return true;
    if (target == null) 
      return false;
    if (getClass() != target.getClass()) 
      return false;
    RuleBasedCollator tgtcoll = (RuleBasedCollator)target;
    return getRules().equals(tgtcoll.getRules()) && 
           getStrength() == tgtcoll.getStrength() && 
           getDecomposition() == tgtcoll.getDecomposition();
  }
  RuleBasedCollator()
  {
    m_collator_ = NativeCollation.openCollator();
  }
  RuleBasedCollator(Locale locale)
  {
    if (locale == null) {
      m_collator_ = NativeCollation.openCollator();
    }
    else {
      m_collator_ = NativeCollation.openCollator(locale.toString());
    }
  }
  protected void finalize()
  {
    NativeCollation.closeCollator(m_collator_);
  }
  private int m_collator_;
  private int m_hashcode_ = 0;
  private RuleBasedCollator(int collatoraddress)
  {
    m_collator_ = collatoraddress;
  }
}

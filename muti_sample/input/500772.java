public abstract class Collator implements Cloneable
{ 
    public final static int PRIMARY = CollationAttribute.VALUE_PRIMARY;
    public final static int SECONDARY = CollationAttribute.VALUE_SECONDARY;
    public final static int TERTIARY = CollationAttribute.VALUE_TERTIARY;                            
    public final static int QUATERNARY = CollationAttribute.VALUE_QUATERNARY;
    public final static int IDENTICAL = CollationAttribute.VALUE_IDENTICAL;
    public final static int NO_DECOMPOSITION = CollationAttribute.VALUE_OFF;
    public final static int CANONICAL_DECOMPOSITION 
                                                = CollationAttribute.VALUE_ON;
    public static final int RESULT_EQUAL = 0;
    public static final int RESULT_GREATER = 1;
    public static final int RESULT_LESS = -1;
    public static final int RESULT_DEFAULT = -1;
  public static Collator getInstance()
  {
    return getInstance(null);
  }
  public static Collator getInstance(Locale locale)
  {
    RuleBasedCollator result = new RuleBasedCollator(locale);
    return result;
  }
  public boolean equals(String source, String target)
  {
    return (compare(source, target) == RESULT_EQUAL);
  }
  public abstract boolean equals(Object target);
  public abstract Object clone() throws CloneNotSupportedException;
  public abstract int compare(String source, String target);
    public abstract int getDecomposition();
    public abstract void setDecomposition(int mode);
    public abstract int getStrength();
  public abstract int getAttribute(int type);
     public abstract void setStrength(int strength);
  public abstract void setAttribute(int type, int value);
  public abstract CollationKey getCollationKey(String source);
  public abstract int hashCode();
  public static Locale[] getAvailableLocales() {
      String[] locales = NativeCollation.getAvailableLocalesImpl();
      Locale[] result = new Locale[locales.length];
      String locale;
      int index, index2;
      for(int i = 0; i < locales.length; i++) {
          locale = locales[i];
          index = locale.indexOf('_');
          index2 = locale.lastIndexOf('_');
          if(index == -1) {
              result[i] = new Locale(locales[i]);
          } else if(index == 2 && index == index2) {
              result[i] = new Locale(
                      locale.substring(0,2),
                      locale.substring(3,5));
          } else if(index == 2 && index2 > index) {
              result[i] = new Locale(
                      locale.substring(0,index),
                      locale.substring(index + 1,index2),
                      locale.substring(index2 + 1));
          }
      }
      return result;
  }
}

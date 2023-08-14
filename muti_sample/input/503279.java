final class NativeCollation
{
  public NativeCollation() {
  }
  static native int openCollator();
  static native int openCollator(String locale);
  static native int openCollatorFromRules(String rules,
                                           int normalizationmode,
                                           int collationstrength);
  static native void closeCollator(int collatoraddress);
  static native int compare(int collatoraddress, String source, 
                            String target);
  static native int getNormalization(int collatoraddress);
  static native void setNormalization(int collatoraddress, 
                                      int normalizationmode);
  static native String getRules(int collatoraddress);
  static native byte[] getSortKey(int collatoraddress, String source);
  static native void setAttribute(int collatoraddress, int type, int value);
  static native int getAttribute(int collatoraddress, int type);
  static native int safeClone(int collatoraddress);
  static native int getCollationElementIterator(int collatoraddress, 
                                                 String source);
  static native int hashCode(int collatoraddress);
  static native void closeElements(int address);
  static native void reset(int address);
  static native int next(int address);
  static native int previous(int address);
  static native int getMaxExpansion(int address, int order);
  static native void setText(int address, String source);
  static native int getOffset(int address);
  static native void setOffset(int address, int offset);
  static native String[] getAvailableLocalesImpl();
}

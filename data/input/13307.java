abstract public class Enumerated  implements Serializable {
  public Enumerated() throws IllegalArgumentException {
    Enumeration e =getIntTable().keys() ;
    if (e.hasMoreElements()) {
      value = ((Integer)e.nextElement()).intValue() ;
    }
    else {
      throw new IllegalArgumentException() ;
    }
  }
  public Enumerated(int valueIndex) throws IllegalArgumentException {
    if (getIntTable().get(new Integer(valueIndex)) == null) {
      throw new IllegalArgumentException() ;
    }
    value = valueIndex ;
  }
  public Enumerated(Integer valueIndex) throws IllegalArgumentException {
    if (getIntTable().get(valueIndex) == null) {
      throw new IllegalArgumentException() ;
    }
    value = valueIndex.intValue() ;
  }
  public Enumerated(String valueString) throws IllegalArgumentException {
    Integer index = (Integer)getStringTable().get(valueString) ;
    if (index == null) {
      throw new IllegalArgumentException() ;
    }
    else {
      value = index.intValue() ;
    }
  }
  public int intValue() {
    return value ;
  }
  public Enumeration valueIndexes() {
    return getIntTable().keys() ;
  }
  public Enumeration valueStrings() {
    return getStringTable().keys() ;
  }
  public boolean equals(Object obj) {
    return ((obj != null) &&
            (getClass() == obj.getClass()) &&
            (value == ((Enumerated)obj).value)) ;
  }
  public int hashCode() {
    String hashString = getClass().getName() + String.valueOf(value) ;
    return hashString.hashCode() ;
  }
  public String toString() {
    return (String)getIntTable().get(new Integer(value)) ;
  }
  protected abstract Hashtable getIntTable() ;
  protected abstract Hashtable getStringTable() ;
  protected int value ;
}

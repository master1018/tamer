public class symbol_set {
  public symbol_set() { };
  public symbol_set(symbol_set other) throws internal_error
    {
      not_null(other);
      _all = (Hashtable)other._all.clone();
    };
  protected Hashtable _all = new Hashtable(11);
  public Enumeration all() {return _all.elements();};
  public int size() {return _all.size();};
  protected void not_null(Object obj) throws internal_error
    {
      if (obj == null) 
    throw new internal_error("Null object used in set operation");
    }
  public boolean contains(symbol sym) {return _all.containsKey(sym.name());};
  public boolean is_subset_of(symbol_set other) throws internal_error
    {
      not_null(other);
      for (Enumeration e = all(); e.hasMoreElements(); )
    if (!other.contains((symbol)e.nextElement()))
      return false;
      return true;
    }
  public boolean is_superset_of(symbol_set other) throws internal_error
    {
      not_null(other);
      return other.is_subset_of(this);
    }
  public boolean add(symbol sym) throws internal_error
    {
      Object previous;
      not_null(sym); 
      previous = _all.put(sym.name(),sym);
      return previous == null;
    };
  public void remove(symbol sym) throws internal_error
    {
      not_null(sym); 
      _all.remove(sym.name());
    };
  public boolean add(symbol_set other) throws internal_error
    {
      boolean result = false;
      not_null(other);
      for (Enumeration e = other.all(); e.hasMoreElements(); )
    result = add((symbol)e.nextElement()) || result;
      return result;
    };
  public void remove(symbol_set other) throws internal_error
    {
      not_null(other);
      for (Enumeration e = other.all(); e.hasMoreElements(); )
    remove((symbol)e.nextElement());
    };
  public boolean equals(symbol_set other) 
    {
      if (other == null || other.size() != size()) return false;
      try {
        return is_subset_of(other);
      } catch (internal_error e) {
    e.crash();
    return false;
      }
    }
  public boolean equals(Object other)
    {
      if (!(other instanceof symbol_set))
    return false;
      else
    return equals((symbol_set)other);
    }
  public int hashCode()
    {
      int result = 0;
      int cnt;
      Enumeration e;
      for (e = all(), cnt=0 ; e.hasMoreElements() && cnt<5; cnt++)
    result ^= ((symbol)e.nextElement()).hashCode();
      return result;
    }
  public String toString()
    {
      String result;
      boolean comma_flag;
      result = "{";
      comma_flag = false;
      for (Enumeration e = all(); e.hasMoreElements(); )
    {
      if (comma_flag)
        result += ", ";
      else
        comma_flag = true;
      result += ((symbol)e.nextElement()).name();
    }
      result += "}";
      return result;
    }
};

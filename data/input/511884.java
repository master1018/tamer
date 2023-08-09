public class terminal_set {
  public terminal_set() 
    { 
      _elements = new BitSet(terminal.number());
    };
  public terminal_set(terminal_set other) 
    throws internal_error
    {
      not_null(other);
      _elements = (BitSet)other._elements.clone();
    };
  public static final terminal_set EMPTY = new terminal_set();
  protected BitSet _elements;
  protected void not_null(Object obj) throws internal_error
    {
      if (obj == null) 
    throw new internal_error("Null object used in set operation");
    }
  public boolean empty()
    {
      return equals(EMPTY);
    }
  public boolean contains(terminal sym) 
    throws internal_error
    {
      not_null(sym); 
      return _elements.get(sym.index());
    };
  public boolean contains(int indx) 
    {
      return _elements.get(indx);
    };
  public boolean is_subset_of(terminal_set other)
    throws internal_error
    {
      not_null(other);
      BitSet copy_other = (BitSet)other._elements.clone();
      copy_other.or(_elements);
      return copy_other.equals(other._elements);
    }
  public boolean is_superset_of(terminal_set other)
    throws internal_error
    {
      not_null(other);
      return other.is_subset_of(this);
    }
  public boolean add(terminal sym) 
    throws internal_error
    {
      boolean result;
      not_null(sym); 
      result = _elements.get(sym.index());
      if (!result)
    _elements.set(sym.index());
      return result;
    };
  public void remove(terminal sym) 
    throws internal_error
    {
      not_null(sym); 
      _elements.clear(sym.index());
    };
  public boolean add(terminal_set other)
    throws internal_error
    {
      not_null(other);
      BitSet copy = (BitSet)_elements.clone();
      _elements.or(other._elements);
      return !_elements.equals(copy);
    };
   public boolean intersects(terminal_set other)
     throws internal_error
     {
       not_null(other);
       BitSet copy = (BitSet)other._elements.clone();
       copy.xor(this._elements);
       return !copy.equals(other._elements);
     }
  public boolean equals(terminal_set other)
    {
      if (other == null) 
    return false;
      else
    return _elements.equals(other._elements);
    }
  public boolean equals(Object other)
    {
      if (!(other instanceof terminal_set))
    return false;
      else
    return equals((terminal_set)other);
    }
  public String toString()
    {
      String result;
      boolean comma_flag;
      result = "{";
      comma_flag = false;
      for (int t = 0; t < terminal.number(); t++)
    {
      if (_elements.get(t))
        {
          if (comma_flag)
            result += ", ";
          else
            comma_flag = true;
          result += terminal.find(t).name();
        }
    }
      result += "}";
      return result;
    }
};

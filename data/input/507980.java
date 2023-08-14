public class terminal extends symbol {
  public terminal(String nm, String tp) 
    {
      super(nm, tp);
      Object conflict = _all.put(nm,this);
      if (conflict != null)
    (new internal_error("Duplicate terminal (" + nm + ") created")).crash();
      _index = next_index++;
      _all_by_index.put(new Integer(_index), this);
    }
  public terminal(String nm) 
    {
      this(nm, null);
    }
  protected static Hashtable _all = new Hashtable();
  public static Enumeration all() {return _all.elements();};
  public static terminal find(String with_name)
    {
      if (with_name == null)
    return null;
      else 
    return (terminal)_all.get(with_name);
    }
  protected static Hashtable _all_by_index = new Hashtable();
  public static terminal find(int indx)
    {
      Integer the_indx = new Integer(indx);
      return (terminal)_all_by_index.get(the_indx);
    }
  public static int number() {return _all.size();};
  protected static int next_index = 0;
  public static final terminal EOF = new terminal("EOF");
  public static final terminal error = new terminal("error");
  public boolean is_non_term() 
    {
      return false;
    }
  public String toString()
    {
      return super.toString() + "[" + index() + "]";
    }
};

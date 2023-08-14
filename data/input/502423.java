public class non_terminal extends symbol {
  public non_terminal(String nm, String tp) 
    {
      super(nm, tp);
      Object conflict = _all.put(nm,this);
      if (conflict != null)
    (new internal_error("Duplicate non-terminal ("+nm+") created")).crash();
      _index = next_index++;
    }
  public non_terminal(String nm) 
    {
      this(nm, null);
    }
  protected static Hashtable _all = new Hashtable();
  public static Enumeration all() {return _all.elements();};
  public static non_terminal find(String with_name)
    {
      if (with_name == null)
        return null;
      else 
        return (non_terminal)_all.get(with_name);
    }
  public static int number() {return _all.size();};
  protected static int next_index = 0;
  static protected int next_nt = 0;
  public static final non_terminal START_nt = new non_terminal("$START");
  static non_terminal create_new(String prefix) throws internal_error
    {
      if (prefix == null) prefix = "NT$";
      return new non_terminal(prefix + next_nt++);
    }
  static non_terminal create_new() throws internal_error
    { 
      return create_new(null); 
    }
  public static void compute_nullability() throws internal_error
    {
      boolean      change = true;
      non_terminal nt;
      Enumeration  e;
      production   prod;
      while (change)
    {
      change = false;
      for (e=all(); e.hasMoreElements(); )
        {
          nt = (non_terminal)e.nextElement();
          if (!nt.nullable())
        {
          if (nt.looks_nullable())
            {
              nt._nullable = true;
              change = true;
            }
        }
        }
    }
      for (e=production.all(); e.hasMoreElements(); )
    {
      prod = (production)e.nextElement();
      prod.set_nullable(prod.check_nullable());
    }
    }
  public static void compute_first_sets() throws internal_error
    {
      boolean      change = true;
      Enumeration  n;
      Enumeration  p;
      non_terminal nt;
      production   prod;
      terminal_set prod_first;
      while (change)
    {
      change = false;
      for (n = all(); n.hasMoreElements(); )
        {
          nt = (non_terminal)n.nextElement();
          for (p = nt.productions(); p.hasMoreElements(); )
        {
          prod = (production)p.nextElement();
          prod_first = prod.check_first_set();
          if (!prod_first.is_subset_of(nt._first_set))
            {
              change = true;
              nt._first_set.add(prod_first);
            }
        }
        }
    }
    }
  protected Hashtable _productions = new Hashtable(11);
  public Enumeration productions() {return _productions.elements();};
  public int num_productions() {return _productions.size();};
  public void add_production(production prod) throws internal_error
    {
      if (prod == null || prod.lhs() == null || prod.lhs().the_symbol() != this)
    throw new internal_error(
      "Attempt to add invalid production to non terminal production table");
      _productions.put(prod,prod);
    }
  protected boolean _nullable;
  public boolean nullable() {return _nullable;}
  protected terminal_set _first_set = new terminal_set();
  public terminal_set first_set() {return _first_set;}
  public boolean is_non_term() 
    {
      return true;
    }
  protected boolean looks_nullable() throws internal_error
    {
      for (Enumeration e = productions(); e.hasMoreElements(); )
    if (((production)e.nextElement()).check_nullable())
      return true;
      return false;
    }
  public String toString()
    {
      return super.toString() + "[" + index() + "]" + (nullable() ? "*" : "");
    }
};

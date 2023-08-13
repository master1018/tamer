public class lalr_item_set {
  public lalr_item_set() { }
  public lalr_item_set(lalr_item_set other) 
    throws internal_error
    {
      not_null(other);
      _all = (Hashtable)other._all.clone();
    }
  protected Hashtable _all = new Hashtable(11);
  public Enumeration all() {return _all.elements();}
  protected Integer hashcode_cache = null;
  public int size() {return _all.size();}
  public boolean contains(lalr_item itm) {return _all.containsKey(itm);}
  public lalr_item find(lalr_item itm) {return (lalr_item)_all.get(itm);}
  public boolean is_subset_of(lalr_item_set other) throws internal_error
    {
      not_null(other);
      for (Enumeration e = all(); e.hasMoreElements(); )
    if (!other.contains((lalr_item)e.nextElement()))
      return false;
      return true;
    }
  public boolean is_superset_of(lalr_item_set other) throws internal_error
    {
      not_null(other);
      return other.is_subset_of(this);
    }
  public lalr_item add(lalr_item itm) throws internal_error
    {
      lalr_item other;
      not_null(itm); 
      other = (lalr_item)_all.get(itm);
      if (other != null)
    {
      other.lookahead().add(itm.lookahead());
      return other;
    }
      else
    {
          hashcode_cache = null;
          _all.put(itm,itm);
      return itm;
    }
    };
  public void remove(lalr_item itm) throws internal_error
    {
      not_null(itm); 
      hashcode_cache = null;
      _all.remove(itm);
    };
  public void add(lalr_item_set other) throws internal_error
    {
      not_null(other);
      for (Enumeration e = other.all(); e.hasMoreElements(); )
    add((lalr_item)e.nextElement());
    }
  public void remove(lalr_item_set other) throws internal_error
    {
      not_null(other);
      for (Enumeration e = other.all(); e.hasMoreElements(); )
    remove((lalr_item)e.nextElement());
    }
  public lalr_item get_one() throws internal_error
    {
      Enumeration the_set;
      lalr_item result;
      the_set = all();
      if (the_set.hasMoreElements())
    {
          result = (lalr_item)the_set.nextElement();
          remove(result);
      return result;
    }
      else
    return null;
    }
  protected void not_null(Object obj) throws internal_error
    {
      if (obj == null) 
    throw new internal_error("Null object used in set operation");
    }
  public void compute_closure()
    throws internal_error
    {
      lalr_item_set consider;
      lalr_item     itm, new_itm, add_itm;
      non_terminal  nt;
      terminal_set  new_lookaheads;
      Enumeration   p;
      production    prod;
      boolean       need_prop;
      hashcode_cache = null;
      consider = new lalr_item_set(this);
      while (consider.size() > 0)
    {
      itm = consider.get_one(); 
      nt = itm.dot_before_nt();
      if (nt != null)
        {
          new_lookaheads = itm.calc_lookahead(itm.lookahead());
          need_prop = itm.lookahead_visible();
          for (p = nt.productions(); p.hasMoreElements(); )
        {
          prod = (production)p.nextElement();
          new_itm = new lalr_item(prod,new_lookaheads);
          add_itm = add(new_itm);
          if (need_prop)
            itm.add_propagate(add_itm);
          if (add_itm == new_itm)
            {
              consider.add(new_itm);
            } 
        } 
        } 
    } 
    }
  public boolean equals(lalr_item_set other)
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
      if (!(other instanceof lalr_item_set))
    return false;
      else
    return equals((lalr_item_set)other);
    }
  public int hashCode()
    {
      int result = 0;
      Enumeration e;
      int cnt;
      if (hashcode_cache == null)
    {
          for (e = all(), cnt=0 ; e.hasMoreElements() && cnt<5; cnt++)
        result ^= ((lalr_item)e.nextElement()).hashCode();
      hashcode_cache = new Integer(result);
    }
      return hashcode_cache.intValue();
    }
  public String toString()
    {
      StringBuffer result = new StringBuffer();
      result.append("{\n");
      for (Enumeration e=all(); e.hasMoreElements(); ) 
     {
       result.append("  " + (lalr_item)e.nextElement() + "\n");
     }
       result.append("}");
       return result.toString();
    }
};

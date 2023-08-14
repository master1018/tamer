public class lalr_item extends lr_item_core {
  public lalr_item(production prod, int pos, terminal_set look) 
    throws internal_error
    {
      super(prod, pos);
      _lookahead = look;
      _propagate_items = new Stack();
      needs_propagation = true;
    }
  public lalr_item(production prod, terminal_set look) throws internal_error
    {
      this(prod,0,look);
    }
  public lalr_item(production prod) throws internal_error
    {
      this(prod,0,new terminal_set());
    }
  protected terminal_set _lookahead;
  public terminal_set lookahead() {return _lookahead;};
  protected Stack _propagate_items; 
  public Stack propagate_items() {return _propagate_items;}
  protected boolean needs_propagation;
  public void add_propagate(lalr_item prop_to)
    {
      _propagate_items.push(prop_to);
      needs_propagation = true;
    }
  public void propagate_lookaheads(terminal_set incoming) throws internal_error
    {
      boolean change = false;
      if (!needs_propagation && (incoming == null || incoming.empty()))
    return;
      if (incoming != null)
    {
      change = lookahead().add(incoming);
    }
      if (change || needs_propagation)
    {
          needs_propagation = false;
      for (int i = 0; i < propagate_items().size(); i++)
        ((lalr_item)propagate_items().elementAt(i))
                      .propagate_lookaheads(lookahead());
    }
    }
  public lalr_item shift() throws internal_error
    {
      lalr_item result;
      if (dot_at_end())
    throw new internal_error("Attempt to shift past end of an lalr_item");
      result = new lalr_item(the_production(), dot_pos()+1, 
                        new terminal_set(lookahead()));
      add_propagate(result);
      return result;
    }
  public terminal_set calc_lookahead(terminal_set lookahead_after) 
    throws internal_error
    {
      terminal_set    result;
      int             pos;
      production_part part;
      symbol          sym;
      if (dot_at_end())
    throw new internal_error(
      "Attempt to calculate a lookahead set with a completed item");
      result = new terminal_set();
      for (pos = dot_pos()+1; pos < the_production().rhs_length(); pos++) 
    {
       part = the_production().rhs(pos);
       if (!part.is_action())
         {
           sym = ((symbol_part)part).the_symbol();
           if (!sym.is_non_term())
         {
           result.add((terminal)sym);
           return result;
         }
           else
         {
           result.add(((non_terminal)sym).first_set());
           if (!((non_terminal)sym).nullable())
             return result;
         }
         }
    }
      result.add(lookahead_after);
      return result;
    }
  public boolean lookahead_visible() throws internal_error
    {
      production_part part;
      symbol          sym;
      if (dot_at_end()) return true;
      for (int pos = dot_pos() + 1; pos < the_production().rhs_length(); pos++)
    {
      part = the_production().rhs(pos);
      if (!part.is_action())
        {
          sym = ((symbol_part)part).the_symbol();
          if (!sym.is_non_term()) return false;
          if (!((non_terminal)sym).nullable()) return false;
        }
    }
      return true;
    }
  public boolean equals(lalr_item other)
    {
      if (other == null) return false;
      return super.equals(other);
    }
  public boolean equals(Object other)
    {
      if (!(other instanceof lalr_item)) 
    return false;
      else
    return equals((lalr_item)other);
    }
  public int hashCode()
    {
      return super.hashCode();
    }
  public String toString()
    {
      String result = "";
      result += "[";
      result += super.toString();
      result += ", ";
      if (lookahead() != null)
    {
      result += "{";
      for (int t = 0; t < terminal.number(); t++)
        if (lookahead().contains(t))
          result += terminal.find(t).name() + " ";
      result += "}";
    }
      else
    result += "NULL LOOKAHEAD!!";
      result += "]";
      return result;
    }
};

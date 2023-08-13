public class lr_item_core {
  public lr_item_core(production prod, int pos) throws internal_error
    {
      symbol          after_dot = null;
      production_part part;
      if (prod == null)
    throw new internal_error(
      "Attempt to create an lr_item_core with a null production");
      _the_production = prod;
      if (pos < 0 || pos > _the_production.rhs_length())
    throw new internal_error(
      "Attempt to create an lr_item_core with a bad dot position");
      _dot_pos = pos;
      _core_hash_cache = 13*_the_production.hashCode() + pos;
      if (_dot_pos < _the_production.rhs_length())
    {
      part = _the_production.rhs(_dot_pos);
      if (!part.is_action())
        _symbol_after_dot = ((symbol_part)part).the_symbol();
    }
    } 
  public lr_item_core(production prod) throws internal_error
    {
      this(prod,0);
    }
  protected production _the_production;
  public production the_production() {return _the_production;}
  protected int _dot_pos;
  public int dot_pos() {return _dot_pos;}
  protected int _core_hash_cache;
  protected symbol _symbol_after_dot = null;
  public boolean dot_at_end() 
    {
       return _dot_pos >= _the_production.rhs_length();
    }
  public symbol symbol_after_dot()
    {
      return _symbol_after_dot;
    }
  public non_terminal dot_before_nt()
    {
      symbol sym;
      sym = symbol_after_dot();
      if (sym != null && sym.is_non_term())
    return (non_terminal)sym;
      else
    return null;
    }
  public lr_item_core shift_core() throws internal_error
    {
      if (dot_at_end()) 
    throw new internal_error(
      "Attempt to shift past end of an lr_item_core");
      return new lr_item_core(_the_production, _dot_pos+1);
    }
  public boolean core_equals(lr_item_core other)
    {
      return other != null && 
         _the_production.equals(other._the_production) && 
         _dot_pos == other._dot_pos;
    }
  public boolean equals(lr_item_core other) {return core_equals(other);}
  public boolean equals(Object other)
    {
      if (!(other instanceof lr_item_core))
    return false;
      else
    return equals((lr_item_core)other);
    }
  public int core_hashCode()
    {
      return _core_hash_cache;
    }
  public int hashCode() 
    {
      return _core_hash_cache;
    }
  public String to_simple_string() throws internal_error
    {
      String result;
      production_part part;
      if (_the_production.lhs() != null && 
      _the_production.lhs().the_symbol() != null &&
      _the_production.lhs().the_symbol().name() != null)
    result = _the_production.lhs().the_symbol().name();
      else
    result = "$$NULL$$";
      result += " ::= ";
      for (int i = 0; i<_the_production.rhs_length(); i++)
    {
      if (i == _dot_pos)
        result += "(*) ";
      if (_the_production.rhs(i) == null)
        {
          result += "$$NULL$$ ";
        }
      else
        {
          part = _the_production.rhs(i);
          if (part == null)
        result += "$$NULL$$ ";
          else if (part.is_action())
        result += "{ACTION} ";
          else if (((symbol_part)part).the_symbol() != null &&
                       ((symbol_part)part).the_symbol().name() != null)
        result += ((symbol_part)part).the_symbol().name() + " ";
          else
        result += "$$NULL$$ ";
        }
    }
      if (_dot_pos == _the_production.rhs_length())
    result += "(*) ";
      return result;
    }
  public String toString() 
    {
      try {
        return to_simple_string();
      } catch(internal_error e) {
    e.crash();
    return null;
      }
    }
};

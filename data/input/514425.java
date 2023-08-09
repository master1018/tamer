public class symbol_part extends production_part {
  public symbol_part(symbol sym, String lab) throws internal_error
    {
      super(lab);
      if (sym == null)
    throw new internal_error(
      "Attempt to construct a symbol_part with a null symbol");
      _the_symbol = sym;
    }
  public symbol_part(symbol sym) throws internal_error
    {
      this(sym,null);
    }
  protected symbol _the_symbol;
  public symbol the_symbol() {return _the_symbol;}
  public boolean is_action() { return false; }
  public boolean equals(symbol_part other)
    {
      return other != null && super.equals(other) && 
         the_symbol().equals(other.the_symbol());
    }
  public boolean equals(Object other)
    {
      if (!(other instanceof symbol_part))
    return false;
      else
    return equals((symbol_part)other);
    }
  public int hashCode()
    {
      return super.hashCode() ^ 
         (the_symbol()==null ? 0 : the_symbol().hashCode());
    }
  public String toString()
    {
      if (the_symbol() != null)
    return super.toString() + the_symbol();
      else
    return super.toString() + "$$MISSING-SYMBOL$$";
    }
};

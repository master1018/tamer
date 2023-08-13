public class action_production extends production {
  public action_production(
    production      base,
    non_terminal    lhs_sym, 
    production_part rhs_parts[],
    int             rhs_len,
    String          action_str)
    throws internal_error
    {
      super(lhs_sym, rhs_parts, rhs_len, action_str);
      _base_production = base;
    }
  protected production _base_production;
  public production base_production() {return _base_production;}
};

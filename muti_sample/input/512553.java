public class lalr_transition {
  public lalr_transition(symbol on_sym, lalr_state to_st, lalr_transition nxt)
    throws internal_error
    {
      if (on_sym == null)
    throw new internal_error("Attempt to create transition on null symbol");
      if (to_st == null)
    throw new internal_error("Attempt to create transition to null state");
      _on_symbol = on_sym;
      _to_state  = to_st;
      _next      = nxt;
    }
  public lalr_transition(symbol on_sym, lalr_state to_st) throws internal_error
    {
      this(on_sym, to_st, null);
    }
  protected symbol _on_symbol;
  public symbol on_symbol() {return _on_symbol;}
  protected lalr_state _to_state;
  public lalr_state to_state() {return _to_state;}
  protected lalr_transition _next;
  public lalr_transition next() {return _next;}
  public String toString()
    {
      String result;
      result = "transition on " + on_symbol().name() + " to state [";
      result += _to_state.index();
      result += "]";
      return result;
    }
};

public class parse_reduce_row {
  public parse_reduce_row()
    {
      if (_size <= 0 )  _size = non_terminal.number();
      under_non_term = new lalr_state[size()];
    }
  protected static int _size = 0;
  public static int size() {return _size;}
  public lalr_state under_non_term[];
};

public class parse_action_row {
  public parse_action_row()
    {
      if (_size <= 0 )  _size = terminal.number();
      under_term = new parse_action[size()];
      for (int i=0; i<_size; i++)
    under_term[i] = new parse_action();
    }
  protected static int _size = 0;
  public static int size() {return _size;}
  protected static int reduction_count[] = null;
  public parse_action under_term[];
  public int default_reduce;
  public void compute_default()
    {
      int i, prod, max_prod, max_red;
      if (reduction_count == null) 
    reduction_count = new int[production.number()];
      for (i = 0; i < production.number(); i++)
    reduction_count[i] = 0;
      max_prod = -1;
      max_red = 0;
      for (i = 0; i < size(); i++)
    if (under_term[i].kind() == parse_action.REDUCE)
      {
        prod = ((reduce_action)under_term[i]).reduce_with().index();
        reduction_count[prod]++;
        if (reduction_count[prod] > max_red)
          {
        max_red = reduction_count[prod];
        max_prod = prod;
          }
      }
       default_reduce = max_prod;
    }
};

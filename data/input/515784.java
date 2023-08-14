public class parse_action_table {
  public parse_action_table()
    {
      _num_states = lalr_state.number();
      under_state = new parse_action_row[_num_states];
      for (int i=0; i<_num_states; i++)
    under_state[i] = new parse_action_row();
    }
  protected int _num_states;
  public int num_states() {return _num_states;}
  public  parse_action_row[] under_state;
  public void check_reductions()
    throws internal_error
    {
      parse_action act;
      production   prod;
      for (int row = 0; row < num_states(); row++)
    {
      for (int col = 0; col < under_state[row].size(); col++)
        {
          act = under_state[row].under_term[col];
          if (act != null && act.kind() == parse_action.REDUCE)
        {
          ((reduce_action)act).reduce_with().note_reduction_use();
        }
        }
    }
      for (Enumeration p = production.all(); p.hasMoreElements(); )
    {
      prod = (production)p.nextElement();
      if (prod.num_reductions() == 0)
        {
          if (!emit.nowarn)
        {
          System.err.println("*** Production \"" + 
                  prod.to_simple_string() + "\" never reduced");
          lexer.warning_count++;
        }
        }
    }
    }
  public String toString()
    {
      String result;
      int cnt;
      result = "-------- ACTION_TABLE --------\n";
      for (int row = 0; row < num_states(); row++)
    {
      result += "From state #" + row + "\n";
      cnt = 0;
      for (int col = 0; col < under_state[row].size(); col++)
        {
          if (under_state[row].under_term[col].kind() != parse_action.ERROR)
        {
          result += col + ":" + under_state[row].under_term[col] + " ";
          cnt++;
          if (cnt == 3)
            {
              result += "\n";
              cnt = 0;
            }
        }
        }
      if (cnt != 0) result += "\n";
    }
      result += "------------------------------";
      return result;
    }
};

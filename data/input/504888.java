public class parse_reduce_table {
  public parse_reduce_table()
    {
      _num_states = lalr_state.number();
      under_state = new parse_reduce_row[_num_states];
      for (int i=0; i<_num_states; i++)
    under_state[i] = new parse_reduce_row();
    }
  protected int _num_states;
  public int num_states() {return _num_states;}
  public  parse_reduce_row[] under_state;
  public String toString()
    {
      String result;
      lalr_state goto_st;
      int cnt;
      result = "-------- REDUCE_TABLE --------\n";
      for (int row = 0; row < num_states(); row++)
    {
      result += "From state #" + row + "\n";
      cnt = 0;
      for (int col = 0; col < under_state[row].size(); col++)
        {
          goto_st = under_state[row].under_non_term[col];
          if (goto_st != null)
        {
          result += col + ":"; 
          result += goto_st.index();
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
      result += "-----------------------------";
      return result;
    }
};

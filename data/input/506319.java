public class int_token extends token {
  public int_token(int term_num, int iv)
    {
      super(term_num);
      int_val = iv;
    }
  public int_token(int term_num)
    {
      this(term_num,0);
    }
  public int int_val;
};

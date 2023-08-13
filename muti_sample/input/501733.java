public class long_token extends token {
  public long_token(int term_num, long lv)
    {
      super(term_num);
      long_val = lv;
    }
  public long_token(int term_num)
    {
      this(term_num,0);
    }
  public long long_val;
};

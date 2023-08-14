public class str_token extends token {
  public str_token(int term_num, String v)
    {
      super(term_num);
      str_val = v;
    }
  public str_token(int term_num)
    {
      this(term_num, "");
    }
  public String str_val;
};

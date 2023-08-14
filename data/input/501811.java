public class char_token extends token {
  public char_token(int term_num, char v)
    {
      super(term_num);
      char_val = v;
    }
  public char_token(int term_num)
    {
      this(term_num, '\0');
    }
  public char char_val;
};

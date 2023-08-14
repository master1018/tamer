public class action_part extends production_part {
  public action_part(String code_str)
    {
      super(null);
      _code_string = code_str;
    }
  protected String _code_string;
  public String code_string() {return _code_string;}
  public void set_code_string(String new_str) {_code_string = new_str;}
  public boolean is_action() { return true; }
  public boolean equals(action_part other)
    {
      return other != null && super.equals(other) && 
         other.code_string().equals(code_string());
    }
  public boolean equals(Object other)
    {
      if (!(other instanceof action_part)) 
    return false;
      else
    return equals((action_part)other);
    }
  public int hashCode()
    {
      return super.hashCode() ^ 
         (code_string()==null ? 0 : code_string().hashCode());
    }
  public String toString()
    {
      return super.toString() + "{" + code_string() + "}";
    }
};

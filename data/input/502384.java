public class parse_action {
  public parse_action()
    {
    }
  public static final int ERROR = 0;
  public static final int SHIFT = 1;
  public static final int REDUCE = 2;
  public int kind() {return ERROR;}
  public boolean equals(parse_action other)
    {
      return other != null && other.kind() == ERROR;
    }
  public boolean equals(Object other)
    {
      if (other instanceof parse_action)
    return equals((parse_action)other);
      else
    return false;
    }
  public int hashCode()
    {
      return 0xCafe123;
    }
  public String toString() {return "ERROR";}
};

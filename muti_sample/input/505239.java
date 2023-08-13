public class shift_action extends parse_action {
  public shift_action(lalr_state shft_to) throws internal_error
    {
      if (shft_to == null)
    throw new internal_error(
      "Attempt to create a shift_action to a null state");
      _shift_to = shft_to;
    }
  protected lalr_state _shift_to;
  public lalr_state shift_to() {return _shift_to;}
  public int kind() {return SHIFT;}
  public boolean equals(shift_action other)
    {
      return other != null && other.shift_to() == shift_to();
    }
  public boolean equals(Object other)
    {
      if (other instanceof shift_action)
    return equals((shift_action)other);
      else
       return false;
    }
  public int hashCode()
    {
      return shift_to().hashCode();
    }
  public String toString() {return "SHIFT(" + shift_to().index() + ")";}
};

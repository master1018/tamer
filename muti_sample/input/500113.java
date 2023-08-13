public class reduce_action extends parse_action {
  public reduce_action(production prod ) throws internal_error
    {
      if (prod == null)
    throw new internal_error(
      "Attempt to create a reduce_action with a null production");
      _reduce_with = prod;
    }
  protected production _reduce_with;
  public production reduce_with() {return _reduce_with;}
  public int kind() {return REDUCE;}
  public boolean equals(reduce_action other)
    {
      return other != null && other.reduce_with() == reduce_with();
    }
  public boolean equals(Object other)
    {
      if (other instanceof reduce_action)
    return equals((reduce_action)other);
      else
       return false;
    }
  public int hashCode()
    {
      return reduce_with().hashCode();
    }
  public String toString() 
    {
      return "REDUCE(" + reduce_with().index() + ")";
    }
};

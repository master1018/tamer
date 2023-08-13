public abstract class production_part {
  public production_part(String lab)
    {
      _label = lab;
    }
  protected String _label;
  public String label() {return _label;}
  public abstract boolean is_action();
  public boolean equals(production_part other)
    {
      if (other == null) return false;
      if (label() != null)
    return label().equals(other.label());
      else
    return other.label() == null;
    }
  public boolean equals(Object other)
    {
      if (!(other instanceof production_part))
        return false;
      else
    return equals((production_part)other);
    }
  public int hashCode()
    {
      return label()==null ? 0 : label().hashCode();
    }
  public String toString()
    {
      if (label() != null)
    return label() + ":";
      else
    return " ";
    }
};

public final class ExtendedType
{
    private int nodetype;
    private String namespace;
    private String localName;
    private int hash;
    public ExtendedType (int nodetype, String namespace, String localName)
    {
      this.nodetype = nodetype;
      this.namespace = namespace;
      this.localName = localName;
      this.hash = nodetype + namespace.hashCode() + localName.hashCode();
    }
    public ExtendedType (int nodetype, String namespace, String localName, int hash)
    {
      this.nodetype = nodetype;
      this.namespace = namespace;
      this.localName = localName;
      this.hash = hash;
    }
    protected void redefine(int nodetype, String namespace, String localName)
    {
      this.nodetype = nodetype;
      this.namespace = namespace;
      this.localName = localName;
      this.hash = nodetype + namespace.hashCode() + localName.hashCode();
    }
    protected void redefine(int nodetype, String namespace, String localName, int hash)
    {
      this.nodetype = nodetype;
      this.namespace = namespace;
      this.localName = localName;
      this.hash = hash;
    }
    public int hashCode()
    {
      return hash;
    }
    public boolean equals(ExtendedType other)
    {
      try
      {
        return other.nodetype == this.nodetype &&
                other.localName.equals(this.localName) &&
                other.namespace.equals(this.namespace);
      }
      catch(NullPointerException e)
      {
        return false;
      }
    }
    public int getNodeType()
    {
      return nodetype;
    }
    public String getLocalName()
    {
      return localName;
    }
    public String getNamespace()
    {
      return namespace;
    }
}

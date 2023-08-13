public class CommitmentTypeQualifier
    extends ASN1Encodable
{
   private DERObjectIdentifier commitmentTypeIdentifier;
   private DEREncodable qualifier;
    public CommitmentTypeQualifier(
        DERObjectIdentifier commitmentTypeIdentifier)
    {
        this(commitmentTypeIdentifier, null);
    }
    public CommitmentTypeQualifier(
        DERObjectIdentifier commitmentTypeIdentifier,
        DEREncodable qualifier) 
    {
        this.commitmentTypeIdentifier = commitmentTypeIdentifier;
        this.qualifier = qualifier;
    }
    public CommitmentTypeQualifier(
        ASN1Sequence as)
    {
        commitmentTypeIdentifier = (DERObjectIdentifier)as.getObjectAt(0);
        if (as.size() > 1)
        {
            qualifier = as.getObjectAt(1);
        }
    }
    public static CommitmentTypeQualifier getInstance(Object as)
    {
        if (as instanceof CommitmentTypeQualifier || as == null)
        {
            return (CommitmentTypeQualifier)as;
        }
        else if (as instanceof ASN1Sequence)
        {
            return new CommitmentTypeQualifier((ASN1Sequence)as);
        }
        throw new IllegalArgumentException("unknown object in getInstance.");
    }
    public DERObjectIdentifier getCommitmentTypeIdentifier()
    {
        return commitmentTypeIdentifier;
    }
    public DEREncodable getQualifier()
    {
        return qualifier;
    }
   public DERObject toASN1Object() 
   {
      ASN1EncodableVector dev = new ASN1EncodableVector();
      dev.add(commitmentTypeIdentifier);
      if (qualifier != null)
      {
          dev.add(qualifier);
      }
      return new DERSequence(dev);
   }
}

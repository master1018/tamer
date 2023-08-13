public class DataGroupHash 
    extends ASN1Encodable
{
    DERInteger dataGroupNumber;    
    ASN1OctetString    dataGroupHashValue;
    public static DataGroupHash getInstance(
        Object obj)
    {
        if (obj == null || obj instanceof DataGroupHash)
        {
            return (DataGroupHash)obj;
        }
        if (obj instanceof ASN1Sequence)
        {
            return new DataGroupHash(ASN1Sequence.getInstance(obj));            
        }
        else
        {
            throw new IllegalArgumentException("unknown object in getInstance");
        }
    }                
    public DataGroupHash(ASN1Sequence seq)
    {
        Enumeration e = seq.getObjects();
        dataGroupNumber = DERInteger.getInstance(e.nextElement());
        dataGroupHashValue = ASN1OctetString.getInstance(e.nextElement());   
    }
    public DataGroupHash(
        int dataGroupNumber,        
        ASN1OctetString     dataGroupHashValue)
    {
        this.dataGroupNumber = new DERInteger(dataGroupNumber);
        this.dataGroupHashValue = dataGroupHashValue; 
    }    
    public int getDataGroupNumber()
    {
        return dataGroupNumber.getValue().intValue();
    }
    public ASN1OctetString getDataGroupHashValue()
    {
        return dataGroupHashValue;
    }     
    public DERObject toASN1Object() 
    {
        ASN1EncodableVector seq = new ASN1EncodableVector();
        seq.add(dataGroupNumber);
        seq.add(dataGroupHashValue);  
        return new DERSequence(seq);
    }
}

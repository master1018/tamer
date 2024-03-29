public class QCStatement 
    extends ASN1Encodable 
    implements ETSIQCObjectIdentifiers, RFC3739QCObjectIdentifiers
{
    DERObjectIdentifier qcStatementId;
    ASN1Encodable       qcStatementInfo;
    public static QCStatement getInstance(
        Object obj)
    {
        if (obj == null || obj instanceof QCStatement)
        {
            return (QCStatement)obj;
        }
        if (obj instanceof ASN1Sequence)
        {
            return new QCStatement(ASN1Sequence.getInstance(obj));            
        }
        throw new IllegalArgumentException("unknown object in getInstance");
    }    
    public QCStatement(
        ASN1Sequence seq)
    {
        Enumeration e = seq.getObjects();
        qcStatementId = DERObjectIdentifier.getInstance(e.nextElement());
        if (e.hasMoreElements())
        {
            qcStatementInfo = (ASN1Encodable) e.nextElement();
        }
    }    
    public QCStatement(
        DERObjectIdentifier qcStatementId)
    {
        this.qcStatementId = qcStatementId;
        this.qcStatementInfo = null;
    }
    public QCStatement(
        DERObjectIdentifier qcStatementId, 
        ASN1Encodable       qcStatementInfo)
    {
        this.qcStatementId = qcStatementId;
        this.qcStatementInfo = qcStatementInfo;
    }    
    public DERObjectIdentifier getStatementId()
    {
        return qcStatementId;
    }
    public ASN1Encodable getStatementInfo()
    {
        return qcStatementInfo;
    }
    public DERObject toASN1Object() 
    {
        ASN1EncodableVector seq = new ASN1EncodableVector();
        seq.add(qcStatementId);       
        if (qcStatementInfo != null)
        {
            seq.add(qcStatementInfo);
        }
        return new DERSequence(seq);
    }
}

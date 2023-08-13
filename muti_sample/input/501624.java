public class UserNotice 
    extends ASN1Encodable 
{
    private NoticeReference noticeRef;
    private DisplayText     explicitText;
    public UserNotice(
        NoticeReference noticeRef, 
        DisplayText explicitText) 
    {
        this.noticeRef = noticeRef;
        this.explicitText = explicitText;
    }
    public UserNotice(
        NoticeReference noticeRef, 
        String str) 
    {
        this.noticeRef = noticeRef;
        this.explicitText = new DisplayText(str);
    }
    public UserNotice(
       ASN1Sequence as) 
    {
       if (as.size() == 2)
       {
           noticeRef = NoticeReference.getInstance(as.getObjectAt(0));
           explicitText = DisplayText.getInstance(as.getObjectAt(1));
       }
       else if (as.size() == 1)
       {
           if (as.getObjectAt(0).getDERObject() instanceof ASN1Sequence)
           {
               noticeRef = NoticeReference.getInstance(as.getObjectAt(0));
           }
           else
           {
               explicitText = DisplayText.getInstance(as.getObjectAt(0));
           }
       }
       else
       {
           throw new IllegalArgumentException("Bad sequence size: " + as.size());
       }
    }
    public NoticeReference getNoticeRef()
    {
        return noticeRef;
    }
    public DisplayText getExplicitText()
    {
        return explicitText;
    }
    public DERObject toASN1Object() 
    {
        ASN1EncodableVector av = new ASN1EncodableVector();
        if (noticeRef != null)
        {
            av.add(noticeRef);
        }
        if (explicitText != null)
        {
            av.add(explicitText);
        }
        return new DERSequence(av);
    }
}

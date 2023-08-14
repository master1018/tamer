public class NoticeReference 
    extends ASN1Encodable
{
   private DisplayText organization;
   private ASN1Sequence noticeNumbers;
   public NoticeReference(
       String orgName,
       Vector numbers) 
   {
      organization = new DisplayText(orgName);
      Object o = numbers.elementAt(0);
      ASN1EncodableVector av = new ASN1EncodableVector();
      if (o instanceof Integer)
      {
         Enumeration it = numbers.elements();
         while (it.hasMoreElements())
         {
            Integer nm = (Integer) it.nextElement();
               DERInteger di = new DERInteger(nm.intValue());
            av.add (di);
         }
      }
      noticeNumbers = new DERSequence(av);
   }
   public NoticeReference(
       String orgName, 
       ASN1Sequence numbers) 
   {
       organization = new DisplayText (orgName);
       noticeNumbers = numbers;
   }
   public NoticeReference(
       int displayTextType,
       String orgName,
       ASN1Sequence numbers) 
   {
       organization = new DisplayText(displayTextType, 
                                     orgName);
       noticeNumbers = numbers;
   }
   public NoticeReference(
       ASN1Sequence as) 
   {
       if (as.size() != 2)
       {
            throw new IllegalArgumentException("Bad sequence size: "
                    + as.size());
       }
       organization = DisplayText.getInstance(as.getObjectAt(0));
       noticeNumbers = ASN1Sequence.getInstance(as.getObjectAt(1));
   }
   public static NoticeReference getInstance(
       Object as) 
   {
      if (as instanceof NoticeReference)
      {
          return (NoticeReference)as;
      }
      else if (as instanceof ASN1Sequence)
      {
          return new NoticeReference((ASN1Sequence)as);
      }
      throw new IllegalArgumentException("unknown object in getInstance.");
   }
   public DisplayText getOrganization()
   {
       return organization;
   }
   public ASN1Sequence getNoticeNumbers()
   {
       return noticeNumbers;
   }
   public DERObject toASN1Object() 
   {
      ASN1EncodableVector av = new ASN1EncodableVector();
      av.add (organization);
      av.add (noticeNumbers);
      return new DERSequence (av);
   }
}

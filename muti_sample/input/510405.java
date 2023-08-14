public class DisplayText 
    extends ASN1Encodable
    implements ASN1Choice
{
   public static final int CONTENT_TYPE_IA5STRING = 0;
   public static final int CONTENT_TYPE_BMPSTRING = 1;
   public static final int CONTENT_TYPE_UTF8STRING = 2;
   public static final int CONTENT_TYPE_VISIBLESTRING = 3;
   public static final int DISPLAY_TEXT_MAXIMUM_SIZE = 200;
   int contentType;
   DERString contents;
   public DisplayText (int type, String text) 
   {
      if (text.length() > DISPLAY_TEXT_MAXIMUM_SIZE)
      {
         text = text.substring (0, DISPLAY_TEXT_MAXIMUM_SIZE);
      }
      contentType = type;
      switch (type)
      {
         case CONTENT_TYPE_IA5STRING:
            contents = (DERString)new DERIA5String (text);
            break;
         case CONTENT_TYPE_UTF8STRING:
            contents = (DERString)new DERUTF8String(text);
            break;
         case CONTENT_TYPE_VISIBLESTRING:
            contents = (DERString)new DERVisibleString(text);
            break;
         case CONTENT_TYPE_BMPSTRING:
            contents = (DERString)new DERBMPString(text);
            break;
         default:
            contents = (DERString)new DERUTF8String(text);
            break;
      }
   }
   public DisplayText (String text) 
   {
      if (text.length() > DISPLAY_TEXT_MAXIMUM_SIZE)
      {
         text = text.substring(0, DISPLAY_TEXT_MAXIMUM_SIZE);
      }
      contentType = CONTENT_TYPE_UTF8STRING;
      contents = new DERUTF8String(text);
   }
   public DisplayText(DERString de)
   {
      contents = de;
   }
   public static DisplayText getInstance(Object de) 
   {
      if (de instanceof DERString)
      {
          return new DisplayText((DERString)de);
      }
      else if (de instanceof DisplayText)
      {
          return (DisplayText)de;
      }
      throw new IllegalArgumentException("illegal object in getInstance");
   }
   public static DisplayText getInstance(
       ASN1TaggedObject obj,
       boolean          explicit)
   {
       return getInstance(obj.getObject()); 
   }
   public DERObject toASN1Object() 
   {
      return (DERObject)contents;
   }
   public String getString() 
   {
      return contents.getString();
   }   
}

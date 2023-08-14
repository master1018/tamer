public class PolicyQualifierId extends DERObjectIdentifier 
{
   private static final String id_qt = "1.3.6.1.5.5.7.2";
   private PolicyQualifierId(String id) 
      {
         super(id);
      }
   public static final PolicyQualifierId id_qt_cps =
       new PolicyQualifierId(id_qt + ".1");
   public static final PolicyQualifierId id_qt_unotice =
       new PolicyQualifierId(id_qt + ".2");
}

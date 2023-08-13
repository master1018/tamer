public class DirectoryString {
    public static final ASN1Choice ASN1 = new ASN1Choice(new ASN1Type[] {
           ASN1StringType.TELETEXSTRING,
           ASN1StringType.PRINTABLESTRING,
           ASN1StringType.UNIVERSALSTRING,
           ASN1StringType.UTF8STRING,
           ASN1StringType.BMPSTRING }) {
        public int getIndex(java.lang.Object object) {
            return 1; 
        }
        public Object getObjectToEncode(Object object) {
            return  object;
        }
    };
}

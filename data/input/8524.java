public class InvalidParameters {
    public static void main(String[] args) throws Exception {
        try {
            PKIXBuilderParameters p =
                new PKIXBuilderParameters(Collections.EMPTY_SET, null);
            throw new Exception("should have thrown InvalidAlgorithmParameterExc");
        } catch (InvalidAlgorithmParameterException iape) { }
        try {
            PKIXBuilderParameters p = new PKIXBuilderParameters((Set) null, null);
            throw new Exception("should have thrown NullPointerException");
        } catch (NullPointerException npe) { }
        try {
            PKIXBuilderParameters p =
                new PKIXBuilderParameters(Collections.singleton(new String()), null);
            throw new Exception("should have thrown ClassCastException");
        } catch (ClassCastException cce) { }
    }
}

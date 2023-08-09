public class ETypeOrder {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.security.krb5.conf", "no_such_file");
        int[] etypes = EType.getBuiltInDefaults();
        int correct[] = { 18, 17, 16, 23, 1, 3, 2 };
        int match = 0;
        loopi: for (int i=0; i<etypes.length; i++) {
            for (; match < correct.length; match++) {
                if (etypes[i] == correct[match]) {
                    System.out.println("Find " + etypes[i] + " at #" + match);
                    continue loopi;
                }
            }
            throw new Exception("No match or bad order for " + etypes[i]);
        }
    }
}

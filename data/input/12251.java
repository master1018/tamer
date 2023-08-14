public class OidFormat {
    public static void main(String[] args) throws Exception {
        String[] badOids = {
            "0", "1", "2",
            "3.1.1", "3", "4",
            "1.40", "1.111.1",
            "-1.2", "0,-2", "1.-2", "2.-2",
            "1.2.-3.4", "1.2.3.-4",
            "aa", "aa.aa",
            "", "....", "1.2..4", "1.2.3.", "1.", "1.2.", "0.1.",
            "1,2",
        };
        for (String s: badOids) {
            testBad(s);
        }
        String[] goodOids = {
            "0.0", "0.1", "1.0", "1.2",
            "0.39", "1.39", "2.47", "2.40.3.6", "2.100.3", "2.123456.3",
            "1.2.3", "1.2.3445",
            "1.3.6.1.4.1.42.2.17",
            "2.16.764.1.3101555394.1.0.100.2.1",
            "2.2726957624935694386592435",  
            "1.2.777777777777777777",
            "1.2.888888888888888888.111111111111111.2222222222222.33333333333333333.44444444444444",
            "1.2." +
                "1111111111111111111111111111111111111111111111111111111111111." +
                "2222222222222222222222222222222222222222222222222222222222222222." +
                "333333333333333333333333333333333333333333333333333333333333333." +
                "4444444444444444444444444444444444444444444444444444444." +
                "55555555555555555555555555555555555555555555555555555555555555555555555." +
                "666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666." +
                "77777777777777777777777777777777777777777777777777777777777777777777777777." +
                "8888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888." +
                "9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999",
            "1.2.2147483647.4",
            "1.2.268435456.4",
        };
        for (String s: goodOids) {
            testGood(s);
        }
        int[][] goodInts = {
            {0,0}, {0,1}, {1,0}, {1,2},
            {0,39}, {1,39}, {2,47}, {2,40,3,6}, {2,100,3}, {2,123456,3},
            {1,2,3}, {1,2,3445},
            {1,3,6,1,4,1,42,2,17},
        };
        for (int[] is: goodInts) {
            testGood(is);
        }
        int[][] badInts = new int[][] {
            {0}, {1}, {2},
            {3,1,1}, {3}, {4},
            {1,40}, {1,111,1},
            {-1,2}, {0,-2}, {1,-2}, {2,-2},
            {1,2,-3,4}, {1,2,3,-4},
        };
        for (int[] is: badInts) {
            testBad(is);
        }
    }
    static void testBad(int[] ints) throws Exception {
        System.err.println("Trying " + Arrays.toString(ints));
        try {
            new ObjectIdentifier(ints);
            throw new Exception("should be invalid ObjectIdentifier");
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }
    static void testGood(int[] ints) throws Exception {
        System.err.println("Trying " + Arrays.toString(ints));
        ObjectIdentifier oid = new ObjectIdentifier(ints);
        DerOutputStream os = new DerOutputStream();
        os.putOID(oid);
        DerInputStream is = new DerInputStream(os.toByteArray());
        ObjectIdentifier oid2 = is.getOID();
        if (!oid.equals((Object)oid2)) {
            throw new Exception("Test DER I/O fails: " + oid + " and " + oid2);
        }
    }
    static void testGood(String s) throws Exception {
        System.err.println("Trying " + s);
        ObjectIdentifier oid = new ObjectIdentifier(s);
        if (!oid.toString().equals(s)) {
            throw new Exception("equal test fail");
        }
        DerOutputStream os = new DerOutputStream();
        os.putOID(oid);
        DerInputStream is = new DerInputStream(os.toByteArray());
        ObjectIdentifier oid2 = is.getOID();
        if (!oid.equals((Object)oid2)) {
            throw new Exception("Test DER I/O fails: " + oid + " and " + oid2);
        }
    }
    static void testBad(String s) throws Exception {
        System.err.println("Trying " + s);
        try {
            new ObjectIdentifier(s);
            throw new Exception("should be invalid ObjectIdentifier");
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
        try {
            new Oid(s);
            throw new Exception("should be invalid Oid");
        } catch (GSSException gsse) {
            ;
        }
        try {
            new EncryptedPrivateKeyInfo(s, new byte[8]);
            throw new Exception("should be invalid algorithm");
        } catch (NoSuchAlgorithmException e) {
            ;
        }
    }
}

public class ParseCAPaths {
    static Exception failed = null;
    public static void main(String[] args) throws Exception {
        System.setProperty("java.security.krb5.conf",
                System.getProperty("test.src", ".") +"/krb5-capaths.conf");
        check("ANL.GOV", "TEST.ANL.GOV", "ANL.GOV");
        check("ANL.GOV", "ES.NET", "ANL.GOV");
        check("ANL.GOV", "PNL.GOV", "ANL.GOV", "ES.NET");
        check("ANL.GOV", "NERSC.GOV", "ANL.GOV", "ES.NET");
        check("N1.N.COM", "N2.N.COM", "N1.N.COM", "N.COM");     
        check("N1.N.COM", "N2.N3.COM", "N1.N.COM", "N.COM",     
                "COM", "N3.COM");
        check("N1.COM", "N2.COM", "N1.COM", "COM");             
        check("N1", "N2", "N1");                                
        check("A1.COM", "A4.COM", "A1.COM", "A2.COM");
        check("B1.COM", "B3.COM", "B1.COM", "B2.COM");
        check("C1.COM", "C3.COM", "C1.COM", "C2.COM");
        check("D1.COM", "D4.COM", "D1.COM", "D2.COM");
        check("E1.COM", "E4.COM", "E1.COM", "E2.COM");
        check("F1.COM", "F4.COM", "F1.COM", "F9.COM");
        check("G1.COM", "G3.COM", "G1.COM", "COM");
        check("H1.COM", "H3.COM", "H1.COM");
        check("I1.COM", "I4.COM", "I1.COM", "I5.COM");
        check("J1.COM", "J2.COM", "J1.COM");
        check("A9.PRAGUE.XXX.CZ", "SERVIS.XXX.CZ",
                "A9.PRAGUE.XXX.CZ", "PRAGUE.XXX.CZ", "ROOT.XXX.CZ");
        if (failed != null) {
            throw failed;
        }
    }
    static void check(String from, String to, String... paths) {
        try {
            check2(from, to, paths);
        } catch (Exception e) {
            failed = e;
        }
    }
    static void check2(String from, String to, String... paths)
            throws Exception {
        System.out.println(from + " -> " + to);
        System.out.println("    expected: " + Arrays.toString(paths));
        String[] result = Realm.getRealmsList(from, to);
        System.out.println("    result:   " + Arrays.toString(result));
        if (result == null) {
            if (paths.length == 0) {
            } else {
                throw new Exception("Shouldn't have a valid path.");
            }
        } else if(result.length != paths.length) {
            throw new Exception("Length of path not correct");
        } else {
            for (int i=0; i<result.length; i++) {
                if (!result[i].equals(paths[i])) {
                    throw new Exception("Path not same");
                }
            }
        }
    }
}

public class OIDDatabase {
    private static OIDDatabase instance = new OIDDatabase();
    private Set<DBEntry> oids = new HashSet<DBEntry>();
    private Set<DBEntry> algorithms = new HashSet<DBEntry>();
    private OIDDatabase() {
        DBEntry oid;
        DBEntry alg;
        oid = new DBEntry("1.2.840.113549.1.1.4");
        alg = new DBEntry("MD5withRSA");
        wireTogether(oid, alg);
        oid = new DBEntry("1.2.840.113549.1.1.5");
        alg = new DBEntry("SHA1withRSA");
        wireTogether(oid, alg);
        oid = new DBEntry("1.2.840.10040.4.3");
        alg = new DBEntry("SHA1withDSA");
        wireTogether(oid, alg);
        oid = new DBEntry("1.3.14.3.2.26");
        alg = new DBEntry("SHA");
        DBEntry alg2 = new DBEntry("SHA-1");
        wireTogether(oid, alg);
        wireTogether(oid, alg2);
        oid = new DBEntry("1.2.840.113549.2.5");
        alg = new DBEntry("MD5");
        wireTogether(oid, alg);
        oid = new DBEntry("1.2.840.113549.1.1.1");
        alg = new DBEntry("RSA");
        wireTogether(oid, alg);
        oid = new DBEntry("1.2.840.10040.4.1");
        DBEntry oid2 = new DBEntry("1.3.14.3.2.12");
        alg = new DBEntry("DSA");
        wireTogether(oid, alg);
        wireTogether(oid2, alg);
        oid = new DBEntry("1.2.840.10046.2.1");
        alg = new DBEntry("DiffieHellman");
        wireTogether(oid, alg);
    }
    private void wireTogether(DBEntry oidValue, DBEntry algorithmValue) {
        oids.add(oidValue);
        algorithms.add(algorithmValue);
        oidValue.addEquivalent(algorithmValue);
        algorithmValue.addEquivalent(oidValue);
    }
    public static OIDDatabase getInstance() {
        return instance;
    }
    public String getFirstAlgorithmForOID(String oid) {
        String result = null;
        Iterator<String> it = this.getAllAlgorithmsForOID(oid).iterator();
        if (it.hasNext()) {
            result = (it.next());
        }
        return result;
    }
    public Set<String> getAllAlgorithmsForOID(String oid) {
        Set<String> result = null;
        Iterator<DBEntry> it = this.oids.iterator();
        result = getAllEquivalents(oid, it);
        if (result == null) {
            throw new IllegalArgumentException("Unknown OID : " + oid);
        }
        return result;
    }
    public String getFirstOIDForAlgorithm(String algorithm) {
        String result = null;
        Iterator<String> it = this.getAllOIDsForAlgorithm(algorithm).iterator();
        if (it.hasNext()) {
            result = (it.next());
        }
        return result;
    }
    public Set<String> getAllOIDsForAlgorithm(String algorithm) {
        Set<String> result = null;
        Iterator<DBEntry> it = this.algorithms.iterator();
        result = getAllEquivalents(algorithm, it);
        if (result == null) {
            throw new IllegalArgumentException("Unsupported algorithm : "
                    + algorithm);
        }
        return result;
    }
    private Set<String> getAllEquivalents(String value, Iterator<DBEntry> it) {
        Set<String> result = null;
        while (it.hasNext()) {
            DBEntry element = it.next();
            if (element.getValue().equals(value)) {
                Set<DBEntry> allMatchingDBEntries = element.getAllEquivalents();
                result = new HashSet<String>();
                Iterator<DBEntry> dbIt = allMatchingDBEntries.iterator();
                while (dbIt.hasNext()) {
                    DBEntry matchingEntry = dbIt.next();
                    result.add(matchingEntry.getValue());
                }
            }
        }
        return result;
    }
    static class DBEntry {
        private final List<DBEntry> equivalents = new LinkedList<DBEntry>();
        private final String value;
        DBEntry(String value) {
            this.value = value;
        }
        void addEquivalent(DBEntry entry) {
            this.equivalents.add(entry);
        }
        String getValue() {
            return this.value;
        }
        Set<DBEntry> getAllEquivalents() {
            return new HashSet<DBEntry>(this.equivalents);
        }
    }
}

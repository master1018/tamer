public class AlgNameMapper {
    private static final String[] serviceName = {
            "Cipher", 
            "AlgorithmParameters", 
            "Signature" 
    };
    private static final String[][] knownAlgMappings = {
        {"1.2.840.10040.4.1",       "DSA"}, 
        {"1.2.840.10040.4.3",       "SHA1withDSA"}, 
        {"1.2.840.113549.1.1.1",    "RSA"}, 
        {"1.2.840.113549.1.1.4",    "MD5withRSA"}, 
        {"1.2.840.113549.1.1.5",    "SHA1withRSA"}, 
        {"1.2.840.113549.1.3.1",    "DiffieHellman"}, 
        {"1.2.840.113549.1.5.3",    "pbeWithMD5AndDES-CBC"}, 
        {"1.2.840.113549.1.12.1.3", "pbeWithSHAAnd3-KeyTripleDES-CBC"}, 
        {"1.2.840.113549.1.12.1.6", "pbeWithSHAAnd40BitRC2-CBC"}, 
        {"1.2.840.113549.3.2",      "RC2-CBC"}, 
        {"1.2.840.113549.3.3",      "RC2-EBC"}, 
        {"1.2.840.113549.3.4",      "RC4"}, 
        {"1.2.840.113549.3.5",      "RC4WithMAC"}, 
        {"1.2.840.113549.3.6",      "DESx-CBC"}, 
        {"1.2.840.113549.3.7",      "TripleDES-CBC"}, 
        {"1.2.840.113549.3.8",      "rc5CBC"}, 
        {"1.2.840.113549.3.9",      "RC5-CBC"}, 
        {"1.2.840.113549.3.10",     "DESCDMF"}, 
        {"2.23.42.9.11.4.1",        "ECDSA"}, 
    };
    private static final Map<String, String> alg2OidMap = new HashMap<String, String>();
    private static final Map<String, String> oid2AlgMap = new HashMap<String, String>();
    private static final Map<String, String> algAliasesMap = new HashMap<String, String>();
    static {
        for (String[] element : knownAlgMappings) {
            String algUC = Util.toUpperCase(element[1]);
            alg2OidMap.put(algUC, element[0]);
            oid2AlgMap.put(element[0], algUC);
            algAliasesMap.put(algUC, element[1]);
        }
        Provider[] pl = Security.getProviders();
        for (Provider element : pl) {
            selectEntries(element);
        }
    }
    private AlgNameMapper() {
    }
    public static String map2OID(String algName) {
        return alg2OidMap.get(Util.toUpperCase(algName));
    }
    public static String map2AlgName(String oid) {
        String algUC = oid2AlgMap.get(oid);
        return algUC == null ? null : algAliasesMap.get(algUC);
    }
    public static String getStandardName(String algName) {
        return algAliasesMap.get(Util.toUpperCase(algName));
    }
    private static void selectEntries(Provider p) {
        Set<Map.Entry<Object, Object>> entrySet = p.entrySet();
        for (String service : serviceName) {
            String keyPrfix2find = "Alg.Alias." + service + ".";  
            for (Entry<Object, Object> me : entrySet) {
                String key = (String)me.getKey();
                if (key.startsWith(keyPrfix2find)) {
                    String alias = key.substring(keyPrfix2find.length());
                    String alg = (String)me.getValue();
                    String algUC = Util.toUpperCase(alg);
                    if (isOID(alias)) {
                        if (alias.startsWith("OID.")) { 
                            alias = alias.substring(4);
                        }
                        boolean oid2AlgContains = oid2AlgMap.containsKey(alias);
                        boolean alg2OidContains = alg2OidMap.containsKey(algUC);
                        if (!oid2AlgContains || !alg2OidContains) {
                            if (!oid2AlgContains) {
                                oid2AlgMap.put(alias, algUC);
                            } 
                            if (!alg2OidContains) {
                                alg2OidMap.put(algUC, alias);
                            }
                            algAliasesMap.put(algUC, alg);
                        }
                    } else if (!algAliasesMap.containsKey(Util.toUpperCase(alias))) {
                        algAliasesMap.put(Util.toUpperCase(alias), alg);
                    }
                }
            }
        }
    }
    public static boolean isOID(String alias) {
        return ObjectIdentifier.isOID(normalize(alias));
    }
    public static String normalize(String oid) {
        return oid.startsWith("OID.") 
            ? oid.substring(4)
            : oid;
    }
    public static String dump() {
        StringBuilder sb = new StringBuilder("alg2OidMap: "); 
        sb.append(alg2OidMap);
        sb.append("\noid2AlgMap: "); 
        sb.append(oid2AlgMap);
        sb.append("\nalgAliasesMap: "); 
        sb.append(algAliasesMap);
        return sb.toString();
    }
}

@TestTargetClass(Security.class)
public class Security2Test extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "NullPointerException checking missed",
        method = "getProviders",
        args = {java.lang.String.class}
    )
    public void test_getProvidersLjava_lang_String() {
        Hashtable<String, Integer> allSupported = new Hashtable<String, Integer>();
        Provider[] allProviders = Security.getProviders();
        for (int i = 0; i < allProviders.length; i++) {
            Provider provider = allProviders[i];
            Iterator it = provider.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = (String) entry.getKey();
                if (!isAlias(key) && !isProviderData(key)) {
                    addOrIncrementTable(allSupported, key);
                }
            }
        }
        for (int i = 0; i < allProviders.length; i++) {
            Provider provider = allProviders[i];
            Iterator it = provider.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = (String) entry.getKey();
                if (isAlias(key)) {
                    String aliasVal = key.substring("ALG.ALIAS.".length());
                    String aliasKey = aliasVal.substring(0, aliasVal
                            .indexOf(".") + 1)
                            + entry.getValue();
                    if (!aliasVal.equals(aliasKey)) {
                        if (allSupported.containsKey(aliasVal)) {
                            addOrIncrementTable(allSupported, aliasKey);
                        }
                    }
                }
            }
        }
        Provider provTest[] = null;
        Iterator it = allSupported.keySet().iterator();
        while (it.hasNext()) {
            String filterString = (String) it.next();
            try {
                provTest = Security.getProviders(filterString);
                int expected = ((Integer) allSupported.get(filterString))
                        .intValue();
                assertEquals(
                        "Unexpected number of providers returned for filter "
                                + filterString, expected, provTest.length);
            } catch (InvalidParameterException e) {
            }
        }
        try {
            provTest = Security.getProviders("Signature.SHA1withDSA :512");
            fail("InvalidParameterException should be thrown <Signature.SHA1withDSA :512>");
        } catch (InvalidParameterException e) {
        }
    }
    private boolean isProviderData(String key) {
        return key.toUpperCase().startsWith("PROVIDER.");
    }
    private boolean isAlias(String key) {
        return key.toUpperCase().startsWith("ALG.ALIAS.");
    }
    private void addOrIncrementTable(Hashtable<String, Integer> table, String key) {
        if (table.containsKey(key)) {
            Integer before = (Integer) table.get(key);
            table.put(key, new Integer(before.intValue() + 1));
        } else {
            table.put(key, new Integer(1));
        }
    }
    private int getProvidersCount(Map filterMap) {
        int result = 0;
        Provider[] allProviders = Security.getProviders();
        for (int i = 0; i < allProviders.length; i++) {
            Provider provider = allProviders[i];
            Set allProviderKeys = provider.keySet();
            boolean noMatchFoundForFilterEntry = false;
            Set allFilterKeys = filterMap.keySet();
            Iterator fkIter = allFilterKeys.iterator();
            while (fkIter.hasNext()) {
                String filterString = ((String) fkIter.next()).trim();
                if (filterString.endsWith("=")) {
                    filterString = filterString.substring(0, filterString
                            .length() - 1);
                }
                if (filterString != null) {
                    if (filterString.indexOf(" ") == -1) {
                        if (!allProviderKeys.contains(filterString)) {
                            if (!allProviderKeys.contains("Alg.Alias."
                                    + filterString)) {
                                noMatchFoundForFilterEntry = true;
                                break; 
                            }
                        }
                    } else {
                        if (allProviderKeys.contains(filterString)) {
                            String filterVal = (String) filterMap
                                    .get(filterString);
                            String providerVal = (String) provider
                                    .get(filterString);
                            if (providerVal == null
                                    || !providerVal.equals(filterVal)) {
                                noMatchFoundForFilterEntry = true;
                                break; 
                            }
                        }
                    }
                }
            }
            if (!noMatchFoundForFilterEntry) {
                result++;
            }
        }
        return result;
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "NullPointerException checking missed",
        method = "getProviders",
        args = {java.util.Map.class}
    )
    public void test_getProvidersLjava_util_Map() {
        Map<String, String> filter = new Hashtable<String, String>();
        filter.put("KeyStore.BKS", "");
        filter.put("Signature.SHA1withDSA", "");
        Provider provTest[] = Security.getProviders(filter);
        if (provTest == null) {
            assertEquals("Filter : <KeyStore.BKS>,<Signature.SHA1withDSA>",
                    0, getProvidersCount(filter));
        } else {
            assertEquals("Filter : <KeyStore.BKS>,<Signature.SHA1withDSA>",
                    getProvidersCount(filter), provTest.length);
        }
        filter = new Hashtable<String, String>();
        filter.put("MessageDigest.SHA-384", "");
        filter.put("CertificateFactory.X.509", "");
        filter.put("KeyFactory.RSA", "");
        provTest = Security.getProviders(filter);
        if (provTest == null) {
            assertEquals("Filter : <MessageDigest.SHA-384>,<CertificateFactory.X.509>,<KeyFactory.RSA>",
                    0, getProvidersCount(filter));
        } else {
            assertEquals(
                    "Filter : <MessageDigest.SHA-384>,<CertificateFactory.X.509>,<KeyFactory.RSA>",
                    getProvidersCount(filter), provTest.length);
        }
        filter = new Hashtable<String, String>();
        filter.put("MessageDigest.SHA1", "");
        filter.put("TrustManagerFactory.X509", "");
        provTest = Security.getProviders(filter);
        if (provTest == null) {
            assertEquals("Filter : <MessageDigest.SHA1><TrustManagerFactory.X509>",
                    0, getProvidersCount(filter));
        } else {
            assertEquals(
                    "Filter : <MessageDigest.SHA1><TrustManagerFactory.X509>",
                    getProvidersCount(filter), provTest.length);
        }
        filter = new Hashtable<String, String>();
        filter.put("CertificateFactory.X509", "");
        provTest = Security.getProviders(filter);
        if (provTest == null) {
            assertEquals("Filter : <CertificateFactory.X509>",
                    0, getProvidersCount(filter));
        } else {
            assertEquals("Filter : <CertificateFactory.X509>",
                    getProvidersCount(filter), provTest.length);
        }
        filter = new Hashtable<String, String>();
        filter.put("Provider.id name", "DRLCertFactory");
        provTest = Security.getProviders(filter);
        assertNull("Filter : <Provider.id name, DRLCertFactory >",
                provTest);
        try {
            filter = new Hashtable<String, String>();
            filter.put("Signature.SHA1withDSA", "512");
            provTest = Security.getProviders(filter);
            fail("InvalidParameterException should be thrown <Signature.SHA1withDSA><512>");
        } catch (InvalidParameterException e) {
        }
        try {
            filter = new Hashtable<String, String>();
            filter.put("Signature. KeySize", "512");
            provTest = Security.getProviders(filter);
            fail("InvalidParameterException should be thrown <Signature. KeySize><512>");
        } catch (InvalidParameterException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "SecurityException checking missed",
        method = "removeProvider",
        args = {java.lang.String.class}
    )
    public void test_removeProviderLjava_lang_String() {
        Provider test = new Support_TestProvider();
        Provider entrust = new Support_ProviderTrust();
        try {
            Security.removeProvider(test.getName());
            int addResult = Security.addProvider(test);
            assertTrue("Failed to add provider", addResult != -1);
            Security.removeProvider(test.getName());
            assertNull(
                    "the provider TestProvider is found after it was removed",
                    Security.getProvider(test.getName()));
            Security.removeProvider(entrust.getName());
            addResult = Security.addProvider(entrust);
            assertTrue("Failed to add provider", addResult != -1);
            Security.removeProvider(entrust.getName());
            Provider provTest[] = Security.getProviders();
            for (int i = 0; i < provTest.length; i++) {
                assertTrue(
                        "the provider entrust is found after it was removed",
                        provTest[i].getName() != entrust.getName());
            }
        } finally {
            Security.removeProvider(test.getName());
            Security.removeProvider(entrust.getName());
        }
    }
}

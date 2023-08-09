@TestTargetClass(PKIXParameters.class)
public class PKIXParametersTest extends TestCase {
    private final static String testIssuer =
        "CN=VM,OU=DRL Security,O=Intel,L=Novosibirsk,ST=NSO,C=RU";
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "PKIXParameters",
        args = {java.util.Set.class}
    )
    public final void testPKIXParametersSet01()
            throws InvalidAlgorithmParameterException {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        CertPathParameters cpp = new PKIXParameters(taSet);
        assertTrue(cpp instanceof PKIXParameters);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify exceptions.",
        method = "PKIXParameters",
        args = {java.util.Set.class}
    )
    @SuppressWarnings("unchecked")
    public final void testPKIXParametersSet02()
            throws InvalidAlgorithmParameterException {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        HashSet<TrustAnchor> originalSet = (HashSet<TrustAnchor>) taSet;
        HashSet<TrustAnchor> originalSetCopy = (HashSet<TrustAnchor>) originalSet
                .clone();
        PKIXParameters pp = new PKIXParameters(originalSetCopy);
        originalSetCopy.clear();
        Set returnedSet = pp.getTrustAnchors();
        assertEquals(originalSet, returnedSet);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "PKIXParameters",
        args = {java.util.Set.class}
    )
    public final void testPKIXParametersSet03() throws Exception {
        try {
            new PKIXParameters((Set<TrustAnchor>) null);
            fail("NPE expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies InvalidAlgorithmParameterException.",
        method = "PKIXParameters",
        args = {java.util.Set.class}
    )
    public final void testPKIXParametersSet04() {
        try {
            new PKIXParameters(new HashSet<TrustAnchor>());
            fail("InvalidAlgorithmParameterException expected");
        } catch (InvalidAlgorithmParameterException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClassCastException.",
        method = "PKIXParameters",
        args = {java.util.Set.class}
    )
    @SuppressWarnings("unchecked")
    public final void testPKIXParametersSet05() throws Exception {
        Set taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        assertTrue(taSet.add(new Object()));
        try {
            new PKIXParameters(taSet);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "PKIXParameters",
        args = {java.security.KeyStore.class}
    )
    public final void testPKIXParametersKeyStore03() throws Exception {
        try {
            new PKIXParameters((KeyStore) null);
            fail("NPE expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public final void testClone() throws InvalidAlgorithmParameterException {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters cpp = new PKIXParameters(taSet);
        PKIXParameters cppc = (PKIXParameters) cpp.clone();
        assertEquals(cpp.getPolicyQualifiersRejected(), cppc
                .getPolicyQualifiersRejected());
        assertEquals(cpp.getCertPathCheckers(), cppc.getCertPathCheckers());
        assertEquals(cpp.getCertStores(), cppc.getCertStores());
        assertEquals(cpp.getDate(), cppc.getDate());
        assertEquals(cpp.getInitialPolicies(), cppc.getInitialPolicies());
        assertEquals(cpp.getSigProvider(), cppc.getSigProvider());
        assertEquals(cpp.getTargetCertConstraints(), cppc
                .getTargetCertConstraints());
        assertEquals(cpp.getTrustAnchors(), cppc.getTrustAnchors());
        assertEquals(cpp.isAnyPolicyInhibited(), cppc.isAnyPolicyInhibited());
        assertEquals(cpp.isExplicitPolicyRequired(), cppc
                .isExplicitPolicyRequired());
        assertEquals(cpp.isPolicyMappingInhibited(), cppc
                .isPolicyMappingInhibited());
        assertEquals(cpp.isRevocationEnabled(), cppc.isRevocationEnabled());
        cpp.setDate(Calendar.getInstance().getTime());
        cpp.setPolicyQualifiersRejected(!cppc.getPolicyQualifiersRejected());
        assertFalse(cpp.getDate().equals(cppc.getDate()));
        assertFalse(cpp.getPolicyQualifiersRejected() == cppc
                .getPolicyQualifiersRejected());
        cppc.setExplicitPolicyRequired(!cpp.isExplicitPolicyRequired());
        cppc.setRevocationEnabled(!cpp.isRevocationEnabled());
        assertFalse(cpp.isExplicitPolicyRequired() == cppc
                .isExplicitPolicyRequired());
        assertFalse(cpp.isRevocationEnabled() == cppc.isRevocationEnabled());
        PKIXParameters cpp1 = null;
        try {
            cpp1.clone();
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPolicyQualifiersRejected",
        args = {}
    )
    public final void testGetPolicyQualifiersRejected() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        assertTrue(p.getPolicyQualifiersRejected());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setPolicyQualifiersRejected",
        args = {boolean.class}
    )
    public final void testSetPolicyQualifiersRejected() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        p.setPolicyQualifiersRejected(false);
        assertFalse("setFalse", p.getPolicyQualifiersRejected());
        p.setPolicyQualifiersRejected(true);
        assertTrue("setTrue", p.getPolicyQualifiersRejected());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isAnyPolicyInhibited",
        args = {}
    )
    public final void testIsAnyPolicyInhibited() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        assertFalse(p.isAnyPolicyInhibited());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setAnyPolicyInhibited",
        args = {boolean.class}
    )
    public final void testSetAnyPolicyInhibited() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        p.setAnyPolicyInhibited(true);
        assertTrue("setTrue", p.isAnyPolicyInhibited());
        p.setAnyPolicyInhibited(false);
        assertFalse("setFalse", p.isAnyPolicyInhibited());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isExplicitPolicyRequired",
        args = {}
    )
    public final void testIsExplicitPolicyRequired() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        assertFalse(p.isExplicitPolicyRequired());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setExplicitPolicyRequired",
        args = {boolean.class}
    )
    public final void testSetExplicitPolicyRequired() throws Exception { 
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        p.setExplicitPolicyRequired(true);
        assertTrue("setTrue", p.isExplicitPolicyRequired());
        p.setExplicitPolicyRequired(false);
        assertFalse("setFalse", p.isExplicitPolicyRequired());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isPolicyMappingInhibited",
        args = {}
    )
    public final void testIsPolicyMappingInhibited() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        assertFalse(p.isPolicyMappingInhibited());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        TestUtils.initCertPathSSCertChain();
        Set<TrustAnchor> taSet2 = Collections.singleton(new TrustAnchor(
               TestUtils.rootCertificateSS, null));
        p = new PKIXParameters(taSet2);
        assertFalse(p.isPolicyMappingInhibited());
        p.setPolicyMappingInhibited(true);
        assertTrue(p.isRevocationEnabled());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setPolicyMappingInhibited",
        args = {boolean.class}
    )
    public final void testSetPolicyMappingInhibited() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        p.setPolicyMappingInhibited(true);
        assertTrue("setTrue", p.isPolicyMappingInhibited());
        p.setPolicyMappingInhibited(false);
        assertFalse("setFalse", p.isPolicyMappingInhibited());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isRevocationEnabled",
        args = {}
    )
    public final void testIsRevocationEnabled() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        assertTrue(p.isRevocationEnabled());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
       TestUtils.initCertPathSSCertChain();
       Set<TrustAnchor> taSet2 = Collections.singleton(new TrustAnchor(
              TestUtils.rootCertificateSS, null));
       p = new PKIXParameters(taSet2);
       assertTrue(p.isRevocationEnabled());
       p.setRevocationEnabled(false);
       assertFalse(p.isRevocationEnabled());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setRevocationEnabled",
        args = {boolean.class}
    )
    public final void testSetRevocationEnabled() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        p.setRevocationEnabled(false);
        assertFalse("setFalse", p.isRevocationEnabled());
        p.setRevocationEnabled(true);
        assertTrue("setTrue", p.isRevocationEnabled());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSigProvider",
        args = {}
    )
    public final void testGetSigProvider() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        assertNull("not set", p.getSigProvider());
        p.setSigProvider("Some Provider");
        assertNotNull("set", p.getSigProvider());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSigProvider",
        args = {java.lang.String.class}
    )
    public final void testSetSigProvider() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        String sigProviderName = "Some Provider";
        p.setSigProvider(sigProviderName);
        assertTrue("set", sigProviderName.equals(p.getSigProvider()));
        p.setSigProvider(null);
        assertNull("unset", p.getSigProvider());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getTargetCertConstraints method returns null, if no constraints are defined.",
        method = "getTargetCertConstraints",
        args = {}
    )
    public final void testGetTargetCertConstraints01() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        assertNull(p.getTargetCertConstraints());
    }
    @TestTargets({
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies that returned CertSelector is cloned to protect against subsequent modifications.",
        method = "setTargetCertConstraints",
        args = {java.security.cert.CertSelector.class}
    ),
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getTargetCertConstraints",
            args = {}
        )
    })
    public final void testGetTargetCertConstraints02() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        X509CertSelector x509cs = new X509CertSelector();
        PKIXParameters p = new PKIXParameters(taSet);
        p.setTargetCertConstraints(x509cs);
        X509CertSelector cs1 = (X509CertSelector)p.getTargetCertConstraints();
        X509CertSelector cs3 = (X509CertSelector)p.getTargetCertConstraints();
        assertNotNull(cs1);
        cs1.setIssuer(testIssuer);
        X509CertSelector cs2 = (X509CertSelector)p.getTargetCertConstraints();
        assertNotSame("notTheSame", cs1, cs2);
        assertFalse("internal stateNotChanged", testIssuer.equals(cs2.getIssuerAsString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "setTargetCertConstraints",
        args = {java.security.cert.CertSelector.class}
    )
    public final void testSetTargetCertConstraints01() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        X509CertSelector x509cs = new X509CertSelector();
        x509cs.setIssuer(testIssuer);
        PKIXParameters p = new PKIXParameters(taSet);
        p.setTargetCertConstraints(x509cs);
        assertEquals("set", testIssuer, ((X509CertSelector) p
                .getTargetCertConstraints()).getIssuerAsString());
        p.setTargetCertConstraints(null);
        assertNull("unset", p.getTargetCertConstraints());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Additional test.",
        method = "setTargetCertConstraints",
        args = {java.security.cert.CertSelector.class}
    )
    public final void testSetTargetCertConstraints02() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        X509CertSelector x509cs = new X509CertSelector();
        PKIXParameters p = new PKIXParameters(taSet);
        p.setTargetCertConstraints(x509cs);
        x509cs.setIssuer(testIssuer);
        X509CertSelector x509cs1 = (X509CertSelector) p
                .getTargetCertConstraints();
        assertFalse(testIssuer.equals(x509cs1.getIssuerAsString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getCertStores method returns empty list, but not null.",
        method = "getCertStores",
        args = {}
    )
    public final void testGetCertStores01() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        assertNotNull("notNull", p.getCertStores());
        assertTrue("isEmpty", p.getCertStores().isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getCertStores method returns an immutable List of CertStores.",
        method = "getCertStores",
        args = {}
    )
    public final void testGetCertStores02() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        List<CertStore> cs = p.getCertStores();
        try {
            cs.add((CertStore) (new Object()));
            fail("must be immutable");
        } catch (Exception e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify ClassCastException.",
        method = "setCertStores",
        args = {java.util.List.class}
    )
    public final void testSetCertStores01() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        p.setCertStores(TestUtils.getCollectionCertStoresList());
        assertFalse(p.getCertStores().isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify ClassCastException.",
        method = "setCertStores",
        args = {java.util.List.class}
    )
    public final void testSetCertStores02() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        p.setCertStores(null);
        assertNotNull("notNull1", p.getCertStores());
        assertTrue("isEmpty1", p.getCertStores().isEmpty());
        p.setCertStores(new ArrayList<CertStore>());
        assertNotNull("notNull2", p.getCertStores());
        assertTrue("isEmpty2", p.getCertStores().isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that the list is copied to protect against subsequent modifications.",
        method = "setCertStores",
        args = {java.util.List.class}
    )
    public final void testSetCertStores03() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        List<CertStore> l = TestUtils.getCollectionCertStoresList();
        p.setCertStores(l);
        l.clear();
        assertFalse(p.getCertStores().isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClassCastException.",
        method = "setCertStores",
        args = {java.util.List.class}
    )
    @SuppressWarnings("unchecked")
    public final void testSetCertStores04() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        List l = TestUtils.getCollectionCertStoresList();
        assertTrue(l.add(new Object()));
        try {
            p.setCertStores(l);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify ClassCastException.",
        method = "addCertStore",
        args = {java.security.cert.CertStore.class}
    )
    public final void testAddCertStore01() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        p.addCertStore(CertStore.getInstance("Collection",
                new CollectionCertStoreParameters()));
        assertFalse(p.getCertStores().isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "addCertStore",
        args = {java.security.cert.CertStore.class}
    )
    public final void testAddCertStore02() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        p.addCertStore(null);
        assertTrue(p.getCertStores().isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getCertPathCheckers method returns not empty list.",
        method = "getCertPathCheckers",
        args = {}
    )
    public final void testGetCertPathCheckers01() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        List l = p.getCertPathCheckers();
        assertNotNull("notNull", l);
        assertTrue("isEmpty", l.isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getCertPathCheckers method returns an immutable List of PKIXCertPathChecker objects.",
        method = "getCertPathCheckers",
        args = {}
    )
    public final void testGetCertPathCheckers02() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        List<PKIXCertPathChecker> l = p.getCertPathCheckers();
        try {
            l.add((PKIXCertPathChecker) new Object());
            fail("must be immutable");
        } catch (Exception e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that the returned List is immutable, and each PKIXCertPathChecker in the List is cloned to protect against subsequent modifications.",
        method = "getCertPathCheckers",
        args = {}
    )
    public final void testGetCertPathCheckers03() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        PKIXCertPathChecker cpc = TestUtils.getTestCertPathChecker();
        List<PKIXCertPathChecker> l = new ArrayList<PKIXCertPathChecker>();
        assertTrue("addedOk", l.add(cpc));
        p.setCertPathCheckers(l);
        PKIXCertPathChecker cpc1 = p.getCertPathCheckers().get(0);
        cpc1.init(true);
        assertTrue("modifiedOk", cpc1.isForwardCheckingSupported());
        PKIXCertPathChecker cpc2 = p.getCertPathCheckers().get(0);
        assertFalse("isCloned", cpc2.isForwardCheckingSupported());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify exceptions.",
        method = "setCertPathCheckers",
        args = {java.util.List.class}
    )
    public final void testSetCertPathCheckers01() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        PKIXCertPathChecker cpc = TestUtils.getTestCertPathChecker();
        List<PKIXCertPathChecker> l = new ArrayList<PKIXCertPathChecker>();
        assertTrue("addedOk", l.add(cpc));
        p.setCertPathCheckers(l);
        List l1 = p.getCertPathCheckers();
        assertNotNull("notNull", l1);
        assertFalse("isNotEmpty", l1.isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify exceptions.",
        method = "setCertPathCheckers",
        args = {java.util.List.class}
    )
    public final void testSetCertPathCheckers02() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        p.setCertPathCheckers(null);
        List<PKIXCertPathChecker> l1 = p.getCertPathCheckers();
        assertNotNull("notNull1", l1);
        assertTrue("isEmpty1", l1.isEmpty());
        p.setCertPathCheckers(new ArrayList<PKIXCertPathChecker>());
        List l2 = p.getCertPathCheckers();
        assertNotNull("notNull2", l2);
        assertTrue("isEmpty2", l2.isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify exceptions.",
        method = "setCertPathCheckers",
        args = {java.util.List.class}
    )
    public final void testSetCertPathCheckers03() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        PKIXCertPathChecker cpc = TestUtils.getTestCertPathChecker();
        List<PKIXCertPathChecker> l = new ArrayList<PKIXCertPathChecker>();
        assertTrue("addedOk", l.add(cpc));
        p.setCertPathCheckers(l);
        l.clear();
        assertFalse("isCopied", p.getCertPathCheckers().isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify exceptions.",
        method = "setCertPathCheckers",
        args = {java.util.List.class}
    )
    public final void testSetCertPathCheckers04() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        PKIXCertPathChecker cpc = TestUtils.getTestCertPathChecker();
        List<PKIXCertPathChecker> l = new ArrayList<PKIXCertPathChecker>();
        assertTrue("addedOk", l.add(cpc));
        p.setCertPathCheckers(l);
        cpc.init(true);
        PKIXCertPathChecker cpc1 = p.getCertPathCheckers().get(0);
        assertFalse("isCopied", cpc1.isForwardCheckingSupported());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verify exception.",
        method = "setCertPathCheckers",
        args = {java.util.List.class}
    )
    @SuppressWarnings("unchecked")
    public final void testSetCertPathCheckers05() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        PKIXCertPathChecker cpc = TestUtils.getTestCertPathChecker();
        List l = new ArrayList<PKIXCertPathChecker>();
        assertTrue("addedOk", l.add(cpc));
        assertTrue("addedOk", l.add(new Object()));
        try {
            p.setCertPathCheckers(l);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "addCertPathChecker",
        args = {java.security.cert.PKIXCertPathChecker.class}
    )
    public final void testAddCertPathChecker01() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        PKIXCertPathChecker cpc = TestUtils.getTestCertPathChecker();
        List<PKIXCertPathChecker> l = new ArrayList<PKIXCertPathChecker>();
        assertTrue("addedOk", l.add(cpc));
        p.setCertPathCheckers(l);
        PKIXCertPathChecker cpc1 = TestUtils.getTestCertPathChecker();
        cpc1.init(true);
        p.addCertPathChecker(cpc1);
        List l1 = p.getCertPathCheckers();
        assertEquals("listSize", 2, l1.size());
        assertFalse("order1", ((PKIXCertPathChecker) l1.get(0))
                .isForwardCheckingSupported());
        assertTrue("order2", ((PKIXCertPathChecker) l1.get(1))
                .isForwardCheckingSupported());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that if PKIXCertPathChecker parameter is null, the checker is ignored (not added to list).",
        method = "addCertPathChecker",
        args = {java.security.cert.PKIXCertPathChecker.class}
    )
    public final void testAddCertPathChecker02() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        PKIXCertPathChecker cpc = TestUtils.getTestCertPathChecker();
        List<PKIXCertPathChecker> l = new ArrayList<PKIXCertPathChecker>();
        assertTrue("addedOk", l.add(cpc));
        p.setCertPathCheckers(l);
        p.addCertPathChecker(null);
        List l1 = p.getCertPathCheckers();
        assertEquals("listSize", 1, l1.size());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that PKIXCertPathChecker is cloned to protect against subsequent modifications.",
        method = "addCertPathChecker",
        args = {java.security.cert.PKIXCertPathChecker.class}
    )
    public final void testAddCertPathChecker03() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        PKIXCertPathChecker cpc = TestUtils.getTestCertPathChecker();
        p.addCertPathChecker(cpc);
        cpc.init(true);
        List l = p.getCertPathCheckers();
        PKIXCertPathChecker cpc1 = (PKIXCertPathChecker) l.get(0);
        assertEquals("listSize", 1, l.size());
        assertFalse("isCopied", cpc1.isForwardCheckingSupported());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getDate",
        args = {}
    )
    public final void testGetDate01() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        assertNull("null", p.getDate());
        Date currentDate = new Date();
        p.setDate(currentDate);
        assertEquals("notNull", currentDate, p.getDate());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that returned Date is copied to protect against subsequent modifications.",
        method = "getDate",
        args = {}
    )
    public final void testGetDate02() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        Date currentDate = new Date();
        p.setDate((Date) currentDate.clone());
        Date ret1 = p.getDate();
        ret1.setTime(0L);
        assertEquals(currentDate, p.getDate());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setDate",
        args = {java.util.Date.class}
    )
    @AndroidOnly("On the RI p.setDate(null) does not reset the date to null "
            + "as specified.")
    public final void test_setDateLjava_util_Date() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        assertNotNull("could not create test TrustAnchor set", taSet);
        PKIXParameters p = new PKIXParameters(taSet);
        p.setDate(null);
        assertNull(p.getDate());
        p = new PKIXParameters(taSet);
        Date toBeSet = new Date(555L);
        p.setDate(toBeSet);
        assertEquals(555L, p.getDate().getTime());
        toBeSet.setTime(0L);
        assertEquals(555L, p.getDate().getTime());
        p.setDate(new Date(333L));
        assertEquals(333L, p.getDate().getTime());
        p = new PKIXParameters(taSet);
        p.setDate(new Date(555L));
        p.setDate(null); 
        assertNull(p.getDate());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getInitialPolicies method returns an empty Set and never null.",
        method = "getInitialPolicies",
        args = {}
    )
    public final void testGetInitialPolicies01() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        assertNotNull("notNull", p.getInitialPolicies());
        assertTrue("isEmpty", p.getInitialPolicies().isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getInitialPolicies method returns an immutable Set of initial policy in String format.",
        method = "getInitialPolicies",
        args = {}
    )
    public final void testGetInitialPolicies02() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        Set<String> s = p.getInitialPolicies();
        try {
            s.add((String) new Object());
            fail("must be immutable");
        } catch (Exception e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify ClassCastException.",
        method = "setInitialPolicies",
        args = {java.util.Set.class}
    )
    public final void testSetInitialPolicies01() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        Set<String> s = new HashSet<String>();
        s.add("1.2.3.4.5.6.7");
        PKIXParameters p = new PKIXParameters(taSet);
        p.setInitialPolicies(s);
        assertEquals(1, p.getInitialPolicies().size());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "setInitialPolicies",
        args = {java.util.Set.class}
    )
    public final void testSetInitialPolicies02() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        p.setInitialPolicies(null);
        assertTrue(p.getInitialPolicies().isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify ClassCastException.",
        method = "setInitialPolicies",
        args = {java.util.Set.class}
    )
    public final void testSetInitialPolicies03() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        p.setInitialPolicies(new HashSet<String>());
        assertTrue(p.getInitialPolicies().isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that Set is copied to protect against subsequent modifications.",
        method = "setInitialPolicies",
        args = {java.util.Set.class}
    )
    public final void testSetInitialPolicies04() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        Set<String> s = new HashSet<String>();
        s.add("1.2.3.4.5.6.7");
        s.add("1.2.3.4.5.6.8");
        PKIXParameters p = new PKIXParameters(taSet);
        p.setInitialPolicies(s);
        s.clear();
        assertEquals(2, p.getInitialPolicies().size());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClassCastException.",
        method = "setInitialPolicies",
        args = {java.util.Set.class}
    )
    @SuppressWarnings("unchecked")
    public final void testSetInitialPolicies05() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        Set s = new HashSet();
        s.add("1.2.3.4.5.6.7");
        s.add(new Object());
        PKIXParameters p = new PKIXParameters(taSet);
        try {
            p.setInitialPolicies(s);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getTrustAnchors returns an immutable Set of TrustAnchors, and never null.",
        method = "getTrustAnchors",
        args = {}
    )
    public final void testGetTrustAnchors01() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        assertNotNull("notNull", p.getTrustAnchors());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getTrustAnchors returns an immutable set of TrustAnchors, and never null.",
        method = "getTrustAnchors",
        args = {}
    )
    public final void testGetTrustAnchors02() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        Set<TrustAnchor> s = p.getTrustAnchors();
        try {
            s.add((TrustAnchor) new Object());
            fail("must be immutable");
        } catch (Exception e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify exceptions.",
        method = "setTrustAnchors",
        args = {java.util.Set.class}
    )
    public final void testSetTrustAnchors01() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        Set<TrustAnchor> taSet1 = TestUtils.getTrustAnchorSet();
        PKIXParameters p = new PKIXParameters(taSet);
        p.setTrustAnchors(taSet1);
        assertFalse(p.getTrustAnchors().isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies InvalidAlgorithmParameterException.",
        method = "setTrustAnchors",
        args = {java.util.Set.class}
    )
    public final void testSetTrustAnchors02() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        try {
            p.setTrustAnchors(new HashSet<TrustAnchor>());
            fail("InvalidAlgorithmParameterException expected");
        } catch (InvalidAlgorithmParameterException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "setTrustAnchors",
        args = {java.util.Set.class}
    )
    public final void testSetTrustAnchors03() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        try {
            p.setTrustAnchors(null);
            fail("NPE expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClassCastException.",
        method = "setTrustAnchors",
        args = {java.util.Set.class}
    )
    @SuppressWarnings("unchecked")
    public final void testSetTrustAnchors04() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        Set s = new HashSet(p.getTrustAnchors());
        s.add(new Object());
        try {
            p.setTrustAnchors(s);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public final void testToString() throws Exception {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName()
                    + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXParameters(taSet);
        assertNotNull(p.toString());
        PKIXParameters p1 = null;
        try {
            p1.toString();
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies everything except null argument",
        method = "PKIXParameters",
        args = {java.security.KeyStore.class}
    )
    @BrokenTest("Fails in CTS environment, but passes in CoreTestRunner")
    public final void testPKIXParametersKeyStore04() throws Exception {
        KeyStore store = KeyStore.getInstance("PKCS12");
        KeyStoreTestPKCS12 k = new KeyStoreTestPKCS12();
        ByteArrayInputStream stream = new ByteArrayInputStream(k.keyStoreData);
        try {
            PKIXParameters p = new PKIXParameters(store);
        } catch (KeyStoreException e) {
        }
        store = KeyStore.getInstance("PKCS12");
        store.load(stream, new String(KeyStoreTestPKCS12.keyStorePassword)
                .toCharArray());
        stream.close();
        try {
              PKIXParameters p = new PKIXParameters(store);
        } catch (InvalidAlgorithmParameterException e) {
        }
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(null,null);
        keystore.setCertificateEntry("test", TestUtils.rootCertificateSS);
        PKIXParameters p = new PKIXParameters(keystore);
    }
}

@TestTargetClass(ECFieldF2m.class)
public class ECFieldF2mTest extends TestCase {
    private static final class ECFieldF2mDomainParams {
        static final NullPointerException NPE = new NullPointerException();
        static final IllegalArgumentException IArgE = new IllegalArgumentException();
        final int m;
        final BigInteger rp;
        final int[] ks;
        final Exception x;
        ECFieldF2mDomainParams(final int m,
                final BigInteger rp,
                final int[] ks,
                final Exception expectedException) {
            this.m = m;
            this.rp = rp;
            this.ks = ks;
            this.x = expectedException;
        }
    }
    private final ECFieldF2mDomainParams[] intCtorTestParameters =
        new ECFieldF2mDomainParams[] {
            new ECFieldF2mDomainParams(1, null, null, null),
            new ECFieldF2mDomainParams(Integer.MAX_VALUE, null, null, null),
            new ECFieldF2mDomainParams(0, null, null, ECFieldF2mDomainParams.IArgE),
            new ECFieldF2mDomainParams(-1, null, null, ECFieldF2mDomainParams.IArgE)
        };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ECFieldF2m",
        args = {int.class}
    )
    public final void testECFieldF2mint() {
        for(int i=0; i<intCtorTestParameters.length; i++) {
            ECFieldF2mDomainParams tp = intCtorTestParameters[i];
            try {
                new ECFieldF2m(tp.m);
                if (tp.x != null) {
                    fail(getName() + ", set " + i +
                            " FAILED: expected exception has not been thrown");
                }
            } catch (Exception e){
                if (tp.x == null || !e.getClass().isInstance(tp.x)) {
                    fail(getName() + ", set " + i +
                            " FAILED: unexpected " + e);
                }
            }
        }
    }
    private final ECFieldF2mDomainParams[] constructorTestParameters =
        new ECFieldF2mDomainParams[] {
            new ECFieldF2mDomainParams(
                    1999,
                    new BigInteger("57406534763712726211641660058884099201115885104434760023882136841288313069618515692832974315825313495922298231949373138672355948043152766571296567808332659269564994572656140000344389574120022435714463495031743122390807731823194181973658513020233176985452498279081199404472314802811655824768082110985166340672084454492229252801189742403957029450467388250214501358353312915261004066118140645880633941658603299497698209063510889929202021079926591625770444716951045960277478891794836019580040978908928741972740865961716524153209532713803393514722581342474556943840519615081302148762454520131486413662191617"),
                    new int[] {367},
                    null),
            new ECFieldF2mDomainParams(
                    2000,
                    new BigInteger("114813069527425452423283320117768198402231770208869520047764273682576626139237031385665948631650626991844596463898746277344711896086305533142593135616665318539129989145312280000688779148240044871428926990063486244781615463646388363947317026040466353970904996558162398808944629605623311649536164221970332681364606313754094036473740741389411285817465477407288087941692709593079057904974473325399237449961796178150263073811552931156681807161003582337510008648338765664631815874608789366699668224806907571505750798647855797220056285479869767291137153732790597348308446887230584637235716444920907512810569735"),
                    new int[] {981,2,1},
                    null),
            new ECFieldF2mDomainParams(
                    1963,
                    null,
                    null,
                    ECFieldF2mDomainParams.NPE),
            new ECFieldF2mDomainParams(
                    1963,
                    new BigInteger("114813069527425452423283320117768198402231770208869520047764273682576626139237031385665948631650626991844596463898746277344711896086305533142593135616665318539129989145312280000688779148240044871428926990063486244781615463646388363947317026040466353970904996558162398808944629605623311649536164221970332681364606313754094036473740741389411285817465477407288087941692709593079057904974473325399237449961796178150263073811552931156681807161003582337510008648338765664631815874608789366699668224806907571505750798647855797220056285479869767291137153732790597348308446887230584637235716444920907512810569734"),
                    new int[] {981,2},
                    ECFieldF2mDomainParams.IArgE),
            new ECFieldF2mDomainParams(
                    1963,
                    new BigInteger("5"),
                    new int[] {981,124,2,1},
                    ECFieldF2mDomainParams.IArgE),
            new ECFieldF2mDomainParams(
                    1999,
                    new BigInteger("5"),
                    new int[] {1999},
                    ECFieldF2mDomainParams.IArgE),
            new ECFieldF2mDomainParams(
                    1999,
                    new BigInteger("5"),
                    new int[] {0},
                    ECFieldF2mDomainParams.IArgE),
            new ECFieldF2mDomainParams(
                    2000,
                    new BigInteger("5"),
                    new int[] {2000,2,1},
                    ECFieldF2mDomainParams.IArgE),
            new ECFieldF2mDomainParams(
                    2000,
                    new BigInteger("5"),
                    new int[] {981,2,0},
                    ECFieldF2mDomainParams.IArgE),
            new ECFieldF2mDomainParams(
                    -5,
                    new BigInteger("114813069527425452423283320117768198402231770208869520047764273682576626139237031385665948631650626991844596463898746277344711896086305533142593135616665318539129989145312280000688779148240044871428926990063486244781615463646388363947317026040466353970904996558162398808944629605623311649536164221970332681364606313754094036473740741389411285817465477407288087941692709593079057904974473325399237449961796178150263073811552931156681807161003582337510008648338765664631815874608789366699668224806907571505750798647855797220056285479869767291137153732790597348308446887230584637235716444920907512810569735"),
                    new int[] {981,2,1},
                    ECFieldF2mDomainParams.IArgE),
            new ECFieldF2mDomainParams(
                    2000,
                    new BigInteger("5"),
                    new int[] {981,1,2},
                    ECFieldF2mDomainParams.IArgE),
            new ECFieldF2mDomainParams(
                    2000,
                    new BigInteger("5"),
                    new int[3],
                    ECFieldF2mDomainParams.IArgE),
            new ECFieldF2mDomainParams(
                    2000,
                    new BigInteger("0"),
                    new int[0],
                    ECFieldF2mDomainParams.IArgE),
        };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ECFieldF2m",
        args = {int.class, int[].class}
    )
    public final void testECFieldF2mintintArray() {
        for(int i=0; i<constructorTestParameters.length; i++) {
            ECFieldF2mDomainParams tp = constructorTestParameters[i];
            try {
                ECFieldF2m test = new ECFieldF2m(tp.m, tp.ks);
                if (tp.x != null) {
                    fail(getName() + ", set " + i +
                            " FAILED: expected exception has not been thrown");
                }
            } catch (Exception e){
                if (tp.x == null || !e.getClass().isInstance(tp.x)) {
                    fail(getName() + ", set " + i +
                            " FAILED: unexpected " + e);
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "ECFieldF2m",
        args = {int.class, java.math.BigInteger.class}
    )
    public final void testECFieldF2mintBigInteger() {
        for(int i=0; i<constructorTestParameters.length; i++) {
            ECFieldF2mDomainParams tp = constructorTestParameters[i];
            try {
                new ECFieldF2m(tp.m, tp.rp);
                if (tp.x != null) {
                    fail(getName() + ", set " + i +
                            " FAILED: expected exception has not been thrown");
                }
            } catch (Exception e){
                if (tp.x == null || !e.getClass().isInstance(tp.x)) {
                    fail(getName() + ", set " + i +
                            " FAILED: unexpected " + e);
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public final void testHashCode01() {
        ECFieldF2m f = new ECFieldF2m(2000);
        int hc = f.hashCode();
        assertTrue(hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public final void testHashCode02() {
        ECFieldF2m f = new ECFieldF2m(2000, new int[] {981, 2, 1});
        int hc = f.hashCode();
        assertTrue(hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public final void testHashCode03() {
        assertTrue(new ECFieldF2m(111).hashCode() ==
                   new ECFieldF2m(111).hashCode());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public final void testHashCode04() {
        assertTrue(new ECFieldF2m(2000, new int[] {981, 2, 1}).hashCode() ==
                   new ECFieldF2m(2000, new int[] {981, 2, 1}).hashCode());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public final void testHashCode05() {
        assertTrue(new ECFieldF2m(2000, new int[] {981, 2, 1}).hashCode() ==
                   new ECFieldF2m(2000, BigInteger.valueOf(0L).
                                        setBit(0).setBit(1).setBit(2).
                                        setBit(981).setBit(2000)).hashCode());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject01() {
        ECFieldF2m obj = new ECFieldF2m(1999, new int[] {367});
        assertTrue(obj.equals(obj));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Simple test. Doesn't verify other cases.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject02() {
        assertTrue(new ECFieldF2m(43).equals(new ECFieldF2m(43)));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject03() {
        assertTrue(new ECFieldF2m(1999, new int[] {367}).equals(
                   new ECFieldF2m(1999, BigInteger.valueOf(0L).
                                        setBit(0).setBit(367).setBit(1999))));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject04() {
        ECFieldF2m f1 = new ECFieldF2m(2000, new int[] {981, 2, 1});
        ECFieldF2m f2 = new ECFieldF2m(2000, BigInteger.valueOf(0L).
                setBit(0).setBit(1).setBit(2).
                setBit(981).setBit(2000)); 
        assertTrue(f1.equals(f2) && f2.equals(f1));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject05() {
        ECFieldF2m f1 = new ECFieldF2m(2000);
        ECFieldF2m f2 = new ECFieldF2m(2000, BigInteger.valueOf(0L).
                setBit(0).setBit(1).setBit(2).
                setBit(981).setBit(2000)); 
        assertFalse(f1.equals(f2) || f2.equals(f1));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject06() {
        assertFalse(new ECFieldF2m(2000).equals(null));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject07() {
        assertFalse(new ECFieldF2m(2000).equals(new Object()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFieldSize",
        args = {}
    )
    public final void testGetFieldSize() {
        assertEquals(2000, new ECFieldF2m(2000).getFieldSize());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getM",
        args = {}
    )
    public final void testGetM() {
        assertEquals(2000, new ECFieldF2m(2000).getM());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getMidTermsOfReductionPolynomial method returns mid terms of reduction polynomial.",
        method = "getMidTermsOfReductionPolynomial",
        args = {}
    )
    public final void testGetMidTermsOfReductionPolynomial01() {
        int[] a = new int[] {981,2,1};
        int[] b = new ECFieldF2m(2000,
                BigInteger.valueOf(0L).setBit(0).setBit(1).
                setBit(2).setBit(981).setBit(2000)).
                getMidTermsOfReductionPolynomial();
        assertTrue(Arrays.equals(a, b));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getMidTermsOfReductionPolynomial method returns null for normal basis.",
        method = "getMidTermsOfReductionPolynomial",
        args = {}
    )
    public final void testGetMidTermsOfReductionPolynomial02() {
        assertNull(new ECFieldF2m(2000).getMidTermsOfReductionPolynomial());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getMidTermsOfReductionPolynomial method returns mid terms of reduction polynomial.",
        method = "getMidTermsOfReductionPolynomial",
        args = {}
    )
    public final void testGetMidTermsOfReductionPolynomial03() {
        int[] a = new int[] {367};
        int[] b = new ECFieldF2m(1999, a).getMidTermsOfReductionPolynomial();
        assertTrue(Arrays.equals(a, b));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getReductionPolynomial method returns reduction polynomial.",
        method = "getReductionPolynomial",
        args = {}
    )
    public final void testGetReductionPolynomial01() {
        BigInteger rp = BigInteger.valueOf(0L).setBit(0).setBit(1).setBit(2).
        setBit(981).setBit(2000);
        assertTrue(new ECFieldF2m(2000, rp).getReductionPolynomial().equals(rp));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getReductionPolynomial method returns null for normal basis.",
        method = "getReductionPolynomial",
        args = {}
    )
    public final void testGetReductionPolynomial02() {
        assertNull(new ECFieldF2m(2000).getReductionPolynomial());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that object state is preserved against modifications through array reference passed to the constructor.",
        method = "ECFieldF2m",
        args = {int.class, int[].class}
    )
    public final void testIsStatePreserved01() {
        int[] a = new int[] {367};
        int[] aCopy = a.clone();
        ECFieldF2m f = new ECFieldF2m(1999, aCopy);
        aCopy[0] = 5;
        assertTrue(Arrays.equals(a, f.getMidTermsOfReductionPolynomial()));        
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies that object state is preserved against modifications through array reference returned by getMidTermsOfReductionPolynomial() method.",
            method = "ECFieldF2m",
            args = {int.class, int[].class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies that object state is preserved against modifications through array reference returned by getMidTermsOfReductionPolynomial() method.",
            method = "getMidTermsOfReductionPolynomial",
            args = {}
        )
    })
    public final void testIsStatePreserved02() {
        int[] a = new int[] {981,2,1};
        int[] aCopy = a.clone();
        ECFieldF2m f = new ECFieldF2m(2000, aCopy);
        f.getMidTermsOfReductionPolynomial()[0] = 1532;
        assertTrue(Arrays.equals(a, f.getMidTermsOfReductionPolynomial()));        
    }
}

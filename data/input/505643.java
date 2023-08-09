@TestTargetClass(KeyRep.class)
public class KeyRepTest extends TestCase {
    private static final Set<String> keyFactoryAlgorithm;
    static {
        keyFactoryAlgorithm = Security.getAlgorithms("KeyFactory");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyRep",
        args = {java.security.KeyRep.Type.class, java.lang.String.class, java.lang.String.class, byte[].class}
    )
    public final void testKeyRep01() {
        try {
            assertNotNull(new KeyRep(KeyRep.Type.SECRET, "", "", new byte[] {}));
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
        try {
            assertNotNull(new KeyRep(KeyRep.Type.PUBLIC, "", "", new byte[] {}));
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
        try {
            assertNotNull(new KeyRep(KeyRep.Type.PRIVATE, "", "", new byte[] {}));
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyRep",
        args = {java.security.KeyRep.Type.class, java.lang.String.class, java.lang.String.class, byte[].class}
    )
    public final void testKeyRep02() {
        try {
            new KeyRep(null, "", "", new byte[] {});
            fail("NullPointerException has not been thrown (type)");
        } catch (NullPointerException ok) {
        }
        try {
            new KeyRep(KeyRep.Type.SECRET, null, "", new byte[] {});
            fail("NullPointerException has not been thrown (alg)");
        } catch (NullPointerException ok) {
        }
        try {
            new KeyRep(KeyRep.Type.PRIVATE, "", null, new byte[] {});
            fail("NullPointerException has not been thrown (format)");
        } catch (NullPointerException ok) {
        }
        try {
            new KeyRep(KeyRep.Type.PUBLIC, "", "", null);
            fail("NullPointerException has not been thrown (encoding)");
        } catch (NullPointerException ok) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "readResolve",
        args = {}
    )
    public final void testReadResolve01() throws ObjectStreamException {
        KeyRepChild kr = new KeyRepChild(KeyRep.Type.SECRET, "", "",
                new byte[] {});
        try {
            kr.readResolve();
            fail("NotSerializableException has not been thrown (no format)");
        } catch (NotSerializableException ok) {
        }
        kr = new KeyRepChild(KeyRep.Type.SECRET, "", "X.509", new byte[] {});
        try {
            kr.readResolve();
            fail("NotSerializableException has not been thrown (unacceptable format)");
        } catch (NotSerializableException ok) {
        }
        kr = new KeyRepChild(KeyRep.Type.SECRET, "", "RAW", new byte[] {});
        try {
            kr.readResolve();
            fail("NotSerializableException has not been thrown (empty key)");
        } catch (NotSerializableException ok) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "readResolve",
        args = {}
    )
    public final void testReadResolve02() throws ObjectStreamException {
        KeyRepChild kr = new KeyRepChild(KeyRep.Type.PUBLIC, "", "",
                new byte[] {});
        try {
            kr.readResolve();
            fail("NotSerializableException has not been thrown (no format)");
        } catch (NotSerializableException ok) {
        }
        kr = new KeyRepChild(KeyRep.Type.PUBLIC, "", "RAW", new byte[] {});
        try {
            kr.readResolve();
            fail("NotSerializableException has not been thrown (unacceptable format)");
        } catch (NotSerializableException ok) {
        }
        kr = new KeyRepChild(KeyRep.Type.PUBLIC, "bla-bla", "X.509",
                new byte[] {});
        try {
            kr.readResolve();
            fail("NotSerializableException has not been thrown (unknown KeyFactory algorithm)");
        } catch (NotSerializableException ok) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "readResolve",
        args = {}
    )
    public final void testReadResolve03() throws ObjectStreamException {
        KeyRepChild kr = new KeyRepChild(KeyRep.Type.PRIVATE, "", "",
                new byte[] {});
        try {
            kr.readResolve();
            fail("NotSerializableException has not been thrown (no format)");
        } catch (NotSerializableException ok) {
        }
        kr = new KeyRepChild(KeyRep.Type.PRIVATE, "", "RAW", new byte[] {});
        try {
            kr.readResolve();
            fail("NotSerializableException has not been thrown (unacceptable format)");
        } catch (NotSerializableException ok) {
        }
        kr = new KeyRepChild(KeyRep.Type.PRIVATE, "bla-bla", "PKCS#8",
                new byte[] {});
        try {
            kr.readResolve();
            fail("NotSerializableException has not been thrown (unknown KeyFactory algorithm)");
        } catch (NotSerializableException ok) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "readResolve",
        args = {}
    )
    public final void testReadResolve04() throws ObjectStreamException {
        if (keyFactoryAlgorithm.isEmpty()) {
            System.err.println(getName()
                    + ": skipped - no KeyFactory algorithms available");
            return;
        } else {
        }
        for (Iterator<String> i = keyFactoryAlgorithm.iterator(); i.hasNext();) {
            KeyRepChild kr = new KeyRepChild(KeyRep.Type.PUBLIC, i.next(),
                    "X.509", new byte[] { 1, 2, 3 });
            try {
                kr.readResolve();
                fail("NotSerializableException has not been thrown (no format)");
            } catch (NotSerializableException ok) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "readResolve",
        args = {}
    )
    public final void testReadResolve05() throws ObjectStreamException {
        if (keyFactoryAlgorithm.isEmpty()) {
            System.err.println(getName()
                    + ": skipped - no KeyFactory algorithms available");
            return;
        } else {
        }
        for (Iterator<String> i = keyFactoryAlgorithm.iterator(); i.hasNext();) {
            KeyRepChild kr = new KeyRepChild(KeyRep.Type.PRIVATE, i.next(),
                    "PKCS#8", new byte[] { 1, 2, 3 });
            try {
                kr.readResolve();
                fail("NotSerializableException has not been thrown (no format)");
            } catch (NotSerializableException ok) {
            }
        }
    }
    class KeyRepChild extends KeyRep {
        public KeyRepChild(KeyRep.Type type, String algorithm, String format,
                byte[] encoded) {
            super(type, algorithm, format, encoded);
        }
        public Object readResolve() throws ObjectStreamException {
            return super.readResolve();
        }
    }
}

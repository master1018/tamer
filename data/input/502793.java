@TestTargetClass(CollationKey.class) 
public class CollationKeyTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "compareTo",
        args = {java.text.CollationKey.class}
    )
    public void test_compareToLjava_text_CollationKey() {
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        CollationKey key1 = collator.getCollationKey("abc");
        CollationKey key2 = collator.getCollationKey("ABC");
        assertEquals("Should be equal", 0, key1.compareTo(key2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "compareTo",
        args = {java.lang.Object.class}
    )
    public void test_compareToLjava_lang_Object() {
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        CollationKey key1 = collator.getCollationKey("abc");
        CollationKey key2 = collator.getCollationKey("ABC");
        assertEquals("Should be equal", 0, key1.compareTo(key2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        CollationKey key1 = collator.getCollationKey("abc");
        CollationKey key2 = collator.getCollationKey("ABC");
        assertTrue("Should be equal", key1.equals(key2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSourceString",
        args = {}
    )
    public void test_getSourceString() {
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        assertTrue("Wrong source string1", collator.getCollationKey("abc")
                .getSourceString() == "abc");
        assertTrue("Wrong source string2", collator.getCollationKey("ABC")
                .getSourceString() == "ABC");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        CollationKey key1 = collator.getCollationKey("abc");
        CollationKey key2 = collator.getCollationKey("ABC");
        assertTrue("Should be equal", key1.hashCode() == key2.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toByteArray",
        args = {}
    )
    public void test_toByteArray() {
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        CollationKey key1 = collator.getCollationKey("abc");
        byte[] bytes = key1.toByteArray();
        assertTrue("Not enough bytes", bytes.length >= 3);
        try {
            collator = new RuleBasedCollator("= 1 , 2 ; 3 , 4 < 5 ; 6 , 7");
        } catch (ParseException e) {
            fail("ParseException");
            return;
        }
        byte[] bytes1 = collator.getCollationKey("12").toByteArray();
        byte[] bytes2 = collator.getCollationKey("123").toByteArray();
        byte[] bytes3 = collator.getCollationKey("124").toByteArray();
        byte[] bytes4 = collator.getCollationKey("1245").toByteArray();
        byte[] bytes5 = collator.getCollationKey("1245").toByteArray();
        assertTrue("returned collation key does not sort correctly",
                compareUnsignedByteArrays(bytes1, bytes2) < 0);
        assertTrue("returned collation key does not sort correctly",
                compareUnsignedByteArrays(bytes2, bytes3) < 0);
        assertTrue("returned collation key does not sort correctly",
                compareUnsignedByteArrays(bytes3, bytes4) < 0);
        assertTrue("returned collation key does not sort correctly",
                compareUnsignedByteArrays(bytes4, bytes5) == 0);
    }
    private int compareUnsignedByteArrays(byte[] bytes1, byte[] bytes2) {
        int commonLength = Math.min(bytes1.length, bytes2.length);
        for (int i = 0; i < commonLength; i++) {
            int keyA = bytes1[i] & 0xFF;
            int keyB = bytes2[i] & 0xFF;
            if (keyA < keyB) {
                return -1;
            }
            if (keyA > keyB) {
                return 1;
            }
        }
        if (bytes1.length < bytes2.length) {
            return -1;
        } else if (bytes1.length > bytes2.length) {
            return 1;
        } else {
            return 0;
        }
    }
}

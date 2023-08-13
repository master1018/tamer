public class PackedStringTests extends TestCase {
    private static final char DELIMITER_ELEMENT = '\1';
    private static final char DELIMITER_TAG = '\2';
    private static final String PACKED_STRING_TAGGED = "val1" + DELIMITER_TAG + "tag1" +
            DELIMITER_ELEMENT + "val2" + DELIMITER_TAG + "tag2" +
            DELIMITER_ELEMENT + "val3" + DELIMITER_TAG + "tag3" +
            DELIMITER_ELEMENT + "val4" + DELIMITER_TAG + "tag4";
    public void testPackedString() {
        PackedString ps = new PackedString(PACKED_STRING_TAGGED);
        assertEquals("val1", ps.get("tag1"));
        assertEquals("val2", ps.get("tag2"));
        assertEquals("val3", ps.get("tag3"));
        assertEquals("val4", ps.get("tag4"));
        assertNull(ps.get("tag100"));
    }
    public void testPackedStringBuilderCreate() {
        PackedString.Builder b = new PackedString.Builder();
        b.put("tag1", "value1");
        b.put("tag2", "value2");
        b.put("tag3", "value3");
        b.put("tag4", "value4");
        String packedOut = b.toString();
        PackedString.Builder b2 = new PackedString.Builder(packedOut);
        assertEquals("value1", b2.get("tag1"));
        assertEquals("value2", b2.get("tag2"));
        assertEquals("value3", b2.get("tag3"));
        assertEquals("value4", b2.get("tag4"));
        assertNull(b2.get("tag100"));
    }
    public void testPackedStringBuilderEdit() {
        PackedString.Builder b = new PackedString.Builder(PACKED_STRING_TAGGED);
        assertEquals("val1", b.get("tag1"));
        assertEquals("val2", b.get("tag2"));
        assertEquals("val3", b.get("tag3"));
        assertEquals("val4", b.get("tag4"));
        assertNull(b.get("tag100"));
        b.put("tag2", "TWO");                   
        b.put("tag3", null);                    
        b.put("tag5", "value5");                
        assertEquals("val1", b.get("tag1"));
        assertEquals("TWO", b.get("tag2"));     
        assertEquals(null, b.get("tag3"));      
        assertEquals("val4", b.get("tag4"));
        assertEquals("value5", b.get("tag5"));  
        assertNull(b.get("tag100"));
        String packedOut = b.toString();
        PackedString.Builder b2 = new PackedString.Builder(packedOut);
        assertEquals("val1", b2.get("tag1"));
        assertEquals("TWO", b2.get("tag2"));
        assertEquals(null, b2.get("tag3"));
        assertEquals("val4", b2.get("tag4"));
        assertEquals("value5", b2.get("tag5"));
        assertNull(b2.get("tag100"));
    }
}

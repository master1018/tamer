public class NormalizerTest extends TestCase {
    public void test_form_values() throws Exception {
        Form[] forms = Form.values();
        assertEquals(4, forms.length);
        assertEquals(Form.NFD, forms[0]);
        assertEquals(Form.NFC, forms[1]);
        assertEquals(Form.NFKD, forms[2]);
        assertEquals(Form.NFKC, forms[3]);
    }
    public void test_form_valueOfLjava_lang_String() {
        try {
            Form.valueOf(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        assertEquals(Form.NFC, Form.valueOf("NFC"));
        assertEquals(Form.NFD, Form.valueOf("NFD"));
        assertEquals(Form.NFKC, Form.valueOf("NFKC"));
        assertEquals(Form.NFKD, Form.valueOf("NFKD"));
        try {
            Form.valueOf("not exist");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            Form.valueOf("nfc");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            Form.valueOf("NFC ");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
    public void test_isNormalized() throws Exception {
        String src = "\u00c1";
        assertTrue(Normalizer.isNormalized(src, Form.NFC));
        assertFalse(Normalizer.isNormalized(src, Form.NFD));
        assertTrue(Normalizer.isNormalized(src, Form.NFKC));
        assertFalse(Normalizer.isNormalized(src, Form.NFKD));
        src = "\u0041\u0301";
        assertFalse(Normalizer.isNormalized(src, Form.NFC));
        assertTrue(Normalizer.isNormalized(src, Form.NFD));
        assertFalse(Normalizer.isNormalized(src, Form.NFKC));
        assertTrue(Normalizer.isNormalized(src, Form.NFKD));
        src = "\ufb03";
        assertTrue(Normalizer.isNormalized(src, Form.NFC));
        assertTrue(Normalizer.isNormalized(src, Form.NFD));
        assertFalse(Normalizer.isNormalized(src, Form.NFKC));
        assertFalse(Normalizer.isNormalized(src, Form.NFKD));
        src = "\u0066\u0066\u0069";
        assertTrue(Normalizer.isNormalized(src, Form.NFC));
        assertTrue(Normalizer.isNormalized(src, Form.NFD));
        assertTrue(Normalizer.isNormalized(src, Form.NFKC));
        assertTrue(Normalizer.isNormalized(src, Form.NFKD));
        src = "";
        assertTrue(Normalizer.isNormalized(src, Form.NFC));
        assertTrue(Normalizer.isNormalized(src, Form.NFD));
        assertTrue(Normalizer.isNormalized(src, Form.NFKC));
        assertTrue(Normalizer.isNormalized(src, Form.NFKD));
    }
    public void test_isNormalized_exception() throws Exception {
        try {
            Normalizer.isNormalized(null, Form.NFC);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            Normalizer.isNormalized("chars", null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    public void test_normalize() throws Exception {
        String src = "\u00c1";
        assertEquals("\u00c1", Normalizer.normalize(src, Form.NFC));
        assertEquals("\u0041\u0301", Normalizer.normalize(src, Form.NFD));
        assertEquals("\u00c1", Normalizer.normalize(src, Form.NFKC));
        assertEquals("\u0041\u0301", Normalizer.normalize(src, Form.NFKD));
        src = "\u0041\u0301";
        assertEquals("\u00c1", Normalizer.normalize(src, Form.NFC));
        assertEquals("\u0041\u0301", Normalizer.normalize(src, Form.NFD));
        assertEquals("\u00c1", Normalizer.normalize(src, Form.NFKC));
        assertEquals("\u0041\u0301", Normalizer.normalize(src, Form.NFKD));
        src = "\ufb03";
        assertEquals("\ufb03", Normalizer.normalize(src, Form.NFC));
        assertEquals("\ufb03", Normalizer.normalize(src, Form.NFD));
        assertEquals("\u0066\u0066\u0069", Normalizer.normalize(src, Form.NFKC));
        assertEquals("\u0066\u0066\u0069", Normalizer.normalize(src, Form.NFKD));
        src = "\u0066\u0066\u0069";
        assertEquals("\u0066\u0066\u0069", Normalizer.normalize(src, Form.NFC));
        assertEquals("\u0066\u0066\u0069", Normalizer.normalize(src, Form.NFD));
        assertEquals("\u0066\u0066\u0069", Normalizer.normalize(src, Form.NFKC));
        assertEquals("\u0066\u0066\u0069", Normalizer.normalize(src, Form.NFKD));
        src = "";
        assertEquals("", Normalizer.normalize(src, Form.NFC));
        assertEquals("", Normalizer.normalize(src, Form.NFD));
        assertEquals("", Normalizer.normalize(src, Form.NFKC));
        assertEquals("", Normalizer.normalize(src, Form.NFKD));
    }
    public void test_normalize_exception() throws Exception {
        try {
            Normalizer.normalize(null, Form.NFC);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            Normalizer.normalize("chars", null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
}

public class JSONObjectTest extends TestCase {
    public void testEmptyObject() throws JSONException {
        JSONObject object = new JSONObject();
        assertEquals(0, object.length());
        assertNull(object.names());
        assertNull(object.toJSONArray(new JSONArray()));
        assertEquals("{}", object.toString());
        assertEquals("{}", object.toString(5));
        try {
            object.get("foo");
            fail();
        } catch (JSONException e) {
        }
        try {
            object.getBoolean("foo");
            fail();
        } catch (JSONException e) {
        }
        try {
            object.getDouble("foo");
            fail();
        } catch (JSONException e) {
        }
        try {
            object.getInt("foo");
            fail();
        } catch (JSONException e) {
        }
        try {
            object.getJSONArray("foo");
            fail();
        } catch (JSONException e) {
        }
        try {
            object.getJSONObject("foo");
            fail();
        } catch (JSONException e) {
        }
        try {
            object.getLong("foo");
            fail();
        } catch (JSONException e) {
        }
        try {
            object.getString("foo");
            fail();
        } catch (JSONException e) {
        }
        assertFalse(object.has("foo"));
        assertTrue(object.isNull("foo")); 
        assertNull(object.opt("foo"));
        assertEquals(false, object.optBoolean("foo"));
        assertEquals(true, object.optBoolean("foo", true));
        assertEquals(Double.NaN, object.optDouble("foo"));
        assertEquals(5.0, object.optDouble("foo", 5.0));
        assertEquals(0, object.optInt("foo"));
        assertEquals(5, object.optInt("foo", 5));
        assertEquals(null, object.optJSONArray("foo"));
        assertEquals(null, object.optJSONObject("foo"));
        assertEquals(0, object.optLong("foo"));
        assertEquals(Long.MAX_VALUE-1, object.optLong("foo", Long.MAX_VALUE-1));
        assertEquals("", object.optString("foo")); 
        assertEquals("bar", object.optString("foo", "bar"));
        assertNull(object.remove("foo"));
    }
    public void testEqualsAndHashCode() throws JSONException {
        JSONObject a = new JSONObject();
        JSONObject b = new JSONObject();
        assertFalse(a.equals(b));
        assertEquals(a.hashCode(), System.identityHashCode(a));
    }
    public void testGet() throws JSONException {
        JSONObject object = new JSONObject();
        Object value = new Object();
        object.put("foo", value);
        object.put("bar", new Object());
        object.put("baz", new Object());
        assertSame(value, object.get("foo"));
        try {
            object.get("FOO");
            fail();
        } catch (JSONException e) {
        }
        try {
            object.put(null, value);
            fail();
        } catch (JSONException e) {
        }
        try {
            object.get(null);
            fail();
        } catch (JSONException e) {
        }
    }
    public void testPut() throws JSONException {
        JSONObject object = new JSONObject();
        assertSame(object, object.put("foo", true));
        object.put("foo", false);
        assertEquals(false, object.get("foo"));
        object.put("foo", 5.0d);
        assertEquals(5.0d, object.get("foo"));
        object.put("foo", 0);
        assertEquals(0, object.get("foo"));
        object.put("bar", Long.MAX_VALUE - 1);
        assertEquals(Long.MAX_VALUE - 1, object.get("bar"));
        object.put("baz", "x");
        assertEquals("x", object.get("baz"));
        object.put("bar", JSONObject.NULL);
        assertSame(JSONObject.NULL, object.get("bar"));
    }
    public void testPutNullRemoves() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", "bar");
        object.put("foo", null);
        assertEquals(0, object.length());
        assertFalse(object.has("foo"));
        try {
            object.get("foo");
            fail();
        } catch (JSONException e) {
        }
    }
    public void testPutOpt() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", "bar");
        object.putOpt("foo", null);
        assertEquals("bar", object.get("foo"));
        object.putOpt(null, null);
        assertEquals(1, object.length());
        object.putOpt(null, "bar");
        assertEquals(1, object.length());
    }
    public void testPutOptUnsupportedNumbers() throws JSONException {
        JSONObject object = new JSONObject();
        try {
            object.putOpt("foo", Double.NaN);
            fail();
        } catch (JSONException e) {
        }
        try {
            object.putOpt("foo", Double.NEGATIVE_INFINITY);
            fail();
        } catch (JSONException e) {
        }
        try {
            object.putOpt("foo", Double.POSITIVE_INFINITY);
            fail();
        } catch (JSONException e) {
        }
    }
    public void testRemove() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", "bar");
        assertEquals(null, object.remove(null));
        assertEquals(null, object.remove(""));
        assertEquals(null, object.remove("bar"));
        assertEquals("bar", object.remove("foo"));
        assertEquals(null, object.remove("foo"));
    }
    public void testBooleans() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", true);
        object.put("bar", false);
        object.put("baz", "true");
        object.put("quux", "false");
        assertEquals(4, object.length());
        assertEquals(true, object.getBoolean("foo"));
        assertEquals(false, object.getBoolean("bar"));
        assertEquals(true, object.getBoolean("baz"));
        assertEquals(false, object.getBoolean("quux"));
        assertFalse(object.isNull("foo"));
        assertFalse(object.isNull("quux"));
        assertTrue(object.has("foo"));
        assertTrue(object.has("quux"));
        assertFalse(object.has("missing"));
        assertEquals(true, object.optBoolean("foo"));
        assertEquals(false, object.optBoolean("bar"));
        assertEquals(true, object.optBoolean("baz"));
        assertEquals(false, object.optBoolean("quux"));
        assertEquals(false, object.optBoolean("missing"));
        assertEquals(true, object.optBoolean("foo", true));
        assertEquals(false, object.optBoolean("bar", true));
        assertEquals(true, object.optBoolean("baz", true));
        assertEquals(false, object.optBoolean("quux", true));
        assertEquals(true, object.optBoolean("missing", true));
        object.put("foo", "truE");
        object.put("bar", "FALSE");
        assertEquals(true, object.getBoolean("foo"));
        assertEquals(false, object.getBoolean("bar"));
        assertEquals(true, object.optBoolean("foo"));
        assertEquals(false, object.optBoolean("bar"));
        assertEquals(true, object.optBoolean("foo", false));
        assertEquals(false, object.optBoolean("bar", false));
    }
    public void testNumbers() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", Double.MIN_VALUE);
        object.put("bar", 9223372036854775806L);
        object.put("baz", Double.MAX_VALUE);
        object.put("quux", -0d);
        assertEquals(4, object.length());
        String toString = object.toString();
        assertTrue(toString, toString.contains("\"foo\":4.9E-324"));
        assertTrue(toString, toString.contains("\"bar\":9223372036854775806"));
        assertTrue(toString, toString.contains("\"baz\":1.7976931348623157E308"));
        assertTrue(toString, toString.contains("\"quux\":-0}") 
                || toString.contains("\"quux\":-0,"));
        assertEquals(Double.MIN_VALUE, object.get("foo"));
        assertEquals(9223372036854775806L, object.get("bar"));
        assertEquals(Double.MAX_VALUE, object.get("baz"));
        assertEquals(-0d, object.get("quux"));
        assertEquals(Double.MIN_VALUE, object.getDouble("foo"));
        assertEquals(9.223372036854776E18, object.getDouble("bar"));
        assertEquals(Double.MAX_VALUE, object.getDouble("baz"));
        assertEquals(-0d, object.getDouble("quux"));
        assertEquals(0, object.getLong("foo"));
        assertEquals(9223372036854775806L, object.getLong("bar"));
        assertEquals(Long.MAX_VALUE, object.getLong("baz"));
        assertEquals(0, object.getLong("quux"));
        assertEquals(0, object.getInt("foo"));
        assertEquals(-2, object.getInt("bar"));
        assertEquals(Integer.MAX_VALUE, object.getInt("baz"));
        assertEquals(0, object.getInt("quux"));
        assertEquals(Double.MIN_VALUE, object.opt("foo"));
        assertEquals(9223372036854775806L, object.optLong("bar"));
        assertEquals(Double.MAX_VALUE, object.optDouble("baz"));
        assertEquals(0, object.optInt("quux"));
        assertEquals(Double.MIN_VALUE, object.opt("foo"));
        assertEquals(9223372036854775806L, object.optLong("bar"));
        assertEquals(Double.MAX_VALUE, object.optDouble("baz"));
        assertEquals(0, object.optInt("quux"));
        assertEquals(Double.MIN_VALUE, object.optDouble("foo", 5.0d));
        assertEquals(9223372036854775806L, object.optLong("bar", 1L));
        assertEquals(Long.MAX_VALUE, object.optLong("baz", 1L));
        assertEquals(0, object.optInt("quux", -1));
        assertEquals("4.9E-324", object.getString("foo"));
        assertEquals("9223372036854775806", object.getString("bar"));
        assertEquals("1.7976931348623157E308", object.getString("baz"));
        assertEquals("-0.0", object.getString("quux"));
    }
    public void testFloats() throws JSONException {
        JSONObject object = new JSONObject();
        try {
            object.put("foo", (Float) Float.NaN);
            fail();
        } catch (JSONException e) {
        }
        try {
            object.put("foo", (Float) Float.NEGATIVE_INFINITY);
            fail();
        } catch (JSONException e) {
        }
        try {
            object.put("foo", (Float) Float.POSITIVE_INFINITY);
            fail();
        } catch (JSONException e) {
        }
    }
    public void testOtherNumbers() throws JSONException {
        Number nan = new Number() {
            public int intValue() {
                throw new UnsupportedOperationException();
            }
            public long longValue() {
                throw new UnsupportedOperationException();
            }
            public float floatValue() {
                throw new UnsupportedOperationException();
            }
            public double doubleValue() {
                return Double.NaN;
            }
            @Override public String toString() {
                return "x";
            }
        };
        JSONObject object = new JSONObject();
        try {
            object.put("foo", nan);
            fail("Object.put() accepted a NaN (via a custom Number class)");
        } catch (JSONException e) {
        }
    }
    public void testForeignObjects() throws JSONException {
        Object foreign = new Object() {
            @Override public String toString() {
                return "x";
            }
        };
        JSONObject object = new JSONObject();
        object.put("foo", foreign);
        assertEquals("{\"foo\":\"x\"}", object.toString());
    }
    public void testNullKeys() {
        try {
            new JSONObject().put(null, false);
            fail();
        } catch (JSONException e) {
        }
        try {
            new JSONObject().put(null, 0.0d);
            fail();
        } catch (JSONException e) {
        }
        try {
            new JSONObject().put(null, 5);
            fail();
        } catch (JSONException e) {
        }
        try {
            new JSONObject().put(null, 5L);
            fail();
        } catch (JSONException e) {
        }
        try {
            new JSONObject().put(null, "foo");
            fail();
        } catch (JSONException e) {
        }
    }
    public void testStrings() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", "true");
        object.put("bar", "5.5");
        object.put("baz", "9223372036854775806");
        object.put("quux", "null");
        object.put("height", "5\"8' tall");
        assertTrue(object.toString().contains("\"foo\":\"true\""));
        assertTrue(object.toString().contains("\"bar\":\"5.5\""));
        assertTrue(object.toString().contains("\"baz\":\"9223372036854775806\""));
        assertTrue(object.toString().contains("\"quux\":\"null\""));
        assertTrue(object.toString().contains("\"height\":\"5\\\"8' tall\""));
        assertEquals("true", object.get("foo"));
        assertEquals("null", object.getString("quux"));
        assertEquals("5\"8' tall", object.getString("height"));
        assertEquals("true", object.opt("foo"));
        assertEquals("5.5", object.optString("bar"));
        assertEquals("true", object.optString("foo", "x"));
        assertFalse(object.isNull("foo"));
        assertEquals(true, object.getBoolean("foo"));
        assertEquals(true, object.optBoolean("foo"));
        assertEquals(true, object.optBoolean("foo", false));
        assertEquals(0, object.optInt("foo"));
        assertEquals(-2, object.optInt("foo", -2));
        assertEquals(5.5d, object.getDouble("bar"));
        assertEquals(5L, object.getLong("bar"));
        assertEquals(5, object.getInt("bar"));
        assertEquals(5, object.optInt("bar", 3));
        assertEquals(9223372036854775807L, object.getLong("baz"));
        assertEquals(9.223372036854776E18, object.getDouble("baz"));
        assertEquals(Integer.MAX_VALUE, object.getInt("baz"));
        assertFalse(object.isNull("quux"));
        try {
            object.getDouble("quux");
            fail();
        } catch (JSONException e) {
        }
        assertEquals(Double.NaN, object.optDouble("quux"));
        assertEquals(-1.0d, object.optDouble("quux", -1.0d));
        object.put("foo", "TRUE");
        assertEquals(true, object.getBoolean("foo"));
    }
    public void testJSONObjects() throws JSONException {
        JSONObject object = new JSONObject();
        JSONArray a = new JSONArray();
        JSONObject b = new JSONObject();
        object.put("foo", a);
        object.put("bar", b);
        assertSame(a, object.getJSONArray("foo"));
        assertSame(b, object.getJSONObject("bar"));
        try {
            object.getJSONObject("foo");
            fail();
        } catch (JSONException e) {
        }
        try {
            object.getJSONArray("bar");
            fail();
        } catch (JSONException e) {
        }
        assertEquals(a, object.optJSONArray("foo"));
        assertEquals(b, object.optJSONObject("bar"));
        assertEquals(null, object.optJSONArray("bar"));
        assertEquals(null, object.optJSONObject("foo"));
    }
    public void testNullCoercionToString() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", JSONObject.NULL);
        assertEquals("null", object.getString("foo"));
    }
    public void testArrayCoercion() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", "[true]");
        try {
            object.getJSONArray("foo");
            fail();
        } catch (JSONException e) {
        }
    }
    public void testObjectCoercion() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", "{}");
        try {
            object.getJSONObject("foo");
            fail();
        } catch (JSONException e) {
        }
    }
    public void testAccumulateValueChecking() throws JSONException {
        JSONObject object = new JSONObject();
        try {
            object.accumulate("foo", Double.NaN);
            fail();
        } catch (JSONException e) {
        }
        object.accumulate("foo", 1);
        try {
            object.accumulate("foo", Double.NaN);
            fail();
        } catch (JSONException e) {
        }
        object.accumulate("foo", 2);
        try {
            object.accumulate("foo", Double.NaN);
            fail();
        } catch (JSONException e) {
        }
    }
    public void testToJSONArray() throws JSONException {
        JSONObject object = new JSONObject();
        Object value = new Object();
        object.put("foo", true);
        object.put("bar", 5.0d);
        object.put("baz", -0.0d);
        object.put("quux", value);
        JSONArray names = new JSONArray();
        names.put("baz");
        names.put("quux");
        names.put("foo");
        JSONArray array = object.toJSONArray(names);
        assertEquals(-0.0d, array.get(0));
        assertEquals(value, array.get(1));
        assertEquals(true, array.get(2));
        object.put("foo", false);
        assertEquals(true, array.get(2));
    }
    public void testToJSONArrayMissingNames() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", true);
        object.put("bar", 5.0d);
        object.put("baz", JSONObject.NULL);
        JSONArray names = new JSONArray();
        names.put("bar");
        names.put("foo");
        names.put("quux");
        names.put("baz");
        JSONArray array = object.toJSONArray(names);
        assertEquals(4, array.length());
        assertEquals(5.0d, array.get(0));
        assertEquals(true, array.get(1));
        try {
            array.get(2);
            fail();
        } catch (JSONException e) {
        }
        assertEquals(JSONObject.NULL, array.get(3));
    }
    public void testToJSONArrayNull() throws JSONException {
        JSONObject object = new JSONObject();
        assertEquals(null, object.toJSONArray(null));
        object.put("foo", 5);
        try {
            object.toJSONArray(null);
        } catch (JSONException e) {
        }
    }
    public void testToJSONArrayEndsUpEmpty() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", 5);
        JSONArray array = new JSONArray();
        array.put("bar");
        assertEquals(1, object.toJSONArray(array).length());
    }
    public void testToJSONArrayNonString() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", 5);
        object.put("null", 10);
        object.put("false", 15);
        JSONArray names = new JSONArray();
        names.put(JSONObject.NULL);
        names.put(false);
        names.put("foo");
        JSONArray array = object.toJSONArray(names);
        assertEquals(3, array.length());
        assertEquals(10, array.get(0));
        assertEquals(15, array.get(1));
        assertEquals(5, array.get(2));
    }
    public void testPutUnsupportedNumbers() throws JSONException {
        JSONObject object = new JSONObject();
        try {
            object.put("foo", Double.NaN);
            fail();
        } catch (JSONException e) {
        }
        try {
            object.put("foo", Double.NEGATIVE_INFINITY);
            fail();
        } catch (JSONException e) {
        }
        try {
            object.put("foo", Double.POSITIVE_INFINITY);
            fail();
        } catch (JSONException e) {
        }
    }
    public void testPutUnsupportedNumbersAsObjects() throws JSONException {
        JSONObject object = new JSONObject();
        try {
            object.put("foo", (Double) Double.NaN);
            fail();
        } catch (JSONException e) {
        }
        try {
            object.put("foo", (Double) Double.NEGATIVE_INFINITY);
            fail();
        } catch (JSONException e) {
        }
        try {
            object.put("foo", (Double) Double.POSITIVE_INFINITY);
            fail();
        } catch (JSONException e) {
        }
    }
    public void testCreateWithUnsupportedNumbers() throws JSONException {
        Map<String, Object> contents = new HashMap<String, Object>();
        contents.put("foo", Double.NaN);
        contents.put("bar", Double.NEGATIVE_INFINITY);
        contents.put("baz", Double.POSITIVE_INFINITY);
        JSONObject object = new JSONObject(contents);
        assertEquals(Double.NaN, object.get("foo"));
        assertEquals(Double.NEGATIVE_INFINITY, object.get("bar"));
        assertEquals(Double.POSITIVE_INFINITY, object.get("baz"));
    }
    public void testToStringWithUnsupportedNumbers() {
        JSONObject object = new JSONObject(Collections.singletonMap("foo", Double.NaN));
        assertEquals(null, object.toString());
    }
    public void testMapConstructorCopiesContents() throws JSONException {
        Map<String, Object> contents = new HashMap<String, Object>();
        contents.put("foo", 5);
        JSONObject object = new JSONObject(contents);
        contents.put("foo", 10);
        assertEquals(5, object.get("foo"));
    }
    public void testMapConstructorWithBogusEntries() {
        Map<Object, Object> contents = new HashMap<Object, Object>();
        contents.put(5, 5);
        try {
            new JSONObject(contents);
            fail("JSONObject constructor doesn't validate its input!");
        } catch (Exception e) {
        }
    }
    public void testTokenerConstructor() throws JSONException {
        JSONObject object = new JSONObject(new JSONTokener("{\"foo\": false}"));
        assertEquals(1, object.length());
        assertEquals(false, object.get("foo"));
    }
    public void testTokenerConstructorWrongType() throws JSONException {
        try {
            new JSONObject(new JSONTokener("[\"foo\", false]"));
            fail();
        } catch (JSONException e) {
        }
    }
    public void testTokenerConstructorNull() throws JSONException {
        try {
            new JSONObject((JSONTokener) null);
            fail();
        } catch (NullPointerException e) {
        }
    }
    public void testTokenerConstructorParseFail() {
        try {
            new JSONObject(new JSONTokener("{"));
            fail();
        } catch (JSONException e) {
        }
    }
    public void testStringConstructor() throws JSONException {
        JSONObject object = new JSONObject("{\"foo\": false}");
        assertEquals(1, object.length());
        assertEquals(false, object.get("foo"));
    }
    public void testStringConstructorWrongType() throws JSONException {
        try {
            new JSONObject("[\"foo\", false]");
            fail();
        } catch (JSONException e) {
        }
    }
    public void testStringConstructorNull() throws JSONException {
        try {
            new JSONObject((String) null);
            fail();
        } catch (NullPointerException e) {
        }
    }
    public void testStringonstructorParseFail() {
        try {
            new JSONObject("{");
            fail();
        } catch (JSONException e) {
        }
    }
    public void testCopyConstructor() throws JSONException {
        JSONObject source = new JSONObject();
        source.put("a", JSONObject.NULL);
        source.put("b", false);
        source.put("c", 5);
        JSONObject copy = new JSONObject(source, new String[] { "a", "c" });
        assertEquals(2, copy.length());
        assertEquals(JSONObject.NULL, copy.get("a"));
        assertEquals(5, copy.get("c"));
        assertEquals(null, copy.opt("b"));
    }
    public void testCopyConstructorMissingName() throws JSONException {
        JSONObject source = new JSONObject();
        source.put("a", JSONObject.NULL);
        source.put("b", false);
        source.put("c", 5);
        JSONObject copy = new JSONObject(source, new String[]{ "a", "c", "d" });
        assertEquals(2, copy.length());
        assertEquals(JSONObject.NULL, copy.get("a"));
        assertEquals(5, copy.get("c"));
        assertEquals(0, copy.optInt("b"));
    }
    public void testAccumulateMutatesInPlace() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", 5);
        object.accumulate("foo", 6);
        JSONArray array = object.getJSONArray("foo");
        assertEquals("[5,6]", array.toString());
        object.accumulate("foo", 7);
        assertEquals("[5,6,7]", array.toString());
    }
    public void testAccumulateExistingArray() throws JSONException {
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("foo", array);
        object.accumulate("foo", 5);
        assertEquals("[5]", array.toString());
    }
    public void testAccumulatePutArray() throws JSONException {
        JSONObject object = new JSONObject();
        object.accumulate("foo", 5);
        assertEquals("{\"foo\":5}", object.toString());
        object.accumulate("foo", new JSONArray());
        assertEquals("{\"foo\":[5,[]]}", object.toString());
    }
    public void testAccumulateNull() {
        JSONObject object = new JSONObject();
        try {
            object.accumulate(null, 5);
            fail();
        } catch (JSONException e) {
        }
    }
    public void testEmptyStringKey() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("", 5);
        assertEquals(5, object.get(""));
        assertEquals("{\"\":5}", object.toString());
    }
    public void testNullValue() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", JSONObject.NULL);
        object.put("bar", null);
        assertTrue(object.has("foo"));
        assertFalse(object.has("bar"));
        assertTrue(object.isNull("foo"));
        assertTrue(object.isNull("bar"));
    }
    public void testHas() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", 5);
        assertTrue(object.has("foo"));
        assertFalse(object.has("bar"));
        assertFalse(object.has(null));
    }
    public void testOptNull() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", "bar");
        assertEquals(null, object.opt(null));
        assertEquals(false, object.optBoolean(null));
        assertEquals(Double.NaN, object.optDouble(null));
        assertEquals(0, object.optInt(null));
        assertEquals(0L, object.optLong(null));
        assertEquals(null, object.optJSONArray(null));
        assertEquals(null, object.optJSONObject(null));
        assertEquals("", object.optString(null));
        assertEquals(true, object.optBoolean(null, true));
        assertEquals(0.0d, object.optDouble(null, 0.0d));
        assertEquals(1, object.optInt(null, 1));
        assertEquals(1L, object.optLong(null, 1L));
        assertEquals("baz", object.optString(null, "baz"));
    }
    public void testToStringWithIndentFactor() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", new JSONArray(Arrays.asList(5, 6)));
        object.put("bar", new JSONObject());
        String foobar = "{\n" +
                "     \"foo\": [\n" +
                "          5,\n" +
                "          6\n" +
                "     ],\n" +
                "     \"bar\": {}\n" +
                "}";
        String barfoo = "{\n" +
                "     \"bar\": {},\n" +
                "     \"foo\": [\n" +
                "          5,\n" +
                "          6\n" +
                "     ]\n" +
                "}";
        String string = object.toString(5);
        assertTrue(string, foobar.equals(string) || barfoo.equals(string));
    }
    public void testNames() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", 5);
        object.put("bar", 6);
        object.put("baz", 7);
        JSONArray array = object.names();
        assertTrue(array.toString().contains("foo"));
        assertTrue(array.toString().contains("bar"));
        assertTrue(array.toString().contains("baz"));
    }
    public void testKeysEmptyObject() {
        JSONObject object = new JSONObject();
        assertFalse(object.keys().hasNext());
        try {
            object.keys().next();
            fail();
        } catch (NoSuchElementException e) {
        }
    }
    public void testKeys() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", 5);
        object.put("bar", 6);
        object.put("foo", 7);
        @SuppressWarnings("unchecked")
        Iterator<String> keys = (Iterator<String>) object.keys();
        Set<String> result = new HashSet<String>();
        assertTrue(keys.hasNext());
        result.add(keys.next());
        assertTrue(keys.hasNext());
        result.add(keys.next());
        assertFalse(keys.hasNext());
        assertEquals(new HashSet<String>(Arrays.asList("foo", "bar")), result);
        try {
            keys.next();
            fail();
        } catch (NoSuchElementException e) {
        }
    }
    public void testMutatingKeysMutatesObject() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("foo", 5);
        Iterator keys = object.keys();
        keys.next();
        keys.remove();
        assertEquals(0, object.length());
    }
    public void testQuote() {
    }
    public void testQuoteNull() throws JSONException {
        assertEquals("\"\"", JSONObject.quote(null));
    }
    public void testNumberToString() throws JSONException {
        assertEquals("5", JSONObject.numberToString(5));
        assertEquals("-0", JSONObject.numberToString(-0.0d));
        assertEquals("9223372036854775806", JSONObject.numberToString(9223372036854775806L));
        assertEquals("4.9E-324", JSONObject.numberToString(Double.MIN_VALUE));
        assertEquals("1.7976931348623157E308", JSONObject.numberToString(Double.MAX_VALUE));
        try {
            JSONObject.numberToString(Double.NaN);
            fail();
        } catch (JSONException e) {
        }
        try {
            JSONObject.numberToString(Double.NEGATIVE_INFINITY);
            fail();
        } catch (JSONException e) {
        }
        try {
            JSONObject.numberToString(Double.POSITIVE_INFINITY);
            fail();
        } catch (JSONException e) {
        }
        assertEquals("0.001", JSONObject.numberToString(new BigDecimal("0.001")));
        assertEquals("9223372036854775806",
                JSONObject.numberToString(new BigInteger("9223372036854775806")));
        try {
            JSONObject.numberToString(null);
            fail();
        } catch (JSONException e) {
        }
    }
}

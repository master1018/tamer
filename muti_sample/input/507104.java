@TestTargetClass(Arrays.class)
public class ArraysTest extends junit.framework.TestCase {
    public static class ReversedIntegerComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return -(((Integer) o1).compareTo((Integer) o2));
        }
        public boolean equals(Object o1, Object o2) {
            return ((Integer) o1).compareTo((Integer) o2) == 0;
        }
    }
    final static int arraySize = 100;
    Object[] objArray;
    boolean[] booleanArray;
    byte[] byteArray;
    char[] charArray;
    double[] doubleArray;
    float[] floatArray;
    int[] intArray;
    long[] longArray;
    Object[] objectArray;
    short[] shortArray;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "asList",
        args = {java.lang.Object[].class}
    )
    public void test_asList$Ljava_lang_Object() {
        List convertedList = Arrays.asList(objectArray);
        for (int counter = 0; counter < arraySize; counter++) {
            assertTrue(
                    "Array and List converted from array do not contain identical elements",
                    convertedList.get(counter) == objectArray[counter]);
        }
        convertedList.set(50, new Integer(1000));
        assertTrue("set/get did not work on coverted list", convertedList.get(
                50).equals(new Integer(1000)));
        convertedList.set(50, new Integer(50));
        new Support_UnmodifiableCollectionTest("", convertedList).runTest();
        Object[] myArray = (Object[]) (objectArray.clone());
        myArray[30] = null;
        myArray[60] = null;
        convertedList = Arrays.asList(myArray);
        for (int counter = 0; counter < arraySize; counter++) {
            assertTrue(
                    "Array and List converted from array do not contain identical elements",
                    convertedList.get(counter) == myArray[counter]);
        }
        try {
            Arrays.asList((Object[])null);
            fail("asList with null arg didn't throw NPE");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "binarySearch",
        args = {byte[].class, byte.class}
    )
    public void test_binarySearch$BB() {
        for (byte counter = 0; counter < arraySize; counter++)
            assertTrue("Binary search on byte[] answered incorrect position",
                    Arrays.binarySearch(byteArray, counter) == counter);
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(intArray, (byte) -1));
        assertTrue(
                "Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(intArray, (byte) arraySize) == -(arraySize + 1));
        for (byte counter = 0; counter < arraySize; counter++)
            byteArray[counter] -= 50;
        for (byte counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on byte[] involving negative numbers answered incorrect position",
                    Arrays.binarySearch(byteArray, (byte) (counter - 50)) == counter);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "binarySearch",
        args = {char[].class, char.class}
    )
    public void test_binarySearch$CC() {
        for (char counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on char[] answered incorrect position",
                    Arrays.binarySearch(charArray, (char) (counter + 1)) == counter);
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(charArray, '\u0000'));
        assertTrue(
                "Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(charArray, (char) (arraySize + 1)) == -(arraySize + 1));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "binarySearch",
        args = {double[].class, double.class}
    )
    public void test_binarySearch$DD() {
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on double[] answered incorrect position",
                    Arrays.binarySearch(doubleArray, (double) counter) == (double) counter);
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(doubleArray, (double) -1));
        assertTrue(
                "Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(doubleArray, (double) arraySize) == -(arraySize + 1));
        for (int counter = 0; counter < arraySize; counter++)
            doubleArray[counter] -= (double) 50;
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on double[] involving negative numbers answered incorrect position",
                    Arrays.binarySearch(doubleArray, (double) (counter - 50)) == (double) counter);
        double[] specials = new double[] { Double.NEGATIVE_INFINITY,
                -Double.MAX_VALUE, -2d, -Double.MIN_VALUE, -0d, 0d,
                Double.MIN_VALUE, 2d, Double.MAX_VALUE,
                Double.POSITIVE_INFINITY, Double.NaN };
        for (int i = 0; i < specials.length; i++) {
            int result = Arrays.binarySearch(specials, specials[i]);
            assertTrue(specials[i] + " invalid: " + result, result == i);
        }
        assertEquals("-1d", -4, Arrays.binarySearch(specials, -1d));
        assertEquals("1d", -8, Arrays.binarySearch(specials, 1d));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "binarySearch",
        args = {float[].class, float.class}
    )
    public void test_binarySearch$FF() {
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on float[] answered incorrect position",
                    Arrays.binarySearch(floatArray, (float) counter) == (float) counter);
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(floatArray, (float) -1));
        assertTrue(
                "Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(floatArray, (float) arraySize) == -(arraySize + 1));
        for (int counter = 0; counter < arraySize; counter++)
            floatArray[counter] -= (float) 50;
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on float[] involving negative numbers answered incorrect position",
                    Arrays.binarySearch(floatArray, (float) counter - 50) == (float) counter);
        float[] specials = new float[] { Float.NEGATIVE_INFINITY,
                -Float.MAX_VALUE, -2f, -Float.MIN_VALUE, -0f, 0f,
                Float.MIN_VALUE, 2f, Float.MAX_VALUE, Float.POSITIVE_INFINITY,
                Float.NaN };
        for (int i = 0; i < specials.length; i++) {
            int result = Arrays.binarySearch(specials, specials[i]);
            assertTrue(specials[i] + " invalid: " + result, result == i);
        }
        assertEquals("-1f", -4, Arrays.binarySearch(specials, -1f));
        assertEquals("1f", -8, Arrays.binarySearch(specials, 1f));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "binarySearch",
        args = {int[].class, int.class}
    )
    public void test_binarySearch$II() {
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue("Binary search on int[] answered incorrect position",
                    Arrays.binarySearch(intArray, counter) == counter);
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(intArray, -1));
        assertTrue("Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(intArray, arraySize) == -(arraySize + 1));
        for (int counter = 0; counter < arraySize; counter++)
            intArray[counter] -= 50;
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on int[] involving negative numbers answered incorrect position",
                    Arrays.binarySearch(intArray, counter - 50) == counter);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "binarySearch",
        args = {long[].class, long.class}
    )
    public void test_binarySearch$JJ() {
        for (long counter = 0; counter < arraySize; counter++)
            assertTrue("Binary search on long[] answered incorrect position",
                    Arrays.binarySearch(longArray, counter) == counter);
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(longArray, (long) -1));
        assertTrue(
                "Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(longArray, (long) arraySize) == -(arraySize + 1));
        for (long counter = 0; counter < arraySize; counter++)
            longArray[(int) counter] -= (long) 50;
        for (long counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on long[] involving negative numbers answered incorrect position",
                    Arrays.binarySearch(longArray, counter - (long) 50) == counter);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "binarySearch",
        args = {java.lang.Object[].class, java.lang.Object.class}
    )
    public void test_binarySearch$Ljava_lang_ObjectLjava_lang_Object() {
        assertEquals(
                "Binary search succeeded for non-comparable value in empty array",
                -1, Arrays.binarySearch(new Object[] {}, new Object()));
        assertEquals(
                "Binary search succeeded for comparable value in empty array",
                -1, Arrays.binarySearch(new Object[] {}, new Integer(-1)));
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on Object[] answered incorrect position",
                    Arrays.binarySearch(objectArray, objArray[counter]) == counter);
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(objectArray, new Integer(-1)));
        assertTrue(
                "Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(objectArray, new Integer(arraySize)) == -(arraySize + 1));
        String[] sArray = new String[]{"1", "2", "3", "4", ""};
        Object[] oArray = sArray;
        try {
            Arrays.binarySearch(oArray, new Integer(10));
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify ClassCastException.",
        method = "binarySearch",
        args = {java.lang.Object[].class, java.lang.Object.class, java.util.Comparator.class}
    )
    public void test_binarySearch$Ljava_lang_ObjectLjava_lang_ObjectLjava_util_Comparator() {
        Comparator comp = new ReversedIntegerComparator();
        for (int counter = 0; counter < arraySize; counter++)
            objectArray[counter] = objArray[arraySize - counter - 1];
        assertTrue(
                "Binary search succeeded for value not present in array 1",
                Arrays.binarySearch(objectArray, new Integer(-1), comp) == -(arraySize + 1));
        assertEquals("Binary search succeeded for value not present in array 2",
                -1, Arrays.binarySearch(objectArray, new Integer(arraySize), comp));
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on Object[] with custom comparator answered incorrect position",
                    Arrays.binarySearch(objectArray, objArray[counter], comp) == arraySize
                            - counter - 1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "binarySearch",
        args = {short[].class, short.class}
    )
    public void test_binarySearch$SS() {
        for (short counter = 0; counter < arraySize; counter++)
            assertTrue("Binary search on short[] answered incorrect position",
                    Arrays.binarySearch(shortArray, counter) == counter);
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(intArray, (short) -1));
        assertTrue(
                "Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(intArray, (short) arraySize) == -(arraySize + 1));
        for (short counter = 0; counter < arraySize; counter++)
            shortArray[counter] -= 50;
        for (short counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on short[] involving negative numbers answered incorrect position",
                    Arrays.binarySearch(shortArray, (short) (counter - 50)) == counter);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fill",
        args = {byte[].class, byte.class}
    )
    public void test_fill$BB() {
        byte d[] = new byte[1000];
        Arrays.fill(d, Byte.MAX_VALUE);
        for (int i = 0; i < d.length; i++)
            assertTrue("Failed to fill byte array correctly",
                    d[i] == Byte.MAX_VALUE);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fill",
        args = {byte[].class, int.class, int.class, byte.class}
    )
    public void test_fill$BIIB() {
        byte val = Byte.MAX_VALUE;
        byte d[] = new byte[1000];
        Arrays.fill(d, 400, d.length, val);
        for (int i = 0; i < 400; i++)
            assertTrue("Filled elements not in range", !(d[i] == val));
        for (int i = 400; i < d.length; i++)
            assertTrue("Failed to fill byte array correctly", d[i] == val);
        int result;
        try {
            Arrays.fill(new byte[2], 2, 1, (byte) 27);
            result = 0;
        } catch (ArrayIndexOutOfBoundsException e) {
            result = 1;
        } catch (IllegalArgumentException e) {
            result = 2;
        }
        assertEquals("Wrong exception1", 2, result);
        try {
            Arrays.fill(new byte[2], -1, 1, (byte) 27);
            result = 0;
        } catch (ArrayIndexOutOfBoundsException e) {
            result = 1;
        } catch (IllegalArgumentException e) {
            result = 2;
        }
        assertEquals("Wrong exception2", 1, result);
        try {
            Arrays.fill(new byte[2], 1, 4, (byte) 27);
            result = 0;
        } catch (ArrayIndexOutOfBoundsException e) {
            result = 1;
        } catch (IllegalArgumentException e) {
            result = 2;
        }
        assertEquals("Wrong exception", 1, result);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fill",
        args = {short[].class, short.class}
    )
    public void test_fill$SS() {
        short d[] = new short[1000];
        Arrays.fill(d, Short.MAX_VALUE);
        for (int i = 0; i < d.length; i++)
            assertTrue("Failed to fill short array correctly",
                    d[i] == Short.MAX_VALUE);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fill",
        args = {short[].class, int.class, int.class, short.class}
    )
    public void test_fill$SIIS() {
        short val = Short.MAX_VALUE;
        short d[] = new short[1000];
        Arrays.fill(d, 400, d.length, val);
        for (int i = 0; i < 400; i++)
            assertTrue("Filled elements not in range", !(d[i] == val));
        for (int i = 400; i < d.length; i++)
            assertTrue("Failed to fill short array correctly", d[i] == val);
        try {
            Arrays.fill(d, 10, 0, val);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            Arrays.fill(d, -10, 0, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            Arrays.fill(d, 10, d.length+1, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fill",
        args = {char[].class, char.class}
    )
    public void test_fill$CC() {
        char d[] = new char[1000];
        Arrays.fill(d, 'V');
        for (int i = 0; i < d.length; i++)
            assertEquals("Failed to fill char array correctly", 'V', d[i]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fill",
        args = {char[].class, int.class, int.class, char.class}
    )
    public void test_fill$CIIC() {
        char val = 'T';
        char d[] = new char[1000];
        Arrays.fill(d, 400, d.length, val);
        for (int i = 0; i < 400; i++)
            assertTrue("Filled elements not in range", !(d[i] == val));
        for (int i = 400; i < d.length; i++)
            assertTrue("Failed to fill char array correctly", d[i] == val);
        try {
            Arrays.fill(d, 10, 0, val);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            Arrays.fill(d, -10, 0, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            Arrays.fill(d, 10, d.length+1, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fill",
        args = {int[].class, int.class}
    )
    public void test_fill$II() {
        int d[] = new int[1000];
        Arrays.fill(d, Integer.MAX_VALUE);
        for (int i = 0; i < d.length; i++)
            assertTrue("Failed to fill int array correctly",
                    d[i] == Integer.MAX_VALUE);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fill",
        args = {int[].class, int.class, int.class, int.class}
    )
    public void test_fill$IIII() {
        int val = Integer.MAX_VALUE;
        int d[] = new int[1000];
        Arrays.fill(d, 400, d.length, val);
        for (int i = 0; i < 400; i++)
            assertTrue("Filled elements not in range", !(d[i] == val));
        for (int i = 400; i < d.length; i++)
            assertTrue("Failed to fill int array correctly", d[i] == val);
        try {
            Arrays.fill(d, 10, 0, val);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            Arrays.fill(d, -10, 0, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            Arrays.fill(d, 10, d.length+1, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fill",
        args = {long[].class, long.class}
    )
    public void test_fill$JJ() {
        long d[] = new long[1000];
        Arrays.fill(d, Long.MAX_VALUE);
        for (int i = 0; i < d.length; i++)
            assertTrue("Failed to fill long array correctly",
                    d[i] == Long.MAX_VALUE);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fill",
        args = {long[].class, int.class, int.class, long.class}
    )
    public void test_fill$JIIJ() {
        long d[] = new long[1000];
        Arrays.fill(d, 400, d.length, Long.MAX_VALUE);
        for (int i = 0; i < 400; i++)
            assertTrue("Filled elements not in range", !(d[i] == Long.MAX_VALUE));
        for (int i = 400; i < d.length; i++)
            assertTrue("Failed to fill long array correctly",
                    d[i] == Long.MAX_VALUE);
        try {
            Arrays.fill(d, 10, 0, Long.MIN_VALUE);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            Arrays.fill(d, -10, 0, Long.MAX_VALUE);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            Arrays.fill(d, 10, d.length+1, Long.MAX_VALUE);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fill",
        args = {float[].class, float.class}
    )
    public void test_fill$FF() {
        float d[] = new float[1000];
        Arrays.fill(d, Float.MAX_VALUE);
        for (int i = 0; i < d.length; i++)
            assertTrue("Failed to fill float array correctly",
                    d[i] == Float.MAX_VALUE);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fill",
        args = {float[].class, int.class, int.class, float.class}
    )
    public void test_fill$FIIF() {
        float val = Float.MAX_VALUE;
        float d[] = new float[1000];
        Arrays.fill(d, 400, d.length, val);
        for (int i = 0; i < 400; i++)
            assertTrue("Filled elements not in range", !(d[i] == val));
        for (int i = 400; i < d.length; i++)
            assertTrue("Failed to fill float array correctly", d[i] == val);
        try {
            Arrays.fill(d, 10, 0, val);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            Arrays.fill(d, -10, 0, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            Arrays.fill(d, 10, d.length+1, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fill",
        args = {double[].class, double.class}
    )
    public void test_fill$DD() {
        double d[] = new double[1000];
        Arrays.fill(d, Double.MAX_VALUE);
        for (int i = 0; i < d.length; i++)
            assertTrue("Failed to fill double array correctly",
                    d[i] == Double.MAX_VALUE);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fill",
        args = {double[].class, int.class, int.class, double.class}
    )
    public void test_fill$DIID() {
        double val = Double.MAX_VALUE;
        double d[] = new double[1000];
        Arrays.fill(d, 400, d.length, val);
        for (int i = 0; i < 400; i++)
            assertTrue("Filled elements not in range", !(d[i] == val));
        for (int i = 400; i < d.length; i++)
            assertTrue("Failed to fill double array correctly", d[i] == val);
        try {
            Arrays.fill(d, 10, 0, val);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            Arrays.fill(d, -10, 0, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            Arrays.fill(d, 10, d.length+1, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fill",
        args = {boolean[].class, boolean.class}
    )
    public void test_fill$ZZ() {
        boolean d[] = new boolean[1000];
        Arrays.fill(d, true);
        for (int i = 0; i < d.length; i++)
            assertTrue("Failed to fill boolean array correctly", d[i]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fill",
        args = {boolean[].class, int.class, int.class, boolean.class}
    )
    public void test_fill$ZIIZ() {
        boolean val = true;
        boolean d[] = new boolean[1000];
        Arrays.fill(d, 400, d.length, val);
        for (int i = 0; i < 400; i++)
            assertTrue("Filled elements not in range", !(d[i] == val));
        for (int i = 400; i < d.length; i++)
            assertTrue("Failed to fill boolean array correctly", d[i] == val);
        try {
            Arrays.fill(d, 10, 0, val);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            Arrays.fill(d, -10, 0, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            Arrays.fill(d, 10, d.length+1, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fill",
        args = {java.lang.Object[].class, java.lang.Object.class}
    )
    public void test_fill$Ljava_lang_ObjectLjava_lang_Object() {
        Object val = new Object();
        Object d[] = new Object[1000];
        Arrays.fill(d, 0, d.length, val);
        for (int i = 0; i < d.length; i++)
            assertTrue("Failed to fill Object array correctly", d[i] == val);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fill",
        args = {java.lang.Object[].class, int.class, int.class, java.lang.Object.class}
    )
    public void test_fill$Ljava_lang_ObjectIILjava_lang_Object() {
        Object val = new Object();
        Object d[] = new Object[1000];
        Arrays.fill(d, 400, d.length, val);
        for (int i = 0; i < 400; i++)
            assertTrue("Filled elements not in range", !(d[i] == val));
        for (int i = 400; i < d.length; i++)
            assertTrue("Failed to fill Object array correctly", d[i] == val);
        Arrays.fill(d, 400, d.length, null);
        for (int i = 400; i < d.length; i++)
            assertNull("Failed to fill Object array correctly with nulls",
                    d[i]);
        try {
            Arrays.fill(d, 10, 0, val);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            Arrays.fill(d, -10, 0, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            Arrays.fill(d, 10, d.length+1, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {byte[].class, byte[].class}
    )
    public void test_equals$B$B() {
        byte d[] = new byte[1000];
        byte x[] = new byte[1000];
        Arrays.fill(d, Byte.MAX_VALUE);
        Arrays.fill(x, Byte.MIN_VALUE);
        assertTrue("Inequal arrays returned true", !Arrays.equals(d, x));
        Arrays.fill(x, Byte.MAX_VALUE);
        assertTrue("equal arrays returned false", Arrays.equals(d, x));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {short[].class, short[].class}
    )
    public void test_equals$S$S() {
        short d[] = new short[1000];
        short x[] = new short[1000];
        Arrays.fill(d, Short.MAX_VALUE);
        Arrays.fill(x, Short.MIN_VALUE);
        assertTrue("Inequal arrays returned true", !Arrays.equals(d, x));
        Arrays.fill(x, Short.MAX_VALUE);
        assertTrue("equal arrays returned false", Arrays.equals(d, x));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {char[].class, char[].class}
    )
    public void test_equals$C$C() {
        char d[] = new char[1000];
        char x[] = new char[1000];
        char c = 'T';
        Arrays.fill(d, c);
        Arrays.fill(x, 'L');
        assertTrue("Inequal arrays returned true", !Arrays.equals(d, x));
        Arrays.fill(x, c);
        assertTrue("equal arrays returned false", Arrays.equals(d, x));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {int[].class, int[].class}
    )
    public void test_equals$I$I() {
        int d[] = new int[1000];
        int x[] = new int[1000];
        Arrays.fill(d, Integer.MAX_VALUE);
        Arrays.fill(x, Integer.MIN_VALUE);
        assertTrue("Inequal arrays returned true", !Arrays.equals(d, x));
        Arrays.fill(x, Integer.MAX_VALUE);
        assertTrue("equal arrays returned false", Arrays.equals(d, x));
        assertTrue("wrong result for null array1", !Arrays.equals(new int[2],
                null));
        assertTrue("wrong result for null array2", !Arrays.equals(null,
                new int[2]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {long[].class, long[].class}
    )
    public void test_equals$J$J() {
        long d[] = new long[1000];
        long x[] = new long[1000];
        Arrays.fill(d, Long.MAX_VALUE);
        Arrays.fill(x, Long.MIN_VALUE);
        assertTrue("Inequal arrays returned true", !Arrays.equals(d, x));
        Arrays.fill(x, Long.MAX_VALUE);
        assertTrue("equal arrays returned false", Arrays.equals(d, x));
        assertTrue("should be false", !Arrays.equals(
                new long[] { 0x100000000L }, new long[] { 0x200000000L }));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {float[].class, float[].class}
    )
    public void test_equals$F$F() {
        float d[] = new float[1000];
        float x[] = new float[1000];
        Arrays.fill(d, Float.MAX_VALUE);
        Arrays.fill(x, Float.MIN_VALUE);
        assertTrue("Inequal arrays returned true", !Arrays.equals(d, x));
        Arrays.fill(x, Float.MAX_VALUE);
        assertTrue("equal arrays returned false", Arrays.equals(d, x));
        assertTrue("NaN not equals", Arrays.equals(new float[] { Float.NaN },
                new float[] { Float.NaN }));
        assertTrue("0f equals -0f", !Arrays.equals(new float[] { 0f },
                new float[] { -0f }));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {double[].class, double[].class}
    )
    public void test_equals$D$D() {
        double d[] = new double[1000];
        double x[] = new double[1000];
        Arrays.fill(d, Double.MAX_VALUE);
        Arrays.fill(x, Double.MIN_VALUE);
        assertTrue("Inequal arrays returned true", !Arrays.equals(d, x));
        Arrays.fill(x, Double.MAX_VALUE);
        assertTrue("equal arrays returned false", Arrays.equals(d, x));
        assertTrue("should be false", !Arrays.equals(new double[] { 1.0 },
                new double[] { 2.0 }));
        assertTrue("NaN not equals", Arrays.equals(new double[] { Double.NaN },
                new double[] { Double.NaN }));
        assertTrue("0d equals -0d", !Arrays.equals(new double[] { 0d },
                new double[] { -0d }));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {boolean[].class, boolean[].class}
    )
    public void test_equals$Z$Z() {
        boolean d[] = new boolean[1000];
        boolean x[] = new boolean[1000];
        Arrays.fill(d, true);
        Arrays.fill(x, false);
        assertTrue("Inequal arrays returned true", !Arrays.equals(d, x));
        Arrays.fill(x, true);
        assertTrue("equal arrays returned false", Arrays.equals(d, x));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object[].class, java.lang.Object[].class}
    )
    public void test_equals$Ljava_lang_Object$Ljava_lang_Object() {
        Object d[] = new Object[1000];
        Object x[] = new Object[1000];
        Object o = new Object();
        Arrays.fill(d, o);
        Arrays.fill(x, new Object());
        assertTrue("Inequal arrays returned true", !Arrays.equals(d, x));
        Arrays.fill(x, o);
        d[50] = null;
        x[50] = null;
        assertTrue("equal arrays returned false", Arrays.equals(d, x));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sort",
        args = {byte[].class}
    )
    public void test_sort$B() {
        byte[] reversedArray = new byte[arraySize];
        for (int counter = 0; counter < arraySize; counter++)
            reversedArray[counter] = (byte) (arraySize - counter - 1);
        Arrays.sort(reversedArray);
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue("Resulting array not sorted",
                    reversedArray[counter] == (byte) counter);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sort",
        args = {byte[].class, int.class, int.class}
    )
    public void test_sort$BII() {
        int startIndex = arraySize / 4;
        int endIndex = 3 * arraySize / 4;
        byte[] reversedArray = new byte[arraySize];
        byte[] originalReversedArray = new byte[arraySize];
        for (int counter = 0; counter < arraySize; counter++) {
            reversedArray[counter] = (byte) (arraySize - counter - 1);
            originalReversedArray[counter] = reversedArray[counter];
        }
        Arrays.sort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                    reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        try {
            Arrays.sort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }
        try {
            Arrays.sort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        try {
            Arrays.sort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        try {
            Arrays.sort(new byte[1], startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sort",
        args = {char[].class}
    )
    public void test_sort$C() {
        char[] reversedArray = new char[arraySize];
        for (int counter = 0; counter < arraySize; counter++)
            reversedArray[counter] = (char) (arraySize - counter - 1);
        Arrays.sort(reversedArray);
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue("Resulting array not sorted",
                    reversedArray[counter] == (char) counter);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sort",
        args = {char[].class, int.class, int.class}
    )
    public void test_sort$CII() {
        int startIndex = arraySize / 4;
        int endIndex = 3 * arraySize / 4;
        char[] reversedArray = new char[arraySize];
        char[] originalReversedArray = new char[arraySize];
        for (int counter = 0; counter < arraySize; counter++) {
            reversedArray[counter] = (char) (arraySize - counter - 1);
            originalReversedArray[counter] = reversedArray[counter];
        }
        Arrays.sort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                    reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        try {
            Arrays.sort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }
        try {
            Arrays.sort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        try {
            Arrays.sort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        try {
            Arrays.sort(new char[1], startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sort",
        args = {double[].class}
    )
    public void test_sort$D() {
        double[] reversedArray = new double[arraySize];
        for (int counter = 0; counter < arraySize; counter++)
            reversedArray[counter] = (double) (arraySize - counter - 1);
        Arrays.sort(reversedArray);
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue("Resulting array not sorted",
                    reversedArray[counter] == (double) counter);
        double[] specials1 = new double[] { Double.NaN, Double.MAX_VALUE,
                Double.MIN_VALUE, 0d, -0d, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY };
        double[] specials2 = new double[] { 0d, Double.POSITIVE_INFINITY, -0d,
                Double.NEGATIVE_INFINITY, Double.MIN_VALUE, Double.NaN,
                Double.MAX_VALUE };
        double[] answer = new double[] { Double.NEGATIVE_INFINITY, -0d, 0d,
                Double.MIN_VALUE, Double.MAX_VALUE, Double.POSITIVE_INFINITY,
                Double.NaN };
        Arrays.sort(specials1);
        Object[] print1 = new Object[specials1.length];
        for (int i = 0; i < specials1.length; i++)
            print1[i] = new Double(specials1[i]);
        assertTrue("specials sort incorrectly 1: " + Arrays.asList(print1),
                Arrays.equals(specials1, answer));
        Arrays.sort(specials2);
        Object[] print2 = new Object[specials2.length];
        for (int i = 0; i < specials2.length; i++)
            print2[i] = new Double(specials2[i]);
        assertTrue("specials sort incorrectly 2: " + Arrays.asList(print2),
                Arrays.equals(specials2, answer));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sort",
        args = {double[].class, int.class, int.class}
    )
    public void test_sort$DII() {
        int startIndex = arraySize / 4;
        int endIndex = 3 * arraySize / 4;
        double[] reversedArray = new double[arraySize];
        double[] originalReversedArray = new double[arraySize];
        for (int counter = 0; counter < arraySize; counter++) {
            reversedArray[counter] = (double) (arraySize - counter - 1);
            originalReversedArray[counter] = reversedArray[counter];
        }
        Arrays.sort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                    reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        try {
            Arrays.sort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }
        try {
            Arrays.sort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        try {
            Arrays.sort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        try {
            Arrays.sort(new double[1], startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sort",
        args = {float[].class}
    )
    public void test_sort$F() {
        float[] reversedArray = new float[arraySize];
        for (int counter = 0; counter < arraySize; counter++)
            reversedArray[counter] = (float) (arraySize - counter - 1);
        Arrays.sort(reversedArray);
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue("Resulting array not sorted",
                    reversedArray[counter] == (float) counter);
        float[] specials1 = new float[] { Float.NaN, Float.MAX_VALUE,
                Float.MIN_VALUE, 0f, -0f, Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY };
        float[] specials2 = new float[] { 0f, Float.POSITIVE_INFINITY, -0f,
                Float.NEGATIVE_INFINITY, Float.MIN_VALUE, Float.NaN,
                Float.MAX_VALUE };
        float[] answer = new float[] { Float.NEGATIVE_INFINITY, -0f, 0f,
                Float.MIN_VALUE, Float.MAX_VALUE, Float.POSITIVE_INFINITY,
                Float.NaN };
        Arrays.sort(specials1);
        Object[] print1 = new Object[specials1.length];
        for (int i = 0; i < specials1.length; i++)
            print1[i] = new Float(specials1[i]);
        assertTrue("specials sort incorrectly 1: " + Arrays.asList(print1),
                Arrays.equals(specials1, answer));
        Arrays.sort(specials2);
        Object[] print2 = new Object[specials2.length];
        for (int i = 0; i < specials2.length; i++)
            print2[i] = new Float(specials2[i]);
        assertTrue("specials sort incorrectly 2: " + Arrays.asList(print2),
                Arrays.equals(specials2, answer));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sort",
        args = {float[].class, int.class, int.class}
    )
    public void test_sort$FII() {
        int startIndex = arraySize / 4;
        int endIndex = 3 * arraySize / 4;
        float[] reversedArray = new float[arraySize];
        float[] originalReversedArray = new float[arraySize];
        for (int counter = 0; counter < arraySize; counter++) {
            reversedArray[counter] = (float) (arraySize - counter - 1);
            originalReversedArray[counter] = reversedArray[counter];
        }
        Arrays.sort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                    reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        try {
            Arrays.sort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }
        try {
            Arrays.sort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        try {
            Arrays.sort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        try {
            Arrays.sort(new float[1], startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sort",
        args = {int[].class}
    )
    public void test_sort$I() {
        int[] reversedArray = new int[arraySize];
        for (int counter = 0; counter < arraySize; counter++)
            reversedArray[counter] = arraySize - counter - 1;
        Arrays.sort(reversedArray);
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue("Resulting array not sorted",
                    reversedArray[counter] == counter);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sort",
        args = {int[].class, int.class, int.class}
    )
    public void test_sort$III() {
        int startIndex = arraySize / 4;
        int endIndex = 3 * arraySize / 4;
        int[] reversedArray = new int[arraySize];
        int[] originalReversedArray = new int[arraySize];
        for (int counter = 0; counter < arraySize; counter++) {
            reversedArray[counter] = arraySize - counter - 1;
            originalReversedArray[counter] = reversedArray[counter];
        }
        Arrays.sort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                    reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        try {
            Arrays.sort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }
        try {
            Arrays.sort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        try {
            Arrays.sort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        try {
            Arrays.sort(new int[1], startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sort",
        args = {long[].class}
    )
    public void test_sort$J() {
        long[] reversedArray = new long[arraySize];
        for (int counter = 0; counter < arraySize; counter++)
            reversedArray[counter] = (long) (arraySize - counter - 1);
        Arrays.sort(reversedArray);
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue("Resulting array not sorted",
                    reversedArray[counter] == (long) counter);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sort",
        args = {long[].class, int.class, int.class}
    )
    public void test_sort$JII() {
        int startIndex = arraySize / 4;
        int endIndex = 3 * arraySize / 4;
        long[] reversedArray = new long[arraySize];
        long[] originalReversedArray = new long[arraySize];
        for (int counter = 0; counter < arraySize; counter++) {
            reversedArray[counter] = (long) (arraySize - counter - 1);
            originalReversedArray[counter] = reversedArray[counter];
        }
        Arrays.sort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                    reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        try {
            Arrays.sort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }
        try {
            Arrays.sort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        try {
            Arrays.sort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        try {
            Arrays.sort(new long[1], startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sort",
        args = {java.lang.Object[].class}
    )
    public void test_sort$Ljava_lang_Object() {
        Object[] reversedArray = new Object[arraySize];
        for (int counter = 0; counter < arraySize; counter++)
            reversedArray[counter] = objectArray[arraySize - counter - 1];
        Arrays.sort(reversedArray);
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue("Resulting array not sorted",
                    reversedArray[counter] == objectArray[counter]);
        Arrays.fill(reversedArray, 0, reversedArray.length/2, "String");
        Arrays.fill(reversedArray, reversedArray.length/2, reversedArray.length, new Integer(1));
        try {
            Arrays.sort(reversedArray);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sort",
        args = {java.lang.Object[].class, int.class, int.class}
    )
    public void test_sort$Ljava_lang_ObjectII() {
        int startIndex = arraySize / 4;
        int endIndex = 3 * arraySize / 4;
        Object[] reversedArray = new Object[arraySize];
        Object[] originalReversedArray = new Object[arraySize];
        for (int counter = 0; counter < arraySize; counter++) {
            reversedArray[counter] = objectArray[arraySize - counter - 1];
            originalReversedArray[counter] = reversedArray[counter];
        }
        Arrays.sort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                    ((Comparable) reversedArray[counter])
                            .compareTo(reversedArray[counter + 1]) <= 0);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        try {
            Arrays.sort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }
        try {
            Arrays.sort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        try {
            Arrays.sort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        try {
            Arrays.sort(new Object[1], startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }
        Arrays.fill(reversedArray, 0, reversedArray.length/2, "String");
        Arrays.fill(reversedArray, reversedArray.length/2, reversedArray.length, new Integer(1));
        try {
            Arrays.sort(reversedArray, reversedArray.length/4, 3*reversedArray.length/4);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
        }
        Arrays.sort(reversedArray, 0, reversedArray.length/4);
        Arrays.sort(reversedArray, 3*reversedArray.length/4, reversedArray.length);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sort",
        args = {java.lang.Object[].class, int.class, int.class, java.util.Comparator.class}
    )
    public void test_sort$Ljava_lang_ObjectIILjava_util_Comparator() {
        int startIndex = arraySize / 4;
        int endIndex = 3 * arraySize / 4;
        ReversedIntegerComparator comp = new ReversedIntegerComparator();
        Object[] originalArray = new Object[arraySize];
        for (int counter = 0; counter < arraySize; counter++)
            originalArray[counter] = objectArray[counter];
        Arrays.sort(objectArray, startIndex, endIndex, comp);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                    objectArray[counter] == originalArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds", comp.compare(
                    objectArray[counter], objectArray[counter + 1]) <= 0);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                    objectArray[counter] == originalArray[counter]);
        Arrays.fill(originalArray, 0, originalArray.length/2, "String");
        Arrays.fill(originalArray, originalArray.length/2, originalArray.length, new Integer(1));
        try {
            Arrays.sort(originalArray, startIndex, endIndex, comp);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
        }
        Arrays.sort(originalArray, endIndex, originalArray.length, comp);
        try {
            Arrays.sort(originalArray, endIndex, originalArray.length + 1, comp);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch(ArrayIndexOutOfBoundsException e) {
        }
        try {
            Arrays.sort(originalArray, -1, startIndex, comp);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch(ArrayIndexOutOfBoundsException e) {
        }
        try {
            Arrays.sort(originalArray, originalArray.length, endIndex, comp);
            fail("IllegalArgumentException expected");
        } catch(IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sort",
        args = {java.lang.Object[].class, java.util.Comparator.class}
    )
    public void test_sort$Ljava_lang_ObjectLjava_util_Comparator() {
        ReversedIntegerComparator comp = new ReversedIntegerComparator();
        Arrays.sort(objectArray, comp);
        for (int counter = 0; counter < arraySize - 1; counter++)
            assertTrue("Array not sorted correctly with custom comparator",
                    comp
                            .compare(objectArray[counter],
                                    objectArray[counter + 1]) <= 0);
        Arrays.fill(objectArray, 0, objectArray.length/2, "String");
        Arrays.fill(objectArray, objectArray.length/2, objectArray.length, new Integer(1));
        try {
            Arrays.sort(objectArray, comp);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sort",
        args = {short[].class}
    )
    public void test_sort$S() {
        short[] reversedArray = new short[arraySize];
        for (int counter = 0; counter < arraySize; counter++)
            reversedArray[counter] = (short) (arraySize - counter - 1);
        Arrays.sort(reversedArray);
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue("Resulting array not sorted",
                    reversedArray[counter] == (short) counter);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sort",
        args = {short[].class, int.class, int.class}
    )
    public void test_sort$SII() {
        int startIndex = arraySize / 4;
        int endIndex = 3 * arraySize / 4;
        short[] reversedArray = new short[arraySize];
        short[] originalReversedArray = new short[arraySize];
        for (int counter = 0; counter < arraySize; counter++) {
            reversedArray[counter] = (short) (arraySize - counter - 1);
            originalReversedArray[counter] = reversedArray[counter];
        }
        Arrays.sort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                    reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        try {
            Arrays.sort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }
        try {
            Arrays.sort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        try {
            Arrays.sort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        try {
            Arrays.sort(new short[1], startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "sort",
        args = {byte[].class, int.class, int.class}
    )
    public void test_java_util_Arrays_sort_byte_array_NPE() {
        byte[] byte_array_null = null;
        try {
            java.util.Arrays.sort(byte_array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            java.util.Arrays.sort(byte_array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "sort",
        args = {char[].class, int.class, int.class}
    )
    public void test_java_util_Arrays_sort_char_array_NPE() {
        char[] char_array_null = null;
        try {
            java.util.Arrays.sort(char_array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            java.util.Arrays.sort(char_array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "sort",
        args = {double[].class, int.class, int.class}
    )
    public void test_java_util_Arrays_sort_double_array_NPE() {
        double[] double_array_null = null;
        try {
            java.util.Arrays.sort(double_array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            java.util.Arrays.sort(double_array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "sort",
        args = {float[].class, int.class, int.class}
    )
    public void test_java_util_Arrays_sort_float_array_NPE() {
        float[] float_array_null = null;
        try {
            java.util.Arrays.sort(float_array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            java.util.Arrays.sort(float_array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "sort",
        args = {int[].class, int.class, int.class}
    )
    public void test_java_util_Arrays_sort_int_array_NPE() {
        int[] int_array_null = null;
        try {
            java.util.Arrays.sort(int_array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            java.util.Arrays.sort(int_array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "sort",
        args = {java.lang.Object[].class, int.class, int.class}
    )
    public void test_java_util_Arrays_sort_object_array_NPE() {
        Object[] object_array_null = null;
        try {
            java.util.Arrays.sort(object_array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            java.util.Arrays.sort(object_array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            java.util.Arrays.sort(object_array_null, (int) -1, (int) 1, null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "sort",
        args = {long[].class, int.class, int.class}
    )
    public void test_java_util_Arrays_sort_long_array_NPE() {
        long[] long_array_null = null;
        try {
            java.util.Arrays.sort(long_array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            java.util.Arrays.sort(long_array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "sort",
        args = {short[].class, int.class, int.class}
    )
    public void test_java_util_Arrays_sort_short_array_NPE() {
        short[] short_array_null = null;
        try {
            java.util.Arrays.sort(short_array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            java.util.Arrays.sort(short_array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    private static final int[] LENGTHS = { 0, 1, 2, 3, 5, 8, 13, 21, 34, 55, 100, 1000, 10000 };
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Agressive test of the sort methods for *all* primitive array types",
        method = "sort"
    )
    public void test_sort() {
        for (int len : LENGTHS) {
            PrimitiveTypeArrayBuilder.reset();
            int[] golden = new int[len];
            for (int m = 1; m < 2 * len; m *= 2) {
                for (PrimitiveTypeArrayBuilder builder : PrimitiveTypeArrayBuilder.values()) {
                    builder.build(golden, m);
                    int[] test = golden.clone();
                    for (PrimitiveTypeConverter converter : PrimitiveTypeConverter.values()) {
                        Object convertedGolden = converter.convert(golden);
                        Object convertedTest = converter.convert(test);
                        sort(convertedTest);
                        checkSorted(convertedTest);
                        assertEquals(checkSum(convertedGolden), checkSum(convertedTest));
                    }
                }
            }
        }
    }
    private void sort(Object array) {
        if (array instanceof int[]) {
            Arrays.sort((int[]) array);
        }
        else if (array instanceof long[]) {
            Arrays.sort((long[]) array);
        } else if (array instanceof short[]) {
            Arrays.sort((short[]) array);
        } else if (array instanceof byte[]) {
            Arrays.sort((byte[]) array);
        } else if (array instanceof char[]) {
            Arrays.sort((char[]) array);
        } else if (array instanceof float[]) {
            Arrays.sort((float[]) array);
        } else if (array instanceof double[]) {
            Arrays.sort((double[]) array);
        } else {
            fail("Unknow type of array: " + array.getClass());
        }
    }
    private void checkSorted(Object array) {
        if (array instanceof int[]) {
            checkSorted((int[]) array);
        } else if (array instanceof long[]) {
            checkSorted((long[]) array);
        } else if (array instanceof short[]) {
            checkSorted((short[]) array);
        } else if (array instanceof byte[]) {
            checkSorted((byte[]) array);
        } else if (array instanceof char[]) {
            checkSorted((char[]) array);
        } else if (array instanceof float[]) {
            checkSorted((float[]) array);
        } else if (array instanceof double[]) {
            checkSorted((double[]) array);
        } else {
            fail("Unknow type of array: " + array.getClass());
        }
    }
    private void checkSorted(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                orderFail(i, "" + a[i], "" + a[i + 1]);
            }
        }
    }
    private void checkSorted(long[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                orderFail(i, "" + a[i], "" + a[i + 1]);
            }
        }
    }
    private void checkSorted(short[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                orderFail(i, "" + a[i], "" + a[i + 1]);
            }
        }
    }
    private void checkSorted(byte[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                orderFail(i, "" + a[i], "" + a[i + 1]);
            }
        }
    }
    private void checkSorted(char[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                orderFail(i, "" + a[i], "" + a[i + 1]);
            }
        }
    }
    private void checkSorted(float[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                orderFail(i, "" + a[i], "" + a[i + 1]);
            }
        }
    }
    private void checkSorted(double[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                orderFail(i, "" + a[i], "" + a[i + 1]);
            }
        }
    }
    private void orderFail(int index, String value1, String value2) {
        fail("Array is not sorted at " + index + "-th position: " + value1 + " and " + value2);
    }
    private int checkSum(Object array) {
        if (array instanceof int[]) {
            return checkSum((int[]) array);
        } else if (array instanceof long[]) {
            return checkSum((long[]) array);
        } else if (array instanceof short[]) {
            return checkSum((short[]) array);
        } else if (array instanceof byte[]) {
            return checkSum((byte[]) array);
        } else if (array instanceof char[]) {
            return checkSum((char[]) array);
        } else if (array instanceof float[]) {
            return checkSum((float[]) array);
        } else if (array instanceof double[]) {
            return checkSum((double[]) array);
        } else {
            fail("Unknow type of array: " + array.getClass());
        }
        throw new AssertionError(); 
    }
    private int checkSum(int[] a) {
        int checkSum = 0;
        for (int e : a) {
            checkSum ^= e; 
        }
        return checkSum;
    }
    private int checkSum(long[] a) {
        long checkSum = 0;
        for (long e : a) {
            checkSum ^= e; 
        }
        return (int) checkSum;
    }
    private int checkSum(short[] a) {
        short checkSum = 0;
        for (short e : a) {
            checkSum ^= e; 
        }
        return (int) checkSum;
    }
    private int checkSum(byte[] a) {
        byte checkSum = 0;
        for (byte e : a) {
            checkSum ^= e; 
        }
        return (int) checkSum;
    }
    private int checkSum(char[] a) {
        char checkSum = 0;
        for (char e : a) {
            checkSum ^= e; 
        }
        return (int) checkSum;
    }
    private int checkSum(float[] a) {
        int checkSum = 0;
        for (float e : a) {
            checkSum ^= (int) e; 
        }
        return checkSum;
    }
    private int checkSum(double[] a) {
        int checkSum = 0;
        for (double e : a) {
            checkSum ^= (int) e; 
        }
        return checkSum;
    }
    private enum PrimitiveTypeArrayBuilder {
        RANDOM {
            void build(int[] a, int m) {
                for (int i = 0; i < a.length; i++) {
                    a[i] = ourRandom.nextInt();
                }
            }
        },
        ASCENDING {
            void build(int[] a, int m) {
                for (int i = 0; i < a.length; i++) {
                    a[i] = m + i;
                }
            }
        },
        DESCENDING {
            void build(int[] a, int m) {
                for (int i = 0; i < a.length; i++) {
                    a[i] = a.length - m - i;
                }
            }
        },
        ALL_EQUAL {
            void build(int[] a, int m) {
                for (int i = 0; i < a.length; i++) {
                    a[i] = m;
                }
            }
        },
        SAW {
            void build(int[] a, int m) {
                int incCount = 1;
                int decCount = a.length;
                int i = 0;
                int period = m;
                m--;
                while (true) {
                    for (int k = 1; k <= period; k++) {
                        if (i >= a.length) {
                            return;
                        }
                        a[i++] = incCount++;
                    }
                    period += m;
                    for (int k = 1; k <= period; k++) {
                        if (i >= a.length) {
                            return;
                        }
                        a[i++] = decCount--;
                    }
                    period += m;
                }
            }
        },
        REPEATED {
            void build(int[] a, int m) {
                for (int i = 0; i < a.length; i++) {
                    a[i] = i % m;
                }
            }
        },
        DUPLICATED {
            void build(int[] a, int m) {
                for (int i = 0; i < a.length; i++) {
                    a[i] = ourRandom.nextInt(m);
                }
            }
        },
        ORGAN_PIPES {
            void build(int[] a, int m) {
                int middle = a.length / (m + 1);
                for (int i = 0; i < middle; i++) {
                    a[i] = i;
                }
                for (int i = middle; i < a.length ; i++) {
                    a[i] = a.length - i - 1;
                }
            }
        },
        STAGGER {
            void build(int[] a, int m) {
                for (int i = 0; i < a.length; i++) {
                    a[i] = (i * m + i) % a.length;
                }
            }
        },
        PLATEAU {
            void build(int[] a, int m) {
                for (int i = 0; i < a.length; i++) {
                    a[i] =  Math.min(i, m);
                }
            }
        },
        SHUFFLE {
            void build(int[] a, int m) {
                for (int i = 0; i < a.length; i++) {
                    a[i] = ourRandom.nextBoolean() ? (ourFirst += 2) : (ourSecond += 2);
                }
            }
        };
        abstract void build(int[] a, int m);
        static void reset() {
            ourRandom = new Random(666);
            ourFirst = 0;
            ourSecond = 0;
        }
        @Override
        public String toString() {
            String name = name();
            for (int i = name.length(); i < 12; i++) {
                name += " " ;
            }
            return name;
        }
        private static int ourFirst;
        private static int ourSecond;
        private static Random ourRandom = new Random(666);
    }
    private enum PrimitiveTypeConverter {
        INT {
            Object convert(int[] a) {
                return a;
            }
        },
        LONG {
            Object convert(int[] a) {
                long[] b = new long[a.length];
                for (int i = 0; i < a.length; i++) {
                    b[i] = (int) a[i];
                }
                return b;
            }
        },
        BYTE {
            Object convert(int[] a) {
                byte[] b = new byte[a.length];
                for (int i = 0; i < a.length; i++) {
                    b[i] = (byte) a[i];
                }
                return b;
            }
        },
        SHORT {
            Object convert(int[] a) {
                short[] b = new short[a.length];
                for (int i = 0; i < a.length; i++) {
                    b[i] = (short) a[i];
                }
                return b;
            }
        },
        CHAR {
            Object convert(int[] a) {
                char[] b = new char[a.length];
                for (int i = 0; i < a.length; i++) {
                    b[i] = (char) a[i];
                }
                return b;
            }
        },
        FLOAT {
            Object convert(int[] a) {
                float[] b = new float[a.length];
                for (int i = 0; i < a.length; i++) {
                    b[i] = (float) a[i];
                }
                return b;
            }
        },
        DOUBLE {
            Object convert(int[] a) {
                double[] b = new double[a.length];
                for (int i = 0; i < a.length; i++) {
                    b[i] = (double) a[i];
                }
                return b;
            }
        };
        abstract Object convert(int[] a);
        public String toString() {
            String name = name();
            for (int i = name.length(); i < 9; i++) {
                name += " " ;
            }
            return name;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "deepEquals",
        args = {java.lang.Object[].class, java.lang.Object[].class}
    )
    public void test_deepEquals$Ljava_lang_ObjectLjava_lang_Object() {
       int [] a1 = {1, 2, 3};
       short [] a2 = {0, 1};
       Object [] a3 = {new Integer(1), a2};
       int [] a4 = {6, 5, 4};
       int [] b1 = {1, 2, 3};
       short [] b2 = {0, 1};
       Object [] b3 = {new Integer(1), b2};
       Object a [] = {a1, a2, a3};
       Object b [] = {b1, b2, b3};
       assertFalse(Arrays.equals(a, b));
       assertTrue(Arrays.deepEquals(a,b));
       a[2] = a4;
       assertFalse(Arrays.deepEquals(a, b));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "deepHashCode",
        args = {java.lang.Object[].class}
    )
    public void test_deepHashCode$Ljava_lang_Object() {
        int [] a1 = {1, 2, 3};
        short [] a2 = {0, 1};
        Object [] a3 = {new Integer(1), a2};
        int [] b1 = {1, 2, 3};
        short [] b2 = {0, 1};
        Object [] b3 = {new Integer(1), b2};
        Object a [] = {a1, a2, a3};
        Object b [] = {b1, b2, b3};
        int deep_hash_a = Arrays.deepHashCode(a);
        int deep_hash_b = Arrays.deepHashCode(b);
        assertEquals(deep_hash_a, deep_hash_b);
     }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {boolean[].class}
    )
    public void test_hashCode$LZ() {
        int listHashCode;
        int arrayHashCode;
        boolean [] boolArr = {true, false, false, true, false};
        List listOfBoolean = new LinkedList();
        for (int i = 0; i < boolArr.length; i++) {
            listOfBoolean.add(new Boolean(boolArr[i]));
        }
        listHashCode = listOfBoolean.hashCode();
        arrayHashCode = Arrays.hashCode(boolArr);
        assertEquals(listHashCode, arrayHashCode);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {int[].class}
    )
    public void test_hashCode$LI() {
        int listHashCode;
        int arrayHashCode;
        int [] intArr = {10, 5, 134, 7, 19};
        List listOfInteger = new LinkedList();
        for (int i = 0; i < intArr.length; i++) {
            listOfInteger.add(new Integer(intArr[i]));
        }
        listHashCode = listOfInteger.hashCode();
        arrayHashCode = Arrays.hashCode(intArr);
        assertEquals(listHashCode, arrayHashCode);
        int [] intArr2 = {10, 5, 134, 7, 19};
        assertEquals(Arrays.hashCode(intArr2), Arrays.hashCode(intArr));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {char[].class}
    )
    public void test_hashCode$LC() {
        int listHashCode;
        int arrayHashCode;
        char [] charArr = {'a', 'g', 'x', 'c', 'm'};
        List listOfCharacter = new LinkedList();
        for (int i = 0; i < charArr.length; i++) {
            listOfCharacter.add(new Character(charArr[i]));
        }
        listHashCode = listOfCharacter.hashCode();
        arrayHashCode = Arrays.hashCode(charArr);
        assertEquals(listHashCode, arrayHashCode);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {byte[].class}
    )
    public void test_hashCode$LB() {
        int listHashCode;
        int arrayHashCode;
        byte [] byteArr = {5, 9, 7, 6, 17};
        List listOfByte = new LinkedList();
        for (int i = 0; i < byteArr.length; i++) {
            listOfByte.add(new Byte(byteArr[i]));
        }
        listHashCode = listOfByte.hashCode();
        arrayHashCode = Arrays.hashCode(byteArr);
        assertEquals(listHashCode, arrayHashCode);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {long[].class}
    )
    public void test_hashCode$LJ() {
        int listHashCode;
        int arrayHashCode;
        long [] longArr = {67890234512l, 97587236923425l, 257421912912l,
                6754268100l, 5};
        List listOfLong = new LinkedList();
        for (int i = 0; i < longArr.length; i++) {
            listOfLong.add(new Long(longArr[i]));
        }
        listHashCode = listOfLong.hashCode();
        arrayHashCode = Arrays.hashCode(longArr);
        assertEquals(listHashCode, arrayHashCode);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {float[].class}
    )
    public void test_hashCode$LF() {
        int listHashCode;
        int arrayHashCode;
        float [] floatArr = {0.13497f, 0.268934f, 12e-5f, -3e+2f, 10e-4f};
        List listOfFloat = new LinkedList();
        for (int i = 0; i < floatArr.length; i++) {
            listOfFloat.add(new Float(floatArr[i]));
        }
        listHashCode = listOfFloat.hashCode();
        arrayHashCode = Arrays.hashCode(floatArr);
        assertEquals(listHashCode, arrayHashCode);
        float [] floatArr2 = {0.13497f, 0.268934f, 12e-5f, -3e+2f, 10e-4f};
        assertEquals(Arrays.hashCode(floatArr2), Arrays.hashCode(floatArr));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {double[].class}
    )
    public void test_hashCode$LD() {
        int listHashCode;
        int arrayHashCode;
        double [] doubleArr = {0.134945657, 0.0038754, 11e-150, -30e-300, 10e-4};
        List listOfDouble = new LinkedList();
        for (int i = 0; i < doubleArr.length; i++) {
            listOfDouble.add(new Double(doubleArr[i]));
        }
        listHashCode = listOfDouble.hashCode();
        arrayHashCode = Arrays.hashCode(doubleArr);
        assertEquals(listHashCode, arrayHashCode);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {short[].class}
    )
    public void test_hashCode$LS() {
        int listHashCode;
        int arrayHashCode;
        short [] shortArr = {35, 13, 45, 2, 91};
        List listOfShort = new LinkedList();
        for (int i = 0; i < shortArr.length; i++) {
            listOfShort.add(new Short(shortArr[i]));
        }
        listHashCode = listOfShort.hashCode();
        arrayHashCode = Arrays.hashCode(shortArr);
        assertEquals(listHashCode, arrayHashCode);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {java.lang.Object[].class}
    )
    public void test_hashCode$Ljava_lang_Object() {
        int listHashCode;
        int arrayHashCode;
        Object[] objectArr = {new Integer(1), new Float(10e-12f), null};
        List listOfObject= new LinkedList();
        for (int i = 0; i < objectArr.length; i++) {
            listOfObject.add(objectArr[i]);
        }
        listHashCode = listOfObject.hashCode();
        arrayHashCode = Arrays.hashCode(objectArr);
        assertEquals(listHashCode, arrayHashCode);
    }
    protected void setUp() {
        objArray = new Object[arraySize];
        for (int i = 0; i < objArray.length; i++)
            objArray[i] = new Integer(i);
        booleanArray = new boolean[arraySize];
        byteArray = new byte[arraySize];
        charArray = new char[arraySize];
        doubleArray = new double[arraySize];
        floatArray = new float[arraySize];
        intArray = new int[arraySize];
        longArray = new long[arraySize];
        objectArray = new Object[arraySize];
        shortArray = new short[arraySize];
        for (int counter = 0; counter < arraySize; counter++) {
            byteArray[counter] = (byte) counter;
            charArray[counter] = (char) (counter + 1);
            doubleArray[counter] = counter;
            floatArray[counter] = counter;
            intArray[counter] = counter;
            longArray[counter] = counter;
            objectArray[counter] = objArray[counter];
            shortArray[counter] = (short) counter;
        }
        for (int counter = 0; counter < arraySize; counter += 2) {
            booleanArray[counter] = false;
            booleanArray[counter + 1] = true;
        }
    }
    protected void tearDown() {
        objArray = null;
        booleanArray = null;
        byteArray = null;
        charArray = null;
        doubleArray = null;
        floatArray = null;
        intArray = null;
        longArray = null;
        objectArray = null;
        shortArray = null;
    }
}

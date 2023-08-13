@TestTargetClass(BatchUpdateException.class)
public class BatchUpdateExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BatchUpdateException",
        args = {}
    )
    public void testBatchUpdateException() {
        int[] theFinalStates1 = { 0 }; 
        int[][] theFinalStates2 = { null }; 
        String[] theFinalStates3 = { null }; 
        String[] theFinalStates4 = { null }; 
        Exception[] theExceptions = { null };
        BatchUpdateException aBatchUpdateException;
        int loopCount = 1;
        for (int i = 0; i < loopCount; i++) {
            try {
                aBatchUpdateException = new BatchUpdateException();
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getErrorCode(),
                        theFinalStates1[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getUpdateCounts(),
                        theFinalStates2[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getSQLState(), theFinalStates3[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getMessage(), theFinalStates4[i]);
            } catch (Exception e) {
                if (theExceptions[i] == null) {
                    fail(i + "Unexpected exception");
                }
                assertEquals(i + "Exception mismatch", e.getClass(),
                        theExceptions[i].getClass());
                assertEquals(i + "Exception mismatch", e.getMessage(),
                        theExceptions[i].getMessage());
            } 
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BatchUpdateException",
        args = {int[].class}
    )
    public void testBatchUpdateExceptionintArray() {
        int[][] init1 = { { 1, 2, 3 }, null };
        int[] theFinalStates1 = { 0, 0 }; 
        int[][] theFinalStates2 = init1; 
        String[] theFinalStates3 = { null, null }; 
        String[] theFinalStates4 = { null, null }; 
        Exception[] theExceptions = { null, null };
        BatchUpdateException aBatchUpdateException;
        int loopCount = init1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aBatchUpdateException = new BatchUpdateException(init1[i]);
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getErrorCode(),
                        theFinalStates1[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getUpdateCounts(),
                        theFinalStates2[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getSQLState(), theFinalStates3[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getMessage(), theFinalStates4[i]);
            } catch (Exception e) {
                if (theExceptions[i] == null) {
                    fail(i + "Unexpected exception");
                }
                assertEquals(i + "Exception mismatch", e.getClass(),
                        theExceptions[i].getClass());
                assertEquals(i + "Exception mismatch", e.getMessage(),
                        theExceptions[i].getMessage());
            } 
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BatchUpdateException",
        args = {java.lang.String.class, int[].class}
    )
    public void testBatchUpdateExceptionStringintArray() {
        String[] init1 = { "a", "1", "valid1", "----", "&valid*", null, "",
                ".", "a" };
        int[][] init2 = { { 1, 2, 3 }, {}, { 3 }, null, { 5, 5 }, { 6 },
                { 121, 2, 1 }, { 1 }, { 1, 2 } };
        int[] theFinalStates1 = { 0, 0, 0, 0, 0, 0, 0, 0, 0 }; 
        int[][] theFinalStates2 = init2;
        String[] theFinalStates3 = { null, null, null, null, null, null, null,
                null, null };
        String[] theFinalStates4 = init1; 
        Exception[] theExceptions = { null, null, null, null, null, null, null,
                null, null };
        BatchUpdateException aBatchUpdateException;
        int loopCount = init1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aBatchUpdateException = new BatchUpdateException(init1[i],
                        init2[i]);
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getErrorCode(),
                        theFinalStates1[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getUpdateCounts(),
                        theFinalStates2[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getSQLState(), theFinalStates3[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getMessage(), theFinalStates4[i]);
            } catch (Exception e) {
                if (theExceptions[i] == null) {
                    fail(i + "Unexpected exception");
                }
                assertEquals(i + "Exception mismatch", e.getClass(),
                        theExceptions[i].getClass());
                assertEquals(i + "Exception mismatch", e.getMessage(),
                        theExceptions[i].getMessage());
            } 
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BatchUpdateException",
        args = {java.lang.String.class, java.lang.String.class, int[].class}
    )
    public void testBatchUpdateExceptionStringStringintArray() {
        String[] init1 = { "a", "1", "valid1", "----", "&valid*", null, "",
                ".", "a", "a" };
        String[] init2 = { "a", "1", "valid1", "----", "&valid*", "a", null,
                "", ".", "a" };
        int[][] init3 = { { 1, 2, 3 }, {}, { 3 }, { 5, 5 }, { 6 },
                { 121, 2, 1 }, { 1 }, { 1, 2 }, { 1 }, { 2 }, null };
        int[] theFinalStates1 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; 
        int[][] theFinalStates2 = init3;
        String[] theFinalStates3 = init2;
        String[] theFinalStates4 = init1; 
        Exception[] theExceptions = { null, null, null, null, null, null, null,
                null, null, null, null };
        BatchUpdateException aBatchUpdateException;
        int loopCount = init1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aBatchUpdateException = new BatchUpdateException(init1[i],
                        init2[i], init3[i]);
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getErrorCode(),
                        theFinalStates1[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getUpdateCounts(),
                        theFinalStates2[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getSQLState(), theFinalStates3[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getMessage(), theFinalStates4[i]);
            } catch (Exception e) {
                if (theExceptions[i] == null) {
                    fail(i + "Unexpected exception");
                }
                assertEquals(i + "Exception mismatch", e.getClass(),
                        theExceptions[i].getClass());
                assertEquals(i + "Exception mismatch", e.getMessage(),
                        theExceptions[i].getMessage());
            } 
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BatchUpdateException",
        args = {java.lang.String.class, java.lang.String.class, int.class, int[].class}
    )
    public void testBatchUpdateExceptionStringStringintintArray() {
        String[] init1 = { "a", "1", "valid1", "----", "&valid*", null, "",
                ".", "a", "a" };
        String[] init2 = { "a", "1", "valid1", "----", "&valid*", "a", null,
                "", ".", "a" };
        int[] init3 = { -2147483648, 2147483647, 0, -492417162, -156220255,
                -173012890, -631026360, -2147483648, -2147483648, -2147483648,
                -2147483648 };
        int[][] init4 = { { 1, 2, 3 }, {}, { 3 }, { 5, 5 }, { 6 },
                { 121, 2, 1 }, { 1 }, { 1, 2 }, { 1 }, { 2 }, null };
        int[] theFinalStates1 = init3; 
        int[][] theFinalStates2 = init4;
        String[] theFinalStates3 = init2;
        String[] theFinalStates4 = init1; 
        Exception[] theExceptions = { null, null, null, null, null, null, null,
                null, null, null, null };
        BatchUpdateException aBatchUpdateException;
        int loopCount = init1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aBatchUpdateException = new BatchUpdateException(init1[i],
                        init2[i], init3[i], init4[i]);
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getErrorCode(),
                        theFinalStates1[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getUpdateCounts(),
                        theFinalStates2[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getSQLState(), theFinalStates3[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getMessage(), theFinalStates4[i]);
            } catch (Exception e) {
                if (theExceptions[i] == null) {
                    fail(i + "Unexpected exception");
                }
                assertEquals(i + "Exception mismatch", e.getClass(),
                        theExceptions[i].getClass());
                assertEquals(i + "Exception mismatch", e.getMessage(),
                        theExceptions[i].getMessage());
            } 
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getUpdateCounts",
        args = {}
    )
    public void testGetUpdateCounts() {
        BatchUpdateException aBatchUpdateException;
        int[][] init1 = { { 1, 2, 3 }, {}, null };
        int[] theReturn;
        int[][] theReturns = init1;
        int[] theFinalStates1 = { 0, 0, 0 }; 
        int[][] theFinalStates2 = init1; 
        String[] theFinalStates3 = { null, null, null }; 
        String[] theFinalStates4 = { null, null, null }; 
        Exception[] theExceptions = { null, null, null };
        int loopCount = init1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aBatchUpdateException = new BatchUpdateException(init1[i]);
                theReturn = aBatchUpdateException.getUpdateCounts();
                if (theExceptions[i] != null) {
                    fail(i + "Exception missed");
                }
                assertEquals(i + "Return value mismatch", theReturn,
                        theReturns[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getErrorCode(),
                        theFinalStates1[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getUpdateCounts(),
                        theFinalStates2[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getSQLState(), theFinalStates3[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getMessage(), theFinalStates4[i]);
            } catch (Exception e) {
                if (theExceptions[i] == null) {
                    fail(i + "Unexpected exception");
                }
                assertEquals(i + "Exception mismatch", e.getClass(),
                        theExceptions[i].getClass());
                assertEquals(i + "Exception mismatch", e.getMessage(),
                        theExceptions[i].getMessage());
            } 
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Serialization test",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        BatchUpdateException object = new BatchUpdateException();
        SerializationTest.verifySelf(object, BATCHUPDATEEXCEPTION_COMPARATOR);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Serialization test",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        int vendorCode = 10;
        int[] updateCounts = { 1, 2, 3, 4 };
        BatchUpdateException object = new BatchUpdateException("reason",
                "SQLState", vendorCode, updateCounts);
        SerializationTest.verifyGolden(this, object,
                BATCHUPDATEEXCEPTION_COMPARATOR);
    }
    private static final SerializableAssert BATCHUPDATEEXCEPTION_COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);
            BatchUpdateException initThr = (BatchUpdateException) initial;
            BatchUpdateException dserThr = (BatchUpdateException) deserialized;
            int[] initUpdateCounts = initThr.getUpdateCounts();
            int[] dserUpdateCounts = dserThr.getUpdateCounts();
            assertTrue(Arrays.equals(initUpdateCounts, dserUpdateCounts));
        }
    };
} 

@TestTargetClass(ResultSet.class)
public class ResultSetNotSupportedTests extends SQLTest {
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getArray",
        args = {int.class}
    )
    public void testGetArrayInt() {
        fail();
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getArray",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetArrayString() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getAsciiStream",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetAsciiStreamInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getAsciiStream",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetAsciiStreamString() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getBigDecimal",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBigDecimalInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getBigDecimal",
        args = {int.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBigDecimalIntInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getBigDecimal",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBigDecimalString() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getBigDecimal",
        args = {java.lang.String.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBigDecimalStringInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getBinaryStream",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBinaryStreamInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getBinaryStream",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBinaryStreamString() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getBlob",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBlobInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getBlob",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBlobString() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getBoolean",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBooleanInt() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getBoolean",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBooleanString() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getByte",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetByteString() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getByte",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetByteInt() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getCharacterStream",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetCharacterStreamInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getCharacterStream",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetCharacterStreamString() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getClob",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetClobInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getClob",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetClobString() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported, setColumnName is not supported, therefore GetColumname can not be tested",
        method = "getCursorName",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testGetCursorName() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported.",
        method = "getFetchDirection",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testGetFetchDirection() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getFetchSize",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testGetFetchSize() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getObject",
        args = {int.class, java.util.Map.class}
    )
    @KnownFailure("Not Supported")
    public void testGetObjectIntMapOfStringClassOfQ() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getObject",
        args = {java.lang.String.class, java.util.Map.class}
    )
    @KnownFailure("Not Supported")
    public void testGetObjectStringMapOfStringClassOfQ() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getRef",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetRefInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getRef",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetRefString() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getUnicodeStream",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetUnicodeStreamInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getUnicodeStream",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetUnicodeStreamString() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getWarnings",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testGetWarnings() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "SQLException checking missed",
        method = "cancelRowUpdates",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testCancelRowUpdates() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "deleteRow",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testDeleteRow() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "insertRow",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testInsertRow() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "moveToCurrentRow",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testMoveToCurrentRow() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "moveToInsertRow",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testMoveToInsertRow() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "refreshRow",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testRefreshRow() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "rowDeleted",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testRowDeleted() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "rowInserted",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testRowInserted() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "rowUpdated",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testRowUpdated() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setFetchDirection",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testSetFetchDirection() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setFetchSize",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testSetFetchSize() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "updateArray",
        args = {int.class, java.sql.Array.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateArrayIntArray() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "updateArray",
        args = {java.lang.String.class, java.sql.Array.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateArrayStringArray() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "updateAsciiStream",
        args = {int.class, java.io.InputStream.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateAsciiStreamIntInputStreamInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "updateAsciiStream",
        args = {String.class, java.io.InputStream.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateAsciiStreamStringInputStreamInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBigDecimal",
        args = {int.class, java.math.BigDecimal.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBigDecimalIntBigDecimal() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBigDecimal",
        args = {java.lang.String.class, java.math.BigDecimal.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBigDecimalStringBigDecimal() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBinaryStream",
        args = {int.class, java.io.InputStream.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBinaryStreamIntInputStreamInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBinaryStream",
        args = {java.lang.String.class, java.io.InputStream.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBinaryStreamStringInputStreamInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBlob",
        args = {int.class, java.sql.Blob.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBlobIntBlob() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBlob",
        args = {java.lang.String.class, java.sql.Blob.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBlobStringBlob() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBoolean",
        args = {int.class, boolean.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBooleanIntBoolean() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBoolean",
        args = {java.lang.String.class, boolean.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBooleanStringBoolean() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateByte",
        args = {int.class, byte.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateByteIntByte() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateByte",
        args = {java.lang.String.class, byte.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateByteStringByte() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBytes",
        args = {int.class, byte[].class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBytesIntByteArray() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBytes",
        args = {java.lang.String.class, byte[].class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBytesStringByteArray() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateCharacterStream",
        args = {int.class, java.io.Reader.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateCharacterStreamIntReaderInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateCharacterStream",
        args = {java.lang.String.class, java.io.Reader.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateCharacterStreamStringReaderInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateClob",
        args = {int.class, java.sql.Clob.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateClobIntClob() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateClob",
        args = {java.lang.String.class, java.sql.Clob.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateClobStringClob() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateDate",
        args = {int.class, java.sql.Date.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateDateIntDate() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateDate",
        args = {java.lang.String.class, java.sql.Date.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateDateStringDate() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateDouble",
        args = {int.class, double.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateDoubleIntDouble() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateDouble",
        args = {java.lang.String.class, double.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateDoubleStringDouble() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateFloat",
        args = {int.class, float.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateFloatIntFloat() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateFloat",
        args = {java.lang.String.class, float.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateFloatStringFloat() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateInt",
        args = {int.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateIntIntInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateInt",
        args = {String.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateIntStringInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateLong",
        args = {int.class, long.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateLongIntLong() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateLong",
        args = {java.lang.String.class, long.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateLongStringLong() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateNull",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateNullInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateNull",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateNullString() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateObject",
        args = {int.class, java.lang.Object.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateObjectIntObject() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateObject",
        args = {int.class, java.lang.Object.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateObjectIntObjectInt() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "updateObject",
            args = {String.class, Object.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateStringObject() {
    }
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "updateObject",
            args = {String.class, Object.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateStringObjectInt() {
    }
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "updateRef",
            args = {int.class, Ref.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateRefIntRef() {
    }
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "updateRef",
            args = {String.class, Ref.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateRefStringRef() {
    }
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "updateRow",
            args = {}
    )
    @KnownFailure("Not Supported")
    public void testUpdateRow() {
    }
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "updateShort",
            args = {int.class, short.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateShortIntShort() {
    }
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "updateShort",
            args = {String.class, short.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateShortStringShort() {
    }
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "updateString",
            args = {int.class, String.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateStringIntString() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "updateTime",
        args = {int.class, java.sql.Time.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateTimeIntTime() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "updateTime",
        args = {java.lang.String.class, java.sql.Time.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateTimeStringTime() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "updateTimestamp",
        args = {int.class, java.sql.Timestamp.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateTimestampIntTimestamp() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "updateTimestamp",
        args = {java.lang.String.class, java.sql.Timestamp.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateTimestampStringTimestamp() {
        fail("Not yet implemented");
    }
}

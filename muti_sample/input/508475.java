@TestTargetClass(ConnectionEvent.class)
public class ConnectionEventTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "functional test missing but not feasible: no implementation available.",
        method = "ConnectionEvent",
        args = {javax.sql.PooledConnection.class}
    )  
    public void testConstructorConnection() {
        try {
            new ConnectionEvent(null);
            fail("illegal argument exception expected");
        } catch (IllegalArgumentException e) {
        }
        Impl_PooledConnection ipc = new Impl_PooledConnection();
        ConnectionEvent ce = new ConnectionEvent(ipc);
        assertSame(ipc, ce.getSource());
        assertNull(ce.getSQLException());
        ConnectionEvent ce2 = new ConnectionEvent(ipc,null);
        assertSame(ce2.getSource(),ce.getSource());
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "functional test missing but not feasible: no implementation available.",
        method = "ConnectionEvent",
        args = {javax.sql.PooledConnection.class, java.sql.SQLException.class}
    )  
    public void testConstructorConnectionSQLException() {
        try {
            new ConnectionEvent(null, null);
            fail("illegal argument exception expected");
        } catch (IllegalArgumentException e) {
        }
        Impl_PooledConnection ipc = new Impl_PooledConnection();
        ConnectionEvent ce = new ConnectionEvent(ipc, null);
        assertSame(ipc, ce.getSource());
        assertNull(ce.getSQLException());
        SQLException e = new SQLException();
        ce = new ConnectionEvent(ipc, e);
        assertSame(ipc, ce.getSource());
        assertSame(e, ce.getSQLException());
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "functional test missing but not feasible: no implementation available.",
        method = "getSQLException",
        args = {}
    )    
    public void testGetSQLException() {
        Impl_PooledConnection ipc = new Impl_PooledConnection();
        ConnectionEvent ce = new ConnectionEvent(ipc);
        ConnectionEvent ce2 = new ConnectionEvent(ipc, null);
        assertNull(ce.getSQLException());
        assertEquals(ce2.getSQLException(), ce.getSQLException());
        SQLException e = new SQLException();
        ConnectionEvent ce3 = new ConnectionEvent(ipc, e);
        assertNotNull(ce3.getSQLException());
        assertNotSame(ce3.getSQLException(), ce2.getSQLException());
    }
    @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "",
            method = "!SerializationSelf",
            args = {}
    )
    public void testSerializationSelf() throws Exception {
        Impl_PooledConnection ipc = new Impl_PooledConnection();
        SQLException e = new SQLException();
        ConnectionEvent ce = new ConnectionEvent(ipc, e);
        SerializationTest.verifySelf(ce, CONNECTIONEVENT_COMPARATOR);
    }
    @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "",
            method = "!Serialization",
            args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        Impl_PooledConnection ipc = new Impl_PooledConnection();
        SQLException nextSQLException = new SQLException("nextReason",
                "nextSQLState", 33);
        int vendorCode = 10;
        SQLException sqlException = new SQLException("reason", "SQLState",
                vendorCode);
        sqlException.setNextException(nextSQLException);
        ConnectionEvent ce = new ConnectionEvent(ipc, sqlException);
        SerializationTest.verifyGolden(this, ce, CONNECTIONEVENT_COMPARATOR);
    }
    private static final SerializableAssert CONNECTIONEVENT_COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            ConnectionEvent ceInitial = (ConnectionEvent) initial;
            ConnectionEvent ceDeser = (ConnectionEvent) deserialized;
            SQLException initThr = ceInitial.getSQLException();
            SQLException dserThr = ceDeser.getSQLException();
            assertEquals(initThr.getSQLState(), dserThr.getSQLState());
            assertEquals(initThr.getErrorCode(), dserThr.getErrorCode());
            if (initThr.getNextException() == null) {
                assertNull(dserThr.getNextException());
            }
        }
    };
}

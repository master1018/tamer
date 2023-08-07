public class LsfileacObjectJmsActionTest extends AbstractAdapterObjectJmsClientTester {
    public LsfileacObjectJmsActionTest() {
        super("lsfileac", getRequestObject(), null);
    }
    public void checkObjects(final Object expectedObject, final Object actualObject) {
        LsfileacResponseHolder reply = (LsfileacResponseHolder) actualObject;
        LsfileacCases.checkJavaObjectReplyData(reply.getReplyData());
        LsfileacCases.checkJavaObjectReplyStatus(reply.getReplyStatus());
    }
    public static LsfileacRequestHolder getRequestObject() {
        QueryData qdt = LsfileacCases.getJavaObjectQueryData();
        QueryLimit qlt = LsfileacCases.getJavaObjectQueryLimit();
        LsfileacRequestHolder result = new LsfileacRequestHolder();
        result.setQueryData(qdt);
        result.setQueryLimit(qlt);
        return result;
    }
    public void testRoundTripCICSTS23DirectHttp() {
        return;
    }
    public void testRoundTripCICSTS23DirectMQ() {
        return;
    }
    public void testRoundTripCICSTS23DirectSocket() {
        return;
    }
    public void testRoundTripCICSTS23PooledHttp() {
        return;
    }
    public void testRoundTripCICSTS23PooledMQ() {
        return;
    }
    public void testRoundTripCICSTS23PooledSocket() {
        return;
    }
    public void testRoundTripCICSTS31DirectMQ() {
        return;
    }
    public void testRoundTripCICSTS31PooledMQ() {
        return;
    }
}

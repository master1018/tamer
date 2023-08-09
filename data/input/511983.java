public class NeighboringCellInfoTest extends AndroidTestCase {
    @SmallTest
    public void testConstructor() {
        int rssi = 31;
        NeighboringCellInfo nc;
        nc = new NeighboringCellInfo(rssi, "FFFFFFF", NETWORK_TYPE_EDGE);
        assertEquals(NETWORK_TYPE_EDGE, nc.getNetworkType());
        assertEquals(rssi, nc.getRssi());
        assertEquals(0xfff, nc.getLac());
        assertEquals(0xffff, nc.getCid());
        assertEquals(NeighboringCellInfo.UNKNOWN_CID, nc.getPsc());
        nc = new NeighboringCellInfo(rssi, "1FF", NETWORK_TYPE_UMTS);
        assertEquals(NETWORK_TYPE_UMTS, nc.getNetworkType());
        assertEquals(rssi, nc.getRssi());
        assertEquals(NeighboringCellInfo.UNKNOWN_CID, nc.getCid());
        assertEquals(NeighboringCellInfo.UNKNOWN_CID, nc.getLac());
        assertEquals(0x1ff, nc.getPsc());
        nc = new NeighboringCellInfo(rssi, "1FF", NETWORK_TYPE_UNKNOWN);
        assertEquals(NETWORK_TYPE_UNKNOWN, nc.getNetworkType());
        assertEquals(rssi, nc.getRssi());
        assertEquals(NeighboringCellInfo.UNKNOWN_CID, nc.getCid());
        assertEquals(NeighboringCellInfo.UNKNOWN_CID, nc.getLac());
        assertEquals(NeighboringCellInfo.UNKNOWN_CID, nc.getPsc());
    }
    @SmallTest
    public void testParcel() {
        int rssi = 20;
        NeighboringCellInfo nc = new NeighboringCellInfo(rssi, "12345678", NETWORK_TYPE_GPRS);
        assertEquals(NETWORK_TYPE_GPRS, nc.getNetworkType());
        assertEquals(rssi, nc.getRssi());
        assertEquals(0x1234, nc.getLac());
        assertEquals(0x5678, nc.getCid());
        assertEquals(NeighboringCellInfo.UNKNOWN_CID, nc.getPsc());
        Parcel p = Parcel.obtain();
        p.setDataPosition(0);
        nc.writeToParcel(p, 0);
        p.setDataPosition(0);
        NeighboringCellInfo nw = new NeighboringCellInfo(p);
        assertEquals(NETWORK_TYPE_GPRS, nw.getNetworkType());
        assertEquals(rssi, nw.getRssi());
        assertEquals(0x1234, nw.getLac());
        assertEquals(0x5678, nw.getCid());
        assertEquals(NeighboringCellInfo.UNKNOWN_CID, nw.getPsc());
     }
}

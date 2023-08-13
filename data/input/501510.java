public class NetStatService extends INetStatService.Stub {
    private final Context mContext;
    public NetStatService(Context context) {
        mContext = context;
    }
    public long getMobileTxPackets() {
        return TrafficStats.getMobileTxPackets();
    }
    public long getMobileRxPackets() {
        return TrafficStats.getMobileRxPackets();
    }
    public long getMobileTxBytes() {
        return TrafficStats.getMobileTxBytes();
    }
    public long getMobileRxBytes() {
        return TrafficStats.getMobileRxBytes();
    }
    public long getTotalTxPackets() {
        return TrafficStats.getTotalTxPackets();
    }
    public long getTotalRxPackets() {
        return TrafficStats.getTotalRxPackets();
    }
    public long getTotalTxBytes() {
        return TrafficStats.getTotalTxBytes();
    }
    public long getTotalRxBytes() {
        return TrafficStats.getTotalRxBytes();
    }
    @Override
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print("Elapsed: total=");
        pw.print(SystemClock.elapsedRealtime());
        pw.print("ms awake=");
        pw.print(SystemClock.uptimeMillis());
        pw.println("ms");
        pw.print("Mobile: Tx=");
        pw.print(getMobileTxBytes());
        pw.print("B/");
        pw.print(getMobileTxPackets());
        pw.print("Pkts Rx=");
        pw.print(getMobileRxBytes());
        pw.print("B/");
        pw.print(getMobileRxPackets());
        pw.println("Pkts");
        pw.print("Total: Tx=");
        pw.print(getTotalTxBytes());
        pw.print("B/");
        pw.print(getTotalTxPackets());
        pw.print("Pkts Rx=");
        pw.print(getTotalRxBytes());
        pw.print("B/");
        pw.print(getTotalRxPackets());
        pw.println("Pkts");
    }
}

public class SnapshotSize extends LogHandler {
    int lastSnapshotSize = -1;
    public static void main(String[] args) throws Exception {
        SnapshotSize handler = new SnapshotSize();
        ReliableLog log = new ReliableLog(".", handler);
        if (log.snapshotSize() != handler.lastSnapshotSize) {
            throw new Error();
        }
        String[] snapshots = { "some", "sample", "objects", "to", "snapshot" };
        for (int i = 0; i < snapshots.length; i++) {
            log.snapshot(snapshots[i]);
            if (log.snapshotSize() != handler.lastSnapshotSize) {
                throw new Error();
            }
        }
    }
    public Object initialSnapshot() {
        return "initial snapshot";
    }
    public void snapshot(OutputStream out, Object value) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(value);
        oout.close();
        byte[] buf = bout.toByteArray();
        out.write(buf);         
        lastSnapshotSize = buf.length;
    }
    public Object applyUpdate(Object update, Object state) {
        return state;
    }
}

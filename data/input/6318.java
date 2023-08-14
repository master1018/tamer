public class SunWritableRaster extends WritableRaster {
    private static DataStealer stealer;
    public static interface DataStealer {
        public byte[] getData(DataBufferByte dbb, int bank);
        public short[] getData(DataBufferUShort dbus, int bank);
        public int[] getData(DataBufferInt dbi, int bank);
        public StateTrackableDelegate getTrackable(DataBuffer db);
        public void setTrackable(DataBuffer db, StateTrackableDelegate trackable);
    }
    public static void setDataStealer(DataStealer ds) {
        if (stealer != null) {
            throw new InternalError("Attempt to set DataStealer twice");
        }
        stealer = ds;
    }
    public static byte[] stealData(DataBufferByte dbb, int bank) {
        return stealer.getData(dbb, bank);
    }
    public static short[] stealData(DataBufferUShort dbus, int bank) {
        return stealer.getData(dbus, bank);
    }
    public static int[] stealData(DataBufferInt dbi, int bank) {
        return stealer.getData(dbi, bank);
    }
    public static StateTrackableDelegate stealTrackable(DataBuffer db) {
        return stealer.getTrackable(db);
    }
    public static void setTrackable(DataBuffer db, StateTrackableDelegate trackable) {
        stealer.setTrackable(db, trackable);
    }
    public static void makeTrackable(DataBuffer db) {
        stealer.setTrackable(db, StateTrackableDelegate.createInstance(State.STABLE));
    }
    public static void markDirty(DataBuffer db) {
        stealer.getTrackable(db).markDirty();
    }
    public static void markDirty(WritableRaster wr) {
        if (wr instanceof SunWritableRaster) {
            ((SunWritableRaster) wr).markDirty();
        } else {
            markDirty(wr.getDataBuffer());
        }
    }
    public static void markDirty(Image img) {
        SurfaceData.getPrimarySurfaceData(img).markDirty();
    }
    private StateTrackableDelegate theTrackable;
    public SunWritableRaster(SampleModel sampleModel, Point origin) {
        super(sampleModel, origin);
        theTrackable = stealTrackable(dataBuffer);
    }
    public SunWritableRaster(SampleModel sampleModel,
                             DataBuffer dataBuffer,
                             Point origin)
    {
        super(sampleModel, dataBuffer, origin);
        theTrackable = stealTrackable(dataBuffer);
    }
    public SunWritableRaster(SampleModel sampleModel,
                             DataBuffer dataBuffer,
                             Rectangle aRegion,
                             Point sampleModelTranslate,
                             WritableRaster parent)
    {
        super(sampleModel, dataBuffer, aRegion, sampleModelTranslate, parent);
        theTrackable = stealTrackable(dataBuffer);
    }
    public final void markDirty() {
        theTrackable.markDirty();
    }
}

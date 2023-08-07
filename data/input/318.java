public class SpatialStaticStruct implements HLAfixedRecord {
    private WorldLocationStruct WorldLocation;
    private OMT13boolean IsFrozen;
    private OrientationStruct Orientation;
    public SpatialStaticStruct() throws RTIinternalError {
        WorldLocation = new WorldLocationStruct();
        IsFrozen = new OMT13boolean();
        Orientation = new OrientationStruct();
    }
    public void encode(ByteWrapper byteWrapper) {
        WorldLocation.encode(byteWrapper);
        IsFrozen.encode(byteWrapper);
        Orientation.encode(byteWrapper);
    }
    public void decode(ByteWrapper byteWrapper) throws DecoderException {
        WorldLocation.decode(byteWrapper);
        IsFrozen.decode(byteWrapper);
        Orientation.decode(byteWrapper);
    }
    public void decode(byte[] bytes) throws DecoderException {
        WorldLocation.decode(bytes);
        IsFrozen.decode(bytes);
        Orientation.decode(bytes);
    }
    public int getOctetBoundary() {
        return getEncodedLength() % 4;
    }
    public byte[] toByteArray() {
        int size = getEncodedLength();
        byte[] result = new byte[size];
        byte[] temp;
        int pos = 0;
        temp = WorldLocation.toByteArray();
        System.arraycopy(temp, 0, result, pos, WorldLocation.getEncodedLength());
        temp = IsFrozen.toByteArray();
        System.arraycopy(temp, 0, result, pos, IsFrozen.getEncodedLength());
        temp = Orientation.toByteArray();
        System.arraycopy(temp, 0, result, pos, Orientation.getEncodedLength());
        return result;
    }
    public void add(DataElement dataElement) {
    }
    public int size() {
        return getEncodedLength();
    }
    public DataElement get(int index) {
        return null;
    }
    public Iterator<DataElement> iterator() {
        return null;
    }
    public int getEncodedLength() {
        int size = 0;
        size += WorldLocation.getEncodedLength();
        size += IsFrozen.getEncodedLength();
        size += Orientation.getEncodedLength();
        return size;
    }
    public WorldLocationStruct getWorldLocation() {
        return this.WorldLocation;
    }
    public OMT13boolean getIsFrozen() {
        return this.IsFrozen;
    }
    public OrientationStruct getOrientation() {
        return this.Orientation;
    }
    public void setWorldLocation(WorldLocationStruct WorldLocation) {
        this.WorldLocation = WorldLocation;
    }
    public void setIsFrozen(OMT13boolean IsFrozen) {
        this.IsFrozen = IsFrozen;
    }
    public void setOrientation(OrientationStruct Orientation) {
        this.Orientation = Orientation;
    }
}

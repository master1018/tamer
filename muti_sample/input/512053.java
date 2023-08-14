public abstract class AwtImageBackdoorAccessor {
    static protected AwtImageBackdoorAccessor inst;
    public static AwtImageBackdoorAccessor getInstance(){
        new DataBufferInt(0);
        return inst;
    }
    public abstract Surface getImageSurface(Image image);
    public abstract boolean isGrayPallete(IndexColorModel icm);
    public abstract Object getData(DataBuffer db);
    public abstract int[] getDataInt(DataBuffer db);
    public abstract byte[] getDataByte(DataBuffer db);
    public abstract short[] getDataShort(DataBuffer db);
    public abstract short[] getDataUShort(DataBuffer db);
    public abstract double[] getDataDouble(DataBuffer db);
    public abstract float[] getDataFloat(DataBuffer db);
    public abstract void releaseData(DataBuffer db);
    public abstract void addDataBufferListener(DataBuffer db, DataBufferListener listener);
    public abstract void removeDataBufferListener(DataBuffer db);
    public abstract void validate(DataBuffer db);
}

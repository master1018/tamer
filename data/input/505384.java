class AwtImageBackdoorAccessorImpl extends AwtImageBackdoorAccessor {
    static void init() {
        inst = new AwtImageBackdoorAccessorImpl();
    }
    @Override
    public Surface getImageSurface(Image image) {
        if (image instanceof BufferedImage) {
            return ((BufferedImage)image).getImageSurface();
        } else if (image instanceof GLVolatileImage) {
            return ((GLVolatileImage)image).getImageSurface();
        }
        return null;
    }
    @Override
    public boolean isGrayPallete(IndexColorModel icm) {
        return icm.isGrayPallete();
    }
    @Override
    public Object getData(DataBuffer db) {
        if (db instanceof DataBufferByte) {
            return ((DataBufferByte)db).getData();
        } else if (db instanceof DataBufferUShort) {
            return ((DataBufferUShort)db).getData();
        } else if (db instanceof DataBufferShort) {
            return ((DataBufferShort)db).getData();
        } else if (db instanceof DataBufferInt) {
            return ((DataBufferInt)db).getData();
        } else if (db instanceof DataBufferFloat) {
            return ((DataBufferFloat)db).getData();
        } else if (db instanceof DataBufferDouble) {
            return ((DataBufferDouble)db).getData();
        } else {
            throw new IllegalArgumentException(Messages.getString("awt.235", 
                    db.getClass()));
        }
    }
    @Override
    public int[] getDataInt(DataBuffer db) {
        if (db instanceof DataBufferInt) {
            return ((DataBufferInt)db).getData();
        }
        return null;
    }
    @Override
    public byte[] getDataByte(DataBuffer db) {
        if (db instanceof DataBufferByte) {
            return ((DataBufferByte)db).getData();
        }
        return null;
    }
    @Override
    public short[] getDataShort(DataBuffer db) {
        if (db instanceof DataBufferShort) {
            return ((DataBufferShort)db).getData();
        }
        return null;
    }
    @Override
    public short[] getDataUShort(DataBuffer db) {
        if (db instanceof DataBufferUShort) {
            return ((DataBufferUShort)db).getData();
        }
        return null;
    }
    @Override
    public double[] getDataDouble(DataBuffer db) {
        if (db instanceof DataBufferDouble) {
            return ((DataBufferDouble)db).getData();
        }
        return null;
    }
    @Override
    public float[] getDataFloat(DataBuffer db) {
        if (db instanceof DataBufferFloat) {
            return ((DataBufferFloat)db).getData();
        }
        return null;
    }
    @Override
    public void addDataBufferListener(DataBuffer db, DataBufferListener listener) {
        db.addDataBufferListener(listener);
    }
    @Override
    public void removeDataBufferListener(DataBuffer db) {
        db.removeDataBufferListener();
    }
    @Override
    public void validate(DataBuffer db) {
        db.validate();
    }
    @Override
    public void releaseData(DataBuffer db) {
        db.releaseData();
    }
}

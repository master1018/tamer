public abstract class ImageDecoder {
    public static final int GENERIC_DECODER = 0;
    public static final int JPG_DECODER = 1;
    public static final int GIF_DECODER = 2;
    public static final int PNG_DECODER = 3;
    private static final int MAX_BYTES_IN_SIGNATURE = 8;
    protected List<ImageConsumer> consumers;
    protected InputStream inputStream;
    protected DecodingImageSource src;
    protected boolean terminated;
    static ImageDecoder createDecoder(DecodingImageSource src, InputStream is) {
        InputStream markable;
        if (!is.markSupported()) {
            markable = new BufferedInputStream(is, 8192);
        } else {
            markable = is;
        }
        try {
            markable.mark(MAX_BYTES_IN_SIGNATURE);
            byte[] signature = new byte[MAX_BYTES_IN_SIGNATURE];
            markable.read(signature, 0, MAX_BYTES_IN_SIGNATURE);
            markable.reset();
            if ((signature[0] & 0xFF) == 0xFF &&
                    (signature[1] & 0xFF) == 0xD8 &&
                    (signature[2] & 0xFF) == 0xFF) { 
                return loadDecoder(PNG_DECODER, src, is);
            } else if ((signature[0] & 0xFF) == 0x47 && 
                    (signature[1] & 0xFF) == 0x49 && 
                    (signature[2] & 0xFF) == 0x46) { 
                return loadDecoder(GIF_DECODER, src, is);
            } else if ((signature[0] & 0xFF) == 137 && 
                    (signature[1] & 0xFF) == 80 &&
                    (signature[2] & 0xFF) == 78 &&
                    (signature[3] & 0xFF) == 71 &&
                    (signature[4] & 0xFF) == 13 &&
                    (signature[5] & 0xFF) == 10 &&
                    (signature[6] & 0xFF) == 26 &&
                    (signature[7] & 0xFF) == 10) {
                return loadDecoder(JPG_DECODER, src, is);
            }
            return loadDecoder(GENERIC_DECODER, src, is);
        } catch (IOException e) { 
        }
        return null;
    }
    private static ImageDecoder loadDecoder(int type, DecodingImageSource src, 
            InputStream is) {
        return new AndroidImageDecoder(src, is);
    }
    protected ImageDecoder(DecodingImageSource _src, InputStream is) {
        src = _src;
        consumers = src.consumers;
        inputStream = is;
    }
    public abstract void decodeImage() throws IOException;
    public synchronized void closeStream() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }
    }
    public void terminate() {
        src.lockDecoder(this);
        closeStream();
        AccessController.doPrivileged(
                new PrivilegedAction<Void>() {
                    public Void run() {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
        );
        terminated = true;
    }
    protected void setDimensions(int w, int h) {
        if (terminated) {
            return;
        }
        for (ImageConsumer ic : consumers) {
            ic.setDimensions(w, h);
        }
    }
    protected void setProperties(Hashtable<?, ?> props) {
        if (terminated) {
            return;
        }
        for (ImageConsumer ic : consumers) {
            ic.setProperties(props);
        }
    }
    protected void setColorModel(ColorModel cm) {
        if (terminated) {
            return;
        }
        for (ImageConsumer ic : consumers) {
            ic.setColorModel(cm);
        }
    }
    protected void setHints(int hints) {
        if (terminated) {
            return;
        }
        for (ImageConsumer ic : consumers) {
            ic.setHints(hints);
        }
    }
    protected void setPixels(
            int x, int y,
            int w, int h,
            ColorModel model,
            byte pix[],
            int off, int scansize
            ) {
        if (terminated) {
            return;
        }
        src.lockDecoder(this);
        for (ImageConsumer ic : consumers) {
            ic.setPixels(x, y, w, h, model, pix, off, scansize);
        }
    }
    protected void setPixels(
            int x, int y,
            int w, int h,
            ColorModel model,
            int pix[],
            int off, int scansize
            ) {
        if (terminated) {
            return;
        }
        src.lockDecoder(this);
        for (ImageConsumer ic : consumers) {
            ic.setPixels(x, y, w, h, model, pix, off, scansize);
        }
    }
    protected void imageComplete(int status) {
        if (terminated) {
            return;
        }
        src.lockDecoder(this);
        ImageConsumer ic = null;
        for (Iterator<ImageConsumer> i = consumers.iterator(); i.hasNext();) {
            try {
                ic = i.next();
            } catch (ConcurrentModificationException e) {
                i = consumers.iterator();
                continue;
            }
            ic.imageComplete(status);
        }
    }
}

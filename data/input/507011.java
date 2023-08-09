public class BitmapFactory {
    public static class Options {
        public Options() {
            inDither = true;
            inScaled = true;
        }
        public boolean inJustDecodeBounds;
        public int inSampleSize;
        public Bitmap.Config inPreferredConfig;
        public boolean inDither;
        public int inDensity;
        public int inTargetDensity;
        public int inScreenDensity;
        public boolean inScaled;
        public boolean inPurgeable;
        public boolean inInputShareable;
        public boolean inNativeAlloc;
        public int outWidth;
        public int outHeight;
        public String outMimeType;
        public byte[] inTempStorage;
        private native void requestCancel();
        public boolean mCancel;
        public void requestCancelDecode() {
            mCancel = true;
            requestCancel();
        }
    }
    public static Bitmap decodeFile(String pathName, Options opts) {
        Bitmap bm = null;
        InputStream stream = null;
        try {
            stream = new FileInputStream(pathName);
            bm = decodeStream(stream, null, opts);
        } catch (Exception e) {
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }
        return bm;
    }
    public static Bitmap decodeFile(String pathName) {
        return decodeFile(pathName, null);
    }
    public static Bitmap decodeResourceStream(Resources res, TypedValue value,
            InputStream is, Rect pad, Options opts) {
        if (opts == null) {
            opts = new Options();
        }
        if (opts.inDensity == 0 && value != null) {
            final int density = value.density;
            if (density == TypedValue.DENSITY_DEFAULT) {
                opts.inDensity = DisplayMetrics.DENSITY_DEFAULT;
            } else if (density != TypedValue.DENSITY_NONE) {
                opts.inDensity = density;
            }
        }
        if (opts.inTargetDensity == 0 && res != null) {
            opts.inTargetDensity = res.getDisplayMetrics().densityDpi;
        }
        return decodeStream(is, pad, opts);
    }
    public static Bitmap decodeResource(Resources res, int id, Options opts) {
        Bitmap bm = null;
        InputStream is = null; 
        try {
            final TypedValue value = new TypedValue();
            is = res.openRawResource(id, value);
            bm = decodeResourceStream(res, value, is, null, opts);
        } catch (Exception e) {
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
            }
        }
        return bm;
    }
    public static Bitmap decodeResource(Resources res, int id) {
        return decodeResource(res, id, null);
    }
    public static Bitmap decodeByteArray(byte[] data, int offset, int length, Options opts) {
        if ((offset | length) < 0 || data.length < offset + length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return nativeDecodeByteArray(data, offset, length, opts);
    }
    public static Bitmap decodeByteArray(byte[] data, int offset, int length) {
        return decodeByteArray(data, offset, length, null);
    }
    public static Bitmap decodeStream(InputStream is, Rect outPadding, Options opts) {
        if (is == null) {
            return null;
        }
        if (!is.markSupported()) {
            is = new BufferedInputStream(is, 16 * 1024);
        }
        is.mark(1024);
        Bitmap  bm;
        if (is instanceof AssetManager.AssetInputStream) {
            bm = nativeDecodeAsset(((AssetManager.AssetInputStream) is).getAssetInt(),
                    outPadding, opts);
        } else {
            byte [] tempStorage = null;
            if (opts != null)
                tempStorage = opts.inTempStorage;
            if (tempStorage == null)
                tempStorage = new byte[16 * 1024];
            bm = nativeDecodeStream(is, tempStorage, outPadding, opts);
        }
        return finishDecode(bm, outPadding, opts);
    }
    private static Bitmap finishDecode(Bitmap bm, Rect outPadding, Options opts) {
        if (bm == null || opts == null) {
            return bm;
        }
        final int density = opts.inDensity;
        if (density == 0) {
            return bm;
        }
        bm.setDensity(density);
        final int targetDensity = opts.inTargetDensity;
        if (targetDensity == 0 || density == targetDensity
                || density == opts.inScreenDensity) {
            return bm;
        }
        byte[] np = bm.getNinePatchChunk();
        final boolean isNinePatch = np != null && NinePatch.isNinePatchChunk(np);
        if (opts.inScaled || isNinePatch) {
            float scale = targetDensity / (float)density;
            final Bitmap oldBitmap = bm;
            bm = Bitmap.createScaledBitmap(oldBitmap, (int) (bm.getWidth() * scale + 0.5f),
                    (int) (bm.getHeight() * scale + 0.5f), true);
            oldBitmap.recycle();
            if (isNinePatch) {
                np = nativeScaleNinePatch(np, scale, outPadding);
                bm.setNinePatchChunk(np);
            }
            bm.setDensity(targetDensity);
        }
        return bm;
    }
    public static Bitmap decodeStream(InputStream is) {
        return decodeStream(is, null, null);
    }
    public static Bitmap decodeFileDescriptor(FileDescriptor fd, Rect outPadding, Options opts) {
        try {
            if (MemoryFile.isMemoryFile(fd)) {
                int mappedlength = MemoryFile.getSize(fd);
                MemoryFile file = new MemoryFile(fd, mappedlength, "r");
                InputStream is = file.getInputStream();
                Bitmap bm = decodeStream(is, outPadding, opts);
                return finishDecode(bm, outPadding, opts);
            }
        } catch (IOException ex) {
            return null;
        }
        Bitmap bm = nativeDecodeFileDescriptor(fd, outPadding, opts);
        return finishDecode(bm, outPadding, opts);
    }
    public static Bitmap decodeFileDescriptor(FileDescriptor fd) {
        return decodeFileDescriptor(fd, null, null);
    }
    public static void setDefaultConfig(Bitmap.Config config) {
        if (config == null) {
            config = Bitmap.Config.RGB_565;
        }
        nativeSetDefaultConfig(config.nativeInt);
    }
    private static native void nativeSetDefaultConfig(int nativeConfig);
    private static native Bitmap nativeDecodeStream(InputStream is, byte[] storage,
            Rect padding, Options opts);
    private static native Bitmap nativeDecodeFileDescriptor(FileDescriptor fd,
            Rect padding, Options opts);
    private static native Bitmap nativeDecodeAsset(int asset, Rect padding, Options opts);
    private static native Bitmap nativeDecodeByteArray(byte[] data, int offset,
            int length, Options opts);
    private static native byte[] nativeScaleNinePatch(byte[] chunk, float scale, Rect pad);
}

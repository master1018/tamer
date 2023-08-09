public final class EmojiFactory {
    private int sCacheSize = 100;
    private class CustomLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
        public CustomLinkedHashMap() {
            super(16, 0.75f, true);
        }
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > sCacheSize;
        }
    }
    private int mNativeEmojiFactory;
    private String mName;
    private Map<Integer, WeakReference<Bitmap>> mCache;
    private EmojiFactory(int nativeEmojiFactory, String name) {
        mNativeEmojiFactory = nativeEmojiFactory;
        mName = name;
        mCache = new CustomLinkedHashMap<Integer, WeakReference<Bitmap>>();
    }
    @Override
    protected void finalize() throws Throwable {
        try {
            nativeDestructor(mNativeEmojiFactory);
        } finally {
            super.finalize();
        }
    }
    public String name() {
        return mName;
    }
    public synchronized Bitmap getBitmapFromAndroidPua(int pua) {
        WeakReference<Bitmap> cache = mCache.get(pua);
        if (cache == null) {
            Bitmap ret = nativeGetBitmapFromAndroidPua(mNativeEmojiFactory, pua);
            if (ret != null) {
               mCache.put(pua, new WeakReference<Bitmap>(ret));
            }
            return ret;
        } else {
            Bitmap tmp = cache.get();
            if (tmp == null) {
                Bitmap ret = nativeGetBitmapFromAndroidPua(mNativeEmojiFactory, pua);
                mCache.put(pua, new WeakReference<Bitmap>(ret));
                return ret;
            } else {
                return tmp;
            }
        }
    }
    public synchronized Bitmap getBitmapFromVendorSpecificSjis(char sjis) {
        return getBitmapFromAndroidPua(getAndroidPuaFromVendorSpecificSjis(sjis));
    }
    public synchronized Bitmap getBitmapFromVendorSpecificPua(int vsp) {
        return getBitmapFromAndroidPua(getAndroidPuaFromVendorSpecificPua(vsp));
    }
    public int getAndroidPuaFromVendorSpecificSjis(char sjis) {
        return nativeGetAndroidPuaFromVendorSpecificSjis(mNativeEmojiFactory, sjis);
    }
    public int getVendorSpecificSjisFromAndroidPua(int pua) {
        return nativeGetVendorSpecificSjisFromAndroidPua(mNativeEmojiFactory, pua);
    }
    public int getAndroidPuaFromVendorSpecificPua(int vsp) {
        return nativeGetAndroidPuaFromVendorSpecificPua(mNativeEmojiFactory, vsp);
    }
    public String getAndroidPuaFromVendorSpecificPua(String vspString) {
        if (vspString == null) {
            return null;
        }
        int minVsp = nativeGetMinimumVendorSpecificPua(mNativeEmojiFactory);
        int maxVsp = nativeGetMaximumVendorSpecificPua(mNativeEmojiFactory);
        int len = vspString.length();
        int[] codePoints = new int[vspString.codePointCount(0, len)];
        int new_len = 0;
        for (int i = 0; i < len; i = vspString.offsetByCodePoints(i, 1), new_len++) {
            int codePoint = vspString.codePointAt(i);
            if (minVsp <= codePoint && codePoint <= maxVsp) {
                int newCodePoint = getAndroidPuaFromVendorSpecificPua(codePoint);
                if (newCodePoint > 0) {
                    codePoints[new_len] = newCodePoint;
                    continue;
                }
            }
            codePoints[new_len] = codePoint;
        }
        return new String(codePoints, 0, new_len);
    }
    public int getVendorSpecificPuaFromAndroidPua(int pua) {
        return nativeGetVendorSpecificPuaFromAndroidPua(mNativeEmojiFactory, pua);
    }
    public String getVendorSpecificPuaFromAndroidPua(String puaString) {
        if (puaString == null) {
            return null;
        }
        int minVsp = nativeGetMinimumAndroidPua(mNativeEmojiFactory);
        int maxVsp = nativeGetMaximumAndroidPua(mNativeEmojiFactory);
        int len = puaString.length();
        int[] codePoints = new int[puaString.codePointCount(0, len)];
        int new_len = 0;
        for (int i = 0; i < len; i = puaString.offsetByCodePoints(i, 1), new_len++) {
            int codePoint = puaString.codePointAt(i);
            if (minVsp <= codePoint && codePoint <= maxVsp) {
                int newCodePoint = getVendorSpecificPuaFromAndroidPua(codePoint);
                if (newCodePoint > 0) {
                    codePoints[new_len] = newCodePoint;
                    continue;
                }
            }
            codePoints[new_len] = codePoint;
        }
        return new String(codePoints, 0, new_len);
    }
    public static native EmojiFactory newInstance(String class_name);
    public static native EmojiFactory newAvailableInstance();
    public int getMinimumAndroidPua() {
        return nativeGetMinimumAndroidPua(mNativeEmojiFactory);
    }
    public int getMaximumAndroidPua() {
        return nativeGetMaximumAndroidPua(mNativeEmojiFactory);
    }
    private native void nativeDestructor(int factory);
    private native Bitmap nativeGetBitmapFromAndroidPua(int nativeEmojiFactory, int AndroidPua);
    private native int nativeGetAndroidPuaFromVendorSpecificSjis(int nativeEmojiFactory,
            char sjis);
    private native int nativeGetVendorSpecificSjisFromAndroidPua(int nativeEmojiFactory,
            int pua);
    private native int nativeGetAndroidPuaFromVendorSpecificPua(int nativeEmojiFactory,
            int vsp);
    private native int nativeGetVendorSpecificPuaFromAndroidPua(int nativeEmojiFactory,
            int pua);
    private native int nativeGetMaximumVendorSpecificPua(int nativeEmojiFactory);
    private native int nativeGetMinimumVendorSpecificPua(int nativeEmojiFactory);
    private native int nativeGetMaximumAndroidPua(int nativeEmojiFactory);
    private native int nativeGetMinimumAndroidPua(int nativeEmojiFactory);
}

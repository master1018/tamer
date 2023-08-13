abstract class Effect {
    enum EffectType {
        UNDER, BLENDED, OVER
    }
    abstract EffectType getEffectType();
    abstract float getOpacity();
    abstract BufferedImage applyEffect(BufferedImage src, BufferedImage dst, int w, int h);
    protected static ArrayCache getArrayCache() {
        ArrayCache cache = (ArrayCache)AppContext.getAppContext().get(ArrayCache.class);
        if (cache == null){
            cache = new ArrayCache();
            AppContext.getAppContext().put(ArrayCache.class,cache);
        }
        return cache;
    }
    protected static class ArrayCache {
        private SoftReference<int[]> tmpIntArray = null;
        private SoftReference<byte[]> tmpByteArray1 = null;
        private SoftReference<byte[]> tmpByteArray2 = null;
        private SoftReference<byte[]> tmpByteArray3 = null;
        protected int[] getTmpIntArray(int size) {
            int[] tmp;
            if (tmpIntArray == null || (tmp = tmpIntArray.get()) == null || tmp.length < size) {
                tmp = new int[size];
                tmpIntArray = new SoftReference<int[]>(tmp);
            }
            return tmp;
        }
        protected byte[] getTmpByteArray1(int size) {
            byte[] tmp;
            if (tmpByteArray1 == null || (tmp = tmpByteArray1.get()) == null || tmp.length < size) {
                tmp = new byte[size];
                tmpByteArray1 = new SoftReference<byte[]>(tmp);
            }
            return tmp;
        }
        protected byte[] getTmpByteArray2(int size) {
            byte[] tmp;
            if (tmpByteArray2 == null || (tmp = tmpByteArray2.get()) == null || tmp.length < size) {
                tmp = new byte[size];
                tmpByteArray2 = new SoftReference<byte[]>(tmp);
            }
            return tmp;
        }
        protected byte[] getTmpByteArray3(int size) {
            byte[] tmp;
            if (tmpByteArray3 == null || (tmp = tmpByteArray3.get()) == null || tmp.length < size) {
                tmp = new byte[size];
                tmpByteArray3 = new SoftReference<byte[]>(tmp);
            }
            return tmp;
        }
    }
}

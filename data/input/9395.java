class CodeSetCache
{
    private ThreadLocal converterCaches = new ThreadLocal() {
        public java.lang.Object initialValue() {
            return new Map[] { new WeakHashMap(), new WeakHashMap() };
        }
    };
    private static final int BTC_CACHE_MAP = 0;
    private static final int CTB_CACHE_MAP = 1;
    CharsetDecoder getByteToCharConverter(Object key) {
        Map btcMap = ((Map[])converterCaches.get())[BTC_CACHE_MAP];
        return (CharsetDecoder)btcMap.get(key);
    }
    CharsetEncoder getCharToByteConverter(Object key) {
        Map ctbMap = ((Map[])converterCaches.get())[CTB_CACHE_MAP];
        return (CharsetEncoder)ctbMap.get(key);
    }
    CharsetDecoder setConverter(Object key, CharsetDecoder converter) {
        Map btcMap = ((Map[])converterCaches.get())[BTC_CACHE_MAP];
        btcMap.put(key, converter);
        return converter;
    }
    CharsetEncoder setConverter(Object key, CharsetEncoder converter) {
        Map ctbMap = ((Map[])converterCaches.get())[CTB_CACHE_MAP];
        ctbMap.put(key, converter);
        return converter;
    }
}

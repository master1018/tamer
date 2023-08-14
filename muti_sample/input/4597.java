public class CollatorUtilities {
    public static int toLegacyMode(NormalizerBase.Mode mode) {
        int legacyMode = legacyModeMap.length;
        while (legacyMode > 0) {
            --legacyMode;
            if (legacyModeMap[legacyMode] == mode) {
                break;
            }
        }
        return legacyMode;
    }
    public static NormalizerBase.Mode toNormalizerMode(int mode) {
        NormalizerBase.Mode normalizerMode;
        try {
            normalizerMode = legacyModeMap[mode];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            normalizerMode = NormalizerBase.NONE;
        }
        return normalizerMode;
    }
    static NormalizerBase.Mode[] legacyModeMap = {
        NormalizerBase.NONE,   
        NormalizerBase.NFD,    
        NormalizerBase.NFKD,   
    };
}

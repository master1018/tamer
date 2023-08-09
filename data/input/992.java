public final class ParameterCache {
    private ParameterCache() {
    }
    private final static Map<Integer,DSAParameterSpec> dsaCache;
    private final static Map<Integer,DHParameterSpec> dhCache;
    public static DSAParameterSpec getCachedDSAParameterSpec(int keyLength) {
        return dsaCache.get(Integer.valueOf(keyLength));
    }
    public static DHParameterSpec getCachedDHParameterSpec(int keyLength) {
        return dhCache.get(Integer.valueOf(keyLength));
    }
    public static DSAParameterSpec getDSAParameterSpec(int keyLength,
            SecureRandom random)
            throws NoSuchAlgorithmException, InvalidParameterSpecException {
        DSAParameterSpec spec = getCachedDSAParameterSpec(keyLength);
        if (spec != null) {
            return spec;
        }
        spec = getNewDSAParameterSpec(keyLength, random);
        dsaCache.put(Integer.valueOf(keyLength), spec);
        return spec;
    }
    public static DHParameterSpec getDHParameterSpec(int keyLength,
            SecureRandom random)
            throws NoSuchAlgorithmException, InvalidParameterSpecException {
        DHParameterSpec spec = getCachedDHParameterSpec(keyLength);
        if (spec != null) {
            return spec;
        }
        AlgorithmParameterGenerator gen =
                AlgorithmParameterGenerator.getInstance("DH");
        gen.init(keyLength, random);
        AlgorithmParameters params = gen.generateParameters();
        spec = params.getParameterSpec(DHParameterSpec.class);
        dhCache.put(Integer.valueOf(keyLength), spec);
        return spec;
    }
    public static DSAParameterSpec getNewDSAParameterSpec(int keyLength,
            SecureRandom random)
            throws NoSuchAlgorithmException, InvalidParameterSpecException {
        AlgorithmParameterGenerator gen =
                AlgorithmParameterGenerator.getInstance("DSA");
        gen.init(keyLength, random);
        AlgorithmParameters params = gen.generateParameters();
        DSAParameterSpec spec = params.getParameterSpec(DSAParameterSpec.class);
        return spec;
    }
    static {
        dhCache = Collections.synchronizedMap
                        (new HashMap<Integer,DHParameterSpec>());
        dsaCache = Collections.synchronizedMap
                        (new HashMap<Integer,DSAParameterSpec>());
        BigInteger p512 =
            new BigInteger("fca682ce8e12caba26efccf7110e526db078b05edecb" +
                           "cd1eb4a208f3ae1617ae01f35b91a47e6df63413c5e1" +
                           "2ed0899bcd132acd50d99151bdc43ee737592e17", 16);
        BigInteger q512 =
            new BigInteger("962eddcc369cba8ebb260ee6b6a126d9346e38c5", 16);
        BigInteger g512 =
            new BigInteger("678471b27a9cf44ee91a49c5147db1a9aaf244f05a43" +
                           "4d6486931d2d14271b9e35030b71fd73da179069b32e" +
                           "2935630e1c2062354d0da20a6c416e50be794ca4", 16);
        BigInteger p768 =
            new BigInteger("e9e642599d355f37c97ffd3567120b8e25c9cd43e" +
                           "927b3a9670fbec5d890141922d2c3b3ad24800937" +
                           "99869d1e846aab49fab0ad26d2ce6a22219d470bc" +
                           "e7d777d4a21fbe9c270b57f607002f3cef8393694" +
                           "cf45ee3688c11a8c56ab127a3daf", 16);
        BigInteger q768 =
            new BigInteger("9cdbd84c9f1ac2f38d0f80f42ab952e7338bf511",
                           16);
        BigInteger g768 =
            new BigInteger("30470ad5a005fb14ce2d9dcd87e38bc7d1b1c5fac" +
                           "baecbe95f190aa7a31d23c4dbbcbe06174544401a" +
                           "5b2c020965d8c2bd2171d3668445771f74ba084d2" +
                           "029d83c1c158547f3a9f1a2715be23d51ae4d3e5a" +
                           "1f6a7064f316933a346d3f529252", 16);
        BigInteger p1024 =
            new BigInteger("fd7f53811d75122952df4a9c2eece4e7f611b7523c" +
                           "ef4400c31e3f80b6512669455d402251fb593d8d58" +
                           "fabfc5f5ba30f6cb9b556cd7813b801d346ff26660" +
                           "b76b9950a5a49f9fe8047b1022c24fbba9d7feb7c6" +
                           "1bf83b57e7c6a8a6150f04fb83f6d3c51ec3023554" +
                           "135a169132f675f3ae2b61d72aeff22203199dd148" +
                           "01c7", 16);
        BigInteger q1024 =
            new BigInteger("9760508f15230bccb292b982a2eb840bf0581cf5",
                           16);
        BigInteger g1024 =
            new BigInteger("f7e1a085d69b3ddecbbcab5c36b857b97994afbbfa" +
                           "3aea82f9574c0b3d0782675159578ebad4594fe671" +
                           "07108180b449167123e84c281613b7cf09328cc8a6" +
                           "e13c167a8b547c8d28e0a3ae1e2bb3a675916ea37f" +
                           "0bfa213562f1fb627a01243bcca4f1bea8519089a8" +
                           "83dfe15ae59f06928b665e807b552564014c3bfecf" +
                           "492a", 16);
        dsaCache.put(Integer.valueOf(512),
                                new DSAParameterSpec(p512, q512, g512));
        dsaCache.put(Integer.valueOf(768),
                                new DSAParameterSpec(p768, q768, g768));
        dsaCache.put(Integer.valueOf(1024),
                                new DSAParameterSpec(p1024, q1024, g1024));
        dhCache.put(Integer.valueOf(512), new DHParameterSpec(p512, g512));
        dhCache.put(Integer.valueOf(768), new DHParameterSpec(p768, g768));
        dhCache.put(Integer.valueOf(1024), new DHParameterSpec(p1024, g1024));
    }
}

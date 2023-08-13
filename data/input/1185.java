public class ReadWriteProfileTest implements Runnable {
    final static int TAG_COUNT_OFFSET = 32;
    final static int TAG_ELEM_OFFSET = 33;
    static byte[][] profiles;
    static int [][] tagSigs;
    static Hashtable<Integer,byte[]> [] tags;
    static int [] cspaces = {ColorSpace.CS_sRGB, ColorSpace.CS_PYCC,
                             ColorSpace.CS_LINEAR_RGB, ColorSpace.CS_CIEXYZ,
                             ColorSpace.CS_GRAY};
    static String [] csNames = {"sRGB", "PYCC", "LINEAR_RGB", "CIEXYZ", "GRAY"};
    static void getProfileTags(byte [] data, Hashtable tags) {
        ByteBuffer byteBuf = ByteBuffer.wrap(data);
        IntBuffer intBuf = byteBuf.asIntBuffer();
        int tagCount = intBuf.get(TAG_COUNT_OFFSET);
        intBuf.position(TAG_ELEM_OFFSET);
        for (int i = 0; i < tagCount; i++) {
            int tagSig = intBuf.get();
            int tagDataOff = intBuf.get();
            int tagSize = intBuf.get();
            byte [] tagData = new byte[tagSize];
            byteBuf.position(tagDataOff);
            byteBuf.get(tagData);
            tags.put(tagSig, tagData);
        }
    }
    static {
        profiles = new byte[cspaces.length][];
        tags = new Hashtable[cspaces.length];
        for (int i = 0; i < cspaces.length; i++) {
            ICC_Profile pf = ICC_Profile.getInstance(cspaces[i]);
            profiles[i] = pf.getData();
            tags[i] = new Hashtable();
            getProfileTags(profiles[i], tags[i]);
        }
    }
    public void run() {
        for (int i = 0; i < cspaces.length; i++) {
            ICC_Profile pf = ICC_Profile.getInstance(cspaces[i]);
            byte [] data = pf.getData();
            pf = ICC_Profile.getInstance(data);
            if (!Arrays.equals(data, profiles[i])) {
                System.err.println("Incorrect result of getData() " + "with " +
                                   csNames[i] + " profile");
                throw new RuntimeException("Incorrect result of getData()");
            }
            for (int tagSig : tags[i].keySet()) {
                byte [] tagData = pf.getData(tagSig);
                byte [] empty = new byte[tagData.length];
                boolean emptyDataRejected = false;
                try {
                    pf.setData(tagSig, empty);
                } catch (IllegalArgumentException e) {
                    emptyDataRejected = true;
                }
                if (!emptyDataRejected) {
                    throw new
                        RuntimeException("Test failed: empty tag data was not rejected.");
                }
                pf.setData(tagSig, tagData);
                byte [] tagData1 = pf.getData(tagSig);
                if (!Arrays.equals(tagData1, tags[i].get(tagSig)))
                {
                    System.err.println("Incorrect result of getData(int) with" +
                                       " tag " +
                                       Integer.toHexString(tagSig) +
                                       " of " + csNames[i] + " profile");
                    throw new RuntimeException("Incorrect result of " +
                                               "getData(int)");
                }
            }
        }
    }
    public static void main(String [] args) {
        ReadWriteProfileTest test = new ReadWriteProfileTest();
        test.run();
    }
}

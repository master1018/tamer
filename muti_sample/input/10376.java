public class ReadProfileTest implements Runnable {
    final static int TAG_COUNT_OFFSET = 32;
    final static int TAG_ELEM_OFFSET = 33;
    static byte[][] profiles;
    static int [][] tagSigs;
    static Hashtable [] tags;
    boolean status;
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
    public ReadProfileTest() {
        status = true;
    }
    public void run() {
        for (int i = 0; i < cspaces.length; i++) {
            ICC_Profile pf = ICC_Profile.getInstance(cspaces[i]);
            byte [] data = pf.getData();
            if (!Arrays.equals(data, profiles[i])) {
                status = false;
                System.err.println("Incorrect result of getData() " + "with " +
                                   csNames[i] + " profile");
                throw new RuntimeException("Incorrect result of getData()");
            }
            Iterator<Integer> iter = tags[i].keySet().iterator();
            while(iter.hasNext()) {
                int tagSig = iter.next();
                byte [] tagData = pf.getData(tagSig);
                if (!Arrays.equals(tagData,
                                   (byte[]) tags[i].get(tagSig)))
                {
                    status = false;
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
    public boolean getStatus() {
        return status;
    }
    public static void main(String [] args) {
        ReadProfileTest test = new ReadProfileTest();
        test.run();
    }
}

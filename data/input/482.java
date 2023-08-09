public class ClearHandleTable {
    public static void main(String[] args) throws Exception {
        final int nreps = 100;
        ObjectOutputStream oout =
            new ObjectOutputStream(new ByteArrayOutputStream());
        WeakReference[] refs = new WeakReference[nreps];
        for (int i = 0; i < nreps; i++) {
            String str = new String("blargh");
            oout.writeObject(str);
            refs[i] = new WeakReference(str);
        }
        oout.reset();
        exhaustMemory();
        for (int i = 0; i < nreps; i++) {
            if (refs[i].get() != null) {
                throw new Error("failed to garbage collect object " + i);
            }
        }
    }
    static void exhaustMemory() {
        ArrayList blob = new ArrayList();
        try {
            for (;;) {
                blob.add(new int[0xFFFF]);
            }
        } catch (OutOfMemoryError e) {
        }
    }
}

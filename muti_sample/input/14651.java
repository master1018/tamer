public class WriteReplace {
    static class ReplaceMe implements Serializable {
        private Object obj;
        private boolean writeReplaceCalled = false;
        public ReplaceMe(Object obj) {
            this.obj = obj;
        }
        private Object writeReplace() throws ObjectStreamException {
            if (writeReplaceCalled) {
                throw new Error("multiple calls to writeReplace");
            }
            writeReplaceCalled = true;
            return obj;
        }
    }
    public static void main(String[] args) throws Exception {
        final int nobjs = 10;
        final int nrounds = 10;
        Object common = "foo";
        ReplaceMe[] objs = new ReplaceMe[nobjs];
        for (int i = 0; i < nobjs; i++) {
            objs[i] = new ReplaceMe(common);
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        for (int i = 0; i < nrounds; i++) {
            for (int j = 0; j < nobjs; j++) {
                oout.writeObject(objs[j]);
            }
        }
        oout.flush();
        ByteArrayInputStream bin =
            new ByteArrayInputStream(bout.toByteArray());
        ObjectInputStream oin = new ObjectInputStream(bin);
        common = null;
        for (int i = 0; i < nrounds; i++) {
            for (int j = 0; j < nobjs; j++) {
                if (common == null) {
                    common = oin.readObject();
                } else {
                    if (oin.readObject() != common) {
                        throw new Error("incorrect replacement object");
                    }
                }
            }
        }
    }
}

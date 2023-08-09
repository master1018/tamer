public class ExceptionDetail {
    public static void main(String[] args) throws Exception {
        String className = ExceptionDetail.class.getName();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        try {
            oout.writeObject(new ExceptionDetail());
            throw new Error();
        } catch (NotSerializableException ex) {
            if (ex.getMessage().indexOf(className) == -1) {
                throw new Error();
            }
        }
        oout.close();
        ObjectInputStream oin = new ObjectInputStream(
            new ByteArrayInputStream(bout.toByteArray()));
        try {
            oin.readObject();
            throw new Error();
        } catch (WriteAbortedException ex) {
            if (ex.detail.getMessage().indexOf(className) == -1) {
                throw new Error();
            }
        }
        oin.close();
    }
}

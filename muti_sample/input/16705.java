public class UserRWObjError implements java.io.Serializable {
    public static void main(String[] args) throws Exception {
        try {
            UserRWObjError obj = new UserRWObjError();
            ObjectOutputStream out =
                new ObjectOutputStream(new ByteArrayOutputStream());
            out.writeObject(obj);
        } catch (ClassCastException e) {
            throw e;
        } catch (OutOfMemoryError e) {
            System.err.println("Test PASSED:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An Unexpected exception occurred:");
            throw e;
        }
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new OutOfMemoryError();
    }
}

public class NotAvailable {
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream baos;
        ObjectOutput out;
        try {
            baos = new ByteArrayOutputStream();
            out = new ObjectOutputStream(baos);
            out.writeObject(new Class1(22,33));
            out.writeObject(new Class1(22,33));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        ObjectInputStream in = null;
        try {
            ByteArrayInputStream bois =
                new ByteArrayInputStream(baos.toByteArray()) {
                public int available() {
                      throw new Error("available() is not implemented");
                  }
                };
            in = new ObjectInputStream(bois);
            Class1 cc1 = (Class1) in.readObject();
            Class1 cc2 = (Class1) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }
    }
}
class Class1 implements Serializable {
    int a, b;
    public Class1(int aa, int bb) {
        a = aa;
        b = bb;
    }
}

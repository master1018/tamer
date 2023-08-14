public class RMISerializeTest {
    public static void main(String[] args) throws Exception {
        System.out.println(">>> Tests to serialize RMIConnector.");
        JMXServiceURL url = new JMXServiceURL("rmi", null, 1111);
        RMIConnector rc1 = new RMIConnector(url, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(rc1);
        byte[] bs = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bs);
        ObjectInputStream ois = new ObjectInputStream(bais);
        RMIConnector rc2 = (RMIConnector)ois.readObject();
        try {
            rc2.close();
        } catch (NullPointerException npe) {
            System.out.println(">>> Test failed.");
            npe.printStackTrace(System.out);
            System.exit(1);
        } catch (Exception e) {
        }
    }
}

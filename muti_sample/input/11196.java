public class Deserialize {
    public static void main(String[] args) throws Exception {
        System.out.println("Serialize to file foo ...");
        SSLEngineResult.Status obj = SSLEngineResult.Status.OK;
        File file = new File("deserial-test-file");
        file.deleteOnExit();
        ObjectOutputStream oos = new ObjectOutputStream(
            new FileOutputStream(file));
        oos.writeObject(obj);
        oos.flush();
        oos.close();
        System.out.println("Done");
        System.out.println("Deserialize from file foo...");
        ObjectInputStream ois = new ObjectInputStream(
            new FileInputStream(file));
        obj = (SSLEngineResult.Status) ois.readObject();
        ois.close();
        System.out.println("Passed.");
    }
}

public class Serialized13Name {
    public static void main(String args[]) throws Exception {
        Name name;
        String serialFilename = System.getProperty("test.src", ".") +
                          "/" + "j2se13-name.ser";
        ObjectInputStream in = new ObjectInputStream(
                                new FileInputStream(serialFilename));
        System.out.println();
        System.out.println("Deserialized Name class object:" + in.readObject());
        in.close();
    }
}

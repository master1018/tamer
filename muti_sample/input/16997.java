public class Boot {
    public Boot(ObjectInputStream oin) throws Exception {
        oin.readObject();
    }
}

public class InactiveRegistration {
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject("foo");
        oout.close();
        ObjectInputStream oin = new ObjectInputStream(
            new ByteArrayInputStream(bout.toByteArray()));
        try {
            oin.registerValidation(new ObjectInputValidation() {
                public void validateObject() throws InvalidObjectException {}
            }, 0);
            throw new Error(
                "registerValidation should fail on inactive stream");
        } catch (NotActiveException ex) {
        }
    }
}

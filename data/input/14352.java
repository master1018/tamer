public class Test4165217 {
    public static void main(String[] args) {
        JColorChooser chooser = new JColorChooser();
        chooser.setColor(new Color(new Random().nextInt()));
        Color before = chooser.getColor();
        Color after = copy(chooser).getColor();
        if (!after.equals(before)) {
            throw new Error("color is changed after serialization");
        }
    }
    private static JColorChooser copy(JColorChooser chooser) {
        try {
            return (JColorChooser) deserialize(serialize(chooser));
        }
        catch (ClassNotFoundException exception) {
            throw new Error("unexpected exception during class creation", exception);
        }
        catch (IOException exception) {
            throw new Error("unexpected exception during serialization", exception);
        }
    }
    private static byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.flush();
        return baos.toByteArray();
    }
    private static Object deserialize(byte[] array) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(array);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
    }
}

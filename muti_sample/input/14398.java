public class GetField implements Serializable{
    String str;
    int i;
    public static void main(String[] args) throws Exception {
        ObjectStreamClass cl = ObjectStreamClass.lookup(GetField.class);
        if (cl == null)
            throw new RuntimeException("Cannot resolve class : GetField");
        if (cl.getField("str") == null)
            throw new RuntimeException(
                "ObjectStreamClass.getField() failed for object type");
        if (cl.getField("i") == null)
            throw new RuntimeException(
                "ObjectStreamClass.getField() failed for primitive type");
    }
}

public class SetObject {
    public static void main(String[] args) throws Exception {
        ModelIdentifier id = new ModelIdentifier("test","a",1);
        id.setObject("hello");
        if(!id.getObject().equals("hello"))
            throw new RuntimeException("id.getObject() does't return \"hello\"!");
    }
}

public class NewModelIdentifierStringStringInt {
    public static void main(String[] args) throws Exception {
        ModelIdentifier id = new ModelIdentifier("test","a",1);;
        if(!id.getObject().equals("test"))
            throw new RuntimeException("id.getObject() doesn't return \"test\"!");
        if(!id.getVariable().equals("a"))
            throw new RuntimeException("id.getVariable() doesn't return \"a\"!");
        if(id.getInstance() != 1)
            throw new RuntimeException("id.getInstance() doesn't return 1!");
    }
}

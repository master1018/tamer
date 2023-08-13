public class NewModelIdentifierString {
    public static void main(String[] args) throws Exception {
        ModelIdentifier id = new ModelIdentifier("test");
        if(!id.getObject().equals("test"))
            throw new RuntimeException("id.getObject() doesn't return \"test\"!");
        if(id.getVariable() != null)
            throw new RuntimeException("id.getVariable() doesn't return null!");
        if(id.getInstance() != 0)
            throw new RuntimeException("id.getInstance() doesn't return 0!");
    }
}

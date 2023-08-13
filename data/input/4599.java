public class EqualsObject {
    public static void main(String[] args) throws Exception {
        ModelIdentifier id = new ModelIdentifier("test","a",1);
        ModelIdentifier id2 = new ModelIdentifier("test","a",1);
        ModelIdentifier id3 = new ModelIdentifier("test","a",2);
        ModelIdentifier id4 = new ModelIdentifier("test","b",1);
        ModelIdentifier id5 = new ModelIdentifier("hello","a",1);
        if(!id.equals(id2))
            throw new RuntimeException("Compare failed!");
        if(id.equals(id3))
            throw new RuntimeException("Compare failed!");
        if(id.equals(id4))
            throw new RuntimeException("Compare failed!");
        if(id.equals(id5))
            throw new RuntimeException("Compare failed!");
    }
}

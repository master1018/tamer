public class GetDotResource {
    public static void main(String[] args) throws Exception {
        if (GetDotResource.class.getClassLoader().
            getResourceAsStream(".resource") == null)
            throw new Exception("Could not find resource with " +
                                "leading . in their names");
    }
}

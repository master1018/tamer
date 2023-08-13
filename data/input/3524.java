public class LastIndexOf {
    public static void main(String argv[]) throws Exception {
        Vector v = new Vector(10);
        try{
            int i = v.lastIndexOf(null, 5);
            throw new Exception("lastIndexOf(5/10) " + i);
        } catch (IndexOutOfBoundsException e) {
        }
    }
}

public class UnsoundInference {
    public static void main(String[] args) {
        Object[] objArray = {new Object()};
        ArrayList<String> strList = new ArrayList<String>();
        transferBug(objArray, strList);
        String str = strList.get(0);
    }
    public static <Var> void transferBug(Var[] from, Collection<Var> to) {
        to.add(from[0]);
    }
}

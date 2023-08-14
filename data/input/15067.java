public class ReplaceAll {
    static final int SIZE = 20;
    public static void main(String[] args) throws Exception {
        List a[] = {new ArrayList(), new LinkedList(), new Vector()};
        for (int i=0; i<a.length; i++) {
            List lst = a[i];
            for (int j=1; j<=SIZE; j++)
                lst.add(new Integer(j % 3));
            List goal = Collections.nCopies(SIZE, "*");
            for (int j=0; j<3; j++) {
                List before = new ArrayList(lst);
                if (!Collections.replaceAll(lst, new Integer(j), "*"))
                    throw new Exception("False return value: "+i+", "+j);
                if (lst.equals(before))
                    throw new Exception("Unchanged: "+i+", "+j+", "+": "+lst);
                if (lst.equals(goal) != (j==2))
                    throw new Exception("Wrong change:"+i+", "+j);
            }
            if (Collections.replaceAll(lst, "love", "hate"))
                throw new Exception("True return value: "+i);
        }
    }
}

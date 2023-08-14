public class MakeBooleanComparable {
    public static void main(String args[]) {
        Random rnd = new Random();
        List<Boolean> list = new ArrayList<Boolean>();
        int numFalse = 0;
        for (int i = 0; i < 1000; i++) {
            boolean element = rnd.nextBoolean();
            if (!element)
                numFalse++;
            list.add(element); 
        }
        Collections.sort(list);
        for (int i = 0; i < numFalse; i++)
            if (list.get(i).booleanValue())  
                throw new RuntimeException("False positive: " + i);
        for (int i = numFalse; i < 1000; i++)
            if (!list.get(i).booleanValue()) 
                throw new RuntimeException("False negative: " + i);
    }
}

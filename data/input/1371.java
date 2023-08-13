public class IsEmpty {
    private static String [] tests = { "", " ", "a",
                                       "It was the best of times, it was the worst of times, it was the age of wisdom, it was the age of foolishness, it was the epoch of belief, it was the epoch of incredulity, it was the season of Light, it was the season of Darkness, it was the spring of hope, it was the winter of despair, we had everything before us, we had nothing before us, we were all going direct to Heaven, we were all going direct the other way- in short, the period was so far like the present period, that some of its noisiest authorities insisted on its being received, for good or for evil, in the superlative degree of comparison only.  -- Charles Dickens, Tale of Two Cities"
    };
    public static void main(String [] args) {
        for (int i = 0; i < tests.length; i++) {
            String s = tests[i];
            int len = s.length();
            boolean empty = s.isEmpty();
            if ((len != 0 && empty) || (len == 0 && !empty))
                throw new RuntimeException("String \"" + s + "\": "
                                           + " isEmpty = " + empty
                                           + ", length = " + len);
        }
    }
}

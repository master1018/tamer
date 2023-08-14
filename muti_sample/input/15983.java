public class Bug7003643 {
    private static final int N = 5;
    private static final String[] elements = {
        "'{'", "'{", "{", "''", "}", "a", "'",
    };
    public static void main(String[] args) {
        Random rand = new Random();
        int count = 0;
        int max = (int) (Math.pow((double)elements.length, (double)N)/0.52);
        while (count < max) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < N; i++) {
                sb.append(elements[rand.nextInt(elements.length)]);
            }
            String pattern = sb.toString();
            MessageFormat mf = null;
            try {
                mf = new MessageFormat(pattern);
            } catch (IllegalArgumentException e) {
            }
            if (mf == null) {
                continue;
            }
            count++;
            String res1 = MessageFormat.format(pattern, 123);
            String toPattern = mf.toPattern();
            String res2 = MessageFormat.format(toPattern, 123);
            if (!res1.equals(res2)) {
                String s = String.format("Failed%n      pattern=\"%s\"  =>  result=\"%s\"%n"
                                         + "  toPattern()=\"%s\"  =>  result=\"%s\"%n",
                                         pattern, res1, toPattern, res2);
                throw new RuntimeException(s);
            }
        }
    }
}

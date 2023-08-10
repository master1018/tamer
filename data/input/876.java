public class Transposition {
    public static String encyrpt(String text, int[] key) {
        String[] s = new String[key.length];
        String t = "";
        for (int i = 0; i < key.length; i++) {
            s[i] = "";
        }
        while (text.length() % key.length != 0) text = text.concat(Transposition.randomLetter());
        while (text.length() > 0) {
            for (int i = 0; i < key.length; i++) {
                s[i] = s[i].concat(text.substring(0, 1));
                text = text.substring(1);
            }
        }
        for (int i = 0; i < key.length; i++) {
            t = t.concat(s[key[i]]);
        }
        return t;
    }
    private static String randomLetter() {
        int rand = (int) (Math.random() * 25.999999999999) + 97;
        char c = (char) rand;
        return Character.toString(c);
    }
    @SuppressWarnings("unchecked")
    public static int[] toKey(String s) {
        s = s.replaceAll(" ", "");
        int[] key = null;
        boolean weiter = false;
        int[] set = null;
        if (Character.isLetter(s.charAt(0))) {
            key = new int[s.length()];
            set = new int[key.length];
            for (int i = 0; i < set.length; i++) set[i] = 0;
            char[] text = s.toCharArray();
            char[] text2 = text.clone();
            Arrays.sort(text2);
            for (int i = 0; i < s.length(); i++) {
                if (!Character.isLetter(text[i])) return null;
                key[i] = Arrays.binarySearch(text2, text[i]);
                set[key[i]]++;
                if (set[key[i]] == 2) weiter = true;
            }
        } else if (Character.isDigit(s.charAt(0))) {
            Vector<Integer> num = new Vector<Integer>();
            while (s.length() > 0) {
                String t;
                if (s.indexOf(",") != -1) {
                    t = s.substring(0, s.indexOf(","));
                    s = s.substring(s.indexOf(",") + 1);
                } else {
                    t = s;
                    s = "";
                }
                try {
                    num.add(Integer.parseInt(t));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            key = new int[num.size()];
            Object[] num2 = ((Vector<Integer>) num.clone()).toArray();
            Arrays.sort(num2);
            set = new int[key.length];
            for (int i = 0; i < set.length; i++) set[i] = 0;
            for (int i = 0; i < key.length; i++) {
                key[i] = Arrays.binarySearch(num2, num.toArray()[i]);
                set[key[i]]++;
                if (set[key[i]] == 2) weiter = true;
            }
        }
        if (weiter) {
            int i = 0;
            while (i < set.length) {
                if (indexOf(key, i, true) < 0) {
                    int j = 0;
                    while (set[j] < 2) j++;
                    key[indexOf(key, j, true)] = i;
                    set[j]--;
                } else i++;
            }
        }
        return key;
    }
    private static int indexOf(int[] k, int j, boolean vorne) {
        if (vorne) {
            for (int i = 0; i < k.length; i++) if (k[i] == j) return i;
        } else {
            for (int i = k.length - 1; i >= 0; i--) if (k[i] == j) return i;
        }
        return -1;
    }
}

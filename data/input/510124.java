public class CandidateFilter {
    public static final int FILTER_NONE = 0x0;
    public static final int FILTER_NON_ASCII = 0x2;
    public int filter = 0;
    public boolean isAllowed(WnnWord word) {
        if (filter == 0) {
            return true;
        }
        if ((filter & FILTER_NON_ASCII) != 0) {
            String str = word.candidate;
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) < 0x20 || 0x7E < str.charAt(i)) {
                    return false;
                }
            }
        }
        return true;
    }
}

import com.aliasi.classify.ConfusionMatrix;
import com.aliasi.lm.LanguageModel;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
public class SpellEvaluator {
    private final SpellChecker mSpellChecker;
    private final List<String> mTextList = new ArrayList<String>();
    private final List<String> mCorrectTextList = new ArrayList<String>();
    private final List<String> mSuggestionList = new ArrayList<String>();
    private String mLastCaseReport = "No cases added yet.";
    private int mUserCorrectSystemWrongSuggestion = 0;
    private int mUserCorrectSystemNoSuggestion = 0;
    private int mUserErrorSystemNoSuggestion = 0;
    private int mUserErrorSystemCorrect = 0;
    private int mUserErrorSystemWrongSuggestion = 0;
    private final ObjectToCounterMap<String> mTokenCounter;
    public SpellEvaluator(SpellChecker checker) {
        this(checker, null);
    }
    public SpellEvaluator(SpellChecker checker, ObjectToCounterMap<String> tokenCounter) {
        mSpellChecker = checker;
        mTokenCounter = tokenCounter;
    }
    public void addCase(String text, String correctText) {
        String normalizedText = normalize(text);
        String normalizedCorrectText = normalize(correctText);
        String suggestion = mSpellChecker.didYouMean(text);
        String normalizedSuggestion = (suggestion == null) ? normalizedText : normalize(suggestion);
        mTextList.add(normalizedText);
        mCorrectTextList.add(normalizedCorrectText);
        mSuggestionList.add(normalizedSuggestion);
        String resultDescription = null;
        if (normalizedText.equals(normalizedCorrectText)) {
            resultDescription = "user correct, ";
            if (normalizedText.equals(normalizedSuggestion)) {
                resultDescription += "spell check wrong suggestion (FP)";
                ++mUserCorrectSystemWrongSuggestion;
            } else {
                resultDescription += "spell check no suggestion (TN)";
                ++mUserCorrectSystemNoSuggestion;
            }
        } else {
            resultDescription = "user incorrect, ";
            if (normalizedText.equals(normalizedSuggestion)) {
                resultDescription += "spell check no suggestion (FN)";
                ++mUserErrorSystemNoSuggestion;
            } else if (normalizedCorrectText.equals(normalizedSuggestion)) {
                resultDescription += "spell check correct (TP)";
                ++mUserErrorSystemCorrect;
            } else {
                resultDescription += "spell check wrong suggestion (FP,FN)";
                ++mUserErrorSystemWrongSuggestion;
            }
        }
        StringBuilder sb = new StringBuilder();
        report(sb, "input", normalizedText);
        sb.append("\n");
        report(sb, "correct", normalizedCorrectText);
        sb.append("\n");
        report(sb, "suggest", normalizedSuggestion);
        sb.append("\n");
        mLastCaseReport = sb.toString();
    }
    void report(StringBuilder sb, String msg, String text) {
        sb.append(msg + "=|" + text + "|");
        if (!(mSpellChecker instanceof CompiledSpellChecker)) return;
        CompiledSpellChecker checker = (CompiledSpellChecker) mSpellChecker;
        LanguageModel lm = checker.languageModel();
        double estimate = lm.log2Estimate(" " + text + " ") - lm.log2Estimate(" ");
        sb.append(" log2 p=" + lpFormat(estimate));
        TokenizerFactory tf = checker.tokenizerFactory();
        char[] cs = text.toCharArray();
        Tokenizer tokenizer = tf.tokenizer(cs, 0, cs.length);
        String[] tokens = tokenizer.tokenize();
        Set<String> tokenSet = checker.tokenSet();
        for (int i = 0; i < tokens.length; ++i) {
            sb.append(" ");
            sb.append(tokens[i]);
            sb.append("[");
            if (mTokenCounter == null) sb.append(tokenSet.contains(tokens[i]) ? "+" : "-"); else sb.append(mTokenCounter.getCount(tokens[i]));
            sb.append("]");
        }
    }
    static final DecimalFormat LP_FORMAT = new DecimalFormat("#0.0");
    static String lpFormat(double x) {
        return LP_FORMAT.format(x);
    }
    @Override
    public String toString() {
        int userErrors = mUserErrorSystemWrongSuggestion + mUserErrorSystemCorrect + mUserErrorSystemNoSuggestion;
        int userCorrect = mUserCorrectSystemWrongSuggestion + mUserCorrectSystemNoSuggestion;
        int total = userErrors + userCorrect;
        StringBuilder sb = new StringBuilder();
        sb.append("EVALUATION\n");
        addReport(sb, "User Error", userErrors, total);
        addReport(sb, "     System Correct", mUserErrorSystemCorrect, userErrors);
        addReport(sb, "     System Error", mUserErrorSystemWrongSuggestion, userErrors);
        addReport(sb, "     System No Suggestion", mUserErrorSystemNoSuggestion, userErrors);
        addReport(sb, "User Correct", userCorrect, total);
        addReport(sb, "     System Error", mUserCorrectSystemWrongSuggestion, userCorrect);
        addReport(sb, "     System No Suggestion", mUserCorrectSystemNoSuggestion, userCorrect);
        sb.append("SPELL CHECKER toString()\n");
        sb.append(mSpellChecker);
        return sb.toString();
    }
    static void addReport(StringBuilder sb, String msg, int correct, int total) {
        sb.append(msg);
        sb.append(": ");
        sb.append(correct);
        sb.append(" [");
        double percentage = (total > 0) ? (100.0 * correct) / total : 0;
        sb.append(PERCENT_FORMAT.format(percentage));
        sb.append("%]\n");
    }
    static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("##0.0");
    public String[][] userCorrectSystemNoSuggestion() {
        return extract(true, true, true);
    }
    public String[][] userCorrectSystemWrongSuggestion() {
        return extract(true, false, false);
    }
    public String[][] userErrorSystemCorrect() {
        return extract(false, true, false);
    }
    public String[][] userErrorSystemWrongSuggestion() {
        return extract(false, false, false);
    }
    public String[][] userErrorSystemNoSuggestion() {
        return extract(false, false, true);
    }
    String[][] extract(boolean textEqualsCorrect, boolean correctEqualsSuggestion, boolean textEqualsSuggestion) {
        List<String[]> result = new ArrayList<String[]>();
        for (int i = 0; i < mSuggestionList.size(); ++i) {
            String text = mTextList.get(i).toString();
            String correct = mCorrectTextList.get(i).toString();
            String suggestion = mSuggestionList.get(i).toString();
            if (text.equals(correct) == textEqualsCorrect && correct.equals(suggestion) == correctEqualsSuggestion && text.equals(suggestion) == textEqualsSuggestion) result.add(new String[] { text, correct, suggestion });
        }
        return result.<String[]>toArray(Strings.EMPTY_STRING_2D_ARRAY);
    }
    public String getLastCaseReport() {
        return mLastCaseReport;
    }
    public ConfusionMatrix confusionMatrix() {
        int tn = mUserCorrectSystemNoSuggestion;
        int tp = mUserErrorSystemCorrect;
        int fn = mUserErrorSystemNoSuggestion + mUserErrorSystemWrongSuggestion;
        int fp = mUserCorrectSystemWrongSuggestion;
        return new ConfusionMatrix(new String[] { "correct", "misspelled" }, new int[][] { { tp, fp }, { fn, tn } });
    }
    public String normalize(String text) {
        return text;
    }
}

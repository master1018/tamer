class DictionaryBasedBreakIteratorBuilder extends RuleBasedBreakIteratorBuilder {
    private boolean[] categoryFlags;
    private CharSet dictionaryChars = new CharSet();
    private String dictionaryExpression = "";
    public DictionaryBasedBreakIteratorBuilder(String description) {
        super(description);
    }
    protected void handleSpecialSubstitution(String replace, String replaceWith,
                                             int startPos, String description) {
        super.handleSpecialSubstitution(replace, replaceWith, startPos, description);
        if (replace.equals("<dictionary>")) {
            if (replaceWith.charAt(0) == '(') {
                error("Dictionary group can't be enclosed in (", startPos, description);
            }
            dictionaryExpression = replaceWith;
            dictionaryChars = CharSet.parseString(replaceWith);
        }
    }
    protected void buildCharCategories(Vector tempRuleList) {
        super.buildCharCategories(tempRuleList);
        categoryFlags = new boolean[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            CharSet cs = (CharSet)categories.elementAt(i);
            if (!(cs.intersection(dictionaryChars).empty())) {
                categoryFlags[i] = true;
            }
        }
    }
    protected void mungeExpressionList(Hashtable expressions) {
        expressions.put(dictionaryExpression, dictionaryChars);
    }
    void makeFile(String filename) {
        super.setAdditionalData(super.toByteArray(categoryFlags));
        super.makeFile(filename);
    }
}

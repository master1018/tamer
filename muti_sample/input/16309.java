public class ChoiceCallback implements Callback, java.io.Serializable {
    private static final long serialVersionUID = -3975664071579892167L;
    private String prompt;
    private String[] choices;
    private int defaultChoice;
    private boolean multipleSelectionsAllowed;
    private int[] selections;
    public ChoiceCallback(String prompt, String[] choices,
                int defaultChoice, boolean multipleSelectionsAllowed) {
        if (prompt == null || prompt.length() == 0 ||
            choices == null || choices.length == 0 ||
            defaultChoice < 0 || defaultChoice >= choices.length)
            throw new IllegalArgumentException();
        for (int i = 0; i < choices.length; i++) {
            if (choices[i] == null || choices[i].length() == 0)
                throw new IllegalArgumentException();
        }
        this.prompt = prompt;
        this.choices = choices;
        this.defaultChoice = defaultChoice;
        this.multipleSelectionsAllowed = multipleSelectionsAllowed;
    }
    public String getPrompt() {
        return prompt;
    }
    public String[] getChoices() {
        return choices;
    }
    public int getDefaultChoice() {
        return defaultChoice;
    }
    public boolean allowMultipleSelections() {
        return multipleSelectionsAllowed;
    }
    public void setSelectedIndex(int selection) {
        this.selections = new int[1];
        this.selections[0] = selection;
    }
    public void setSelectedIndexes(int[] selections) {
        if (!multipleSelectionsAllowed)
            throw new UnsupportedOperationException();
        this.selections = selections;
    }
    public int[] getSelectedIndexes() {
        return selections;
    }
}

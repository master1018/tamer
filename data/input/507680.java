public abstract class AbstractSuggestionWrapper implements Suggestion {
    protected abstract Suggestion current();
    public String getShortcutId() {
        return current().getShortcutId();
    }
    public String getSuggestionFormat() {
        return current().getSuggestionFormat();
    }
    public String getSuggestionIcon1() {
        return current().getSuggestionIcon1();
    }
    public String getSuggestionIcon2() {
        return current().getSuggestionIcon2();
    }
    public String getSuggestionIntentAction() {
        return current().getSuggestionIntentAction();
    }
    public String getSuggestionIntentDataString() {
        return current().getSuggestionIntentDataString();
    }
    public String getSuggestionIntentExtraData() {
        return current().getSuggestionIntentExtraData();
    }
    public String getSuggestionLogType() {
        return current().getSuggestionLogType();
    }
    public String getSuggestionQuery() {
        return current().getSuggestionQuery();
    }
    public Source getSuggestionSource() {
        return current().getSuggestionSource();
    }
    public String getSuggestionText1() {
        return current().getSuggestionText1();
    }
    public String getSuggestionText2() {
        return current().getSuggestionText2();
    }
    public String getSuggestionText2Url() {
        return current().getSuggestionText2Url();
    }
    public boolean isSpinnerWhileRefreshing() {
        return current().isSpinnerWhileRefreshing();
    }
    public boolean isSuggestionShortcut() {
        return current().isSuggestionShortcut();
    }
    public boolean isWebSearchSuggestion() {
        return current().isWebSearchSuggestion();
    }
}

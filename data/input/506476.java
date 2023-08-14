public class ContactSuggestionView extends DefaultSuggestionView {
    private ContactBadge mQuickContact;
    public ContactSuggestionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public ContactSuggestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ContactSuggestionView(Context context) {
        super(context);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mQuickContact = (ContactBadge) findViewById(R.id.icon1);
    }
    @Override
    public void bindAsSuggestion(SuggestionCursor suggestion, SuggestionClickListener onClick) {
        super.bindAsSuggestion(suggestion, onClick);
        mQuickContact.assignContactUri(Uri.parse(suggestion.getSuggestionIntentDataString()));
        mQuickContact.setExtraOnClickListener(new ContactBadgeClickListener());
    }
    private class ContactBadgeClickListener implements View.OnClickListener {
        public void onClick(View v) {
            fireOnSuggestionQuickContactClicked();
        }
    }
}
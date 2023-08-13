public class BrowserHomepagePreference extends EditTextPreference {
    private String mCurrentPage;
    public BrowserHomepagePreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }
    public BrowserHomepagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public BrowserHomepagePreference(Context context) {
        super(context);
    }
    @Override
    protected void onAddEditTextToDialogView(View dialogView,
            EditText editText) {
        super.onAddEditTextToDialogView(dialogView, editText);
        ViewGroup parent = (ViewGroup) editText.getParent();
        Button button = new Button(getContext());
        button.setText(R.string.pref_use_current);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getEditText().setText(mCurrentPage);
            }
        });
        if (parent instanceof LinearLayout) {
            ((LinearLayout) parent).setGravity(Gravity.CENTER_HORIZONTAL);
        }
        parent.addView(button, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String url = getEditText().getText().toString();
            if (url.length() > 0
                    && !BrowserActivity.ACCEPTED_URI_SCHEMA.matcher(url)
                            .matches()) {
                int colon = url.indexOf(':');
                int space = url.indexOf(' ');
                if (colon == -1 && space == -1) {
                    getEditText().setText("http:
                } else {
                    new AlertDialog.Builder(this.getContext()).setMessage(
                            R.string.bookmark_url_not_valid).setPositiveButton(
                            R.string.ok, null).show();
                    positiveResult = false;
                }
            }
        }
        super.onDialogClosed(positiveResult);
    }
     void setCurrentPage(String currentPage) {
        mCurrentPage = currentPage;
    }
    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        Window window = getDialog().getWindow();
        View decorView = window.getDecorView();
        WindowManager.LayoutParams params
                = (WindowManager.LayoutParams) decorView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        window.getWindowManager().updateViewLayout(decorView, params);
    }
}

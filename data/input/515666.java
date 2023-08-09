public class ListSetSelection extends ListScenario {
    private Button mButton;
    @Override
    protected void init(Params params) {
        params.setStackFromBottom(false)
                .setStartingSelectionPosition(-1)
                .setNumItems(1000)
                .setItemScreenSizeFactor(0.22);
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mButton = new Button(this);
        mButton.setText("setSelection(0)");
        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getListView().setSelection(0);
            }
        });
        getListViewContainer().addView(mButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
    }
    public Button getButton() {
        return mButton;
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_S) {
            getListView().setSelection(0);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}

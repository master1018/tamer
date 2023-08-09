public class MenuLayout extends MenuScenario {
    private static final String LONG_TITLE = "Really really really really really really really really really really long title";
    private static final String SHORT_TITLE = "Item";
    private Button mButton;
    @Override
    protected void onInitParams(Params params) {
        super.onInitParams(params);
        params
            .setNumItems(2)
            .setItemTitle(0, LONG_TITLE)
            .setItemTitle(1, LONG_TITLE);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        onCreateOptionsMenu(menu);
        return true;
    }
    public Button getButton() {
        return mButton;
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mButton  = new Button(this);
        setContentView(mButton);
    }
}

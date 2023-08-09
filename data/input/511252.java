public class MenuWith1Item extends MenuScenario {
    private Button mButton;
    @Override
    protected void onInitParams(Params params) {
        super.onInitParams(params);
        params.setNumItems(1);
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

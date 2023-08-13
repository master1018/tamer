public class Merge extends Activity {
    private LinearLayout mLayout;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mLayout = new LinearLayout(this);
        mLayout.setOrientation(LinearLayout.VERTICAL);
        LayoutInflater.from(this).inflate(R.layout.merge_tag, mLayout);
        setContentView(mLayout);
    }
    public ViewGroup getLayout() {
        return mLayout;
    }
}

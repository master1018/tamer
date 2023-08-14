public class InternalSelectionScroll extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView sv = new ScrollView(this);
        ViewGroup.LayoutParams svLp = new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout ll = new LinearLayout(this);
        ll.setLayoutParams(svLp);
        sv.addView(ll);
        InternalSelectionView isv = new InternalSelectionView(this, 10);
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        LinearLayout.LayoutParams llLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                2 * screenHeight);  
        isv.setLayoutParams(llLp);
        ll.addView(isv);
        setContentView(sv);
    }
}

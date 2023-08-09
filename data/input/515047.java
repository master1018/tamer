public class ScrollBar3 extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scrollbar3);
        findViewById(R.id.view3).setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
    }
}

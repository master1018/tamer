public class ListViewHeight extends Activity {
    private View mButton1;
    private View mButton2;
    private View mButton3;
    private View mOuterLayout;
    private ListView mInnerList;
    ArrayAdapter<String> mAdapter;
    private String[] mStrings = {
            "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi" };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linear_layout_listview_height);
        mButton1 = findViewById(R.id.button1);
        mButton2 = findViewById(R.id.button2);
        mButton3 = findViewById(R.id.button3);
        mOuterLayout = findViewById(R.id.layout);
        mInnerList = (ListView)findViewById(R.id.inner_list);
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, 
                                            mStrings);
        mButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ViewGroup.MarginLayoutParams lp;
                lp = (ViewGroup.MarginLayoutParams) mInnerList.getLayoutParams();
                lp.height = 200;
                mInnerList.setLayoutParams(lp);
                mInnerList.setAdapter(mAdapter);
                mOuterLayout.setVisibility(View.VISIBLE);
            }
        });
        mButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ViewGroup.MarginLayoutParams lp;
                lp = (ViewGroup.MarginLayoutParams) mInnerList.getLayoutParams();
                lp.height = lp.MATCH_PARENT;
                mInnerList.setLayoutParams(lp);
                mInnerList.setAdapter(mAdapter);
                mOuterLayout.setVisibility(View.VISIBLE);
            }
        });
        mButton3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mInnerList.setAdapter(null);
                View parent = (View) mOuterLayout.getParent();      
                View grandpa = (View) parent.getParent();           
                View great = (View) grandpa.getParent();            
                great.setVisibility(View.GONE);
            }
        });
    }
}

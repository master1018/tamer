public class AdapterViewStubActivity extends Activity {
    private ListView mView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = new ListView(this);
        mView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        setContentView(mView);
    }
    public ListView getListView() {
        return mView;
    }
    public ArrayAdapter<String> getArrayAdapter() {
        final List<String> list = new ArrayList<String>();
        for (int i = 0; i < 4; i++) {
            list.add("test:" + i);
        }
        return new ArrayAdapter<String>(this, R.layout.adapterview_layout, list);
    }
}

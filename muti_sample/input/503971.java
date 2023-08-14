public class LayoutAnimStubActivity extends ListActivity {
    private String[] mStrings = {
            "Android",
            "CTS",
            "Test",
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mStrings));
    }
}

public class ListActivityTestHelper extends ListActivity {
    public ListView listView;
    public View view;
    public int itemPosition;
    public long itemId;
    public boolean isOnContentChangedCalled = false;
    public static boolean isOnRestoreInstanceStateCalled = false;
    public boolean isSubActivityFinished = false;
    private static final int WAIT_BEFORE_FINISH = 1;
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        listView = l;
        view = v;
        itemPosition = position;
        itemId = id;
    }
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setListAdapter(new ArrayAdapter<String>(this,
                R.layout.list_activity_layout, STRING_ITEMS));
        Intent intent = new Intent(TestedScreen.WAIT_BEFORE_FINISH);
        intent.setClass(this, LocalScreen.class);
        startActivityForResult(intent, WAIT_BEFORE_FINISH);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case WAIT_BEFORE_FINISH:
                isSubActivityFinished = true;
                break;
            default:
                break;
        }
    }
    public static final String[] STRING_ITEMS = {
            "Item 0", "Item 1", "Item 2", "Item 3", "Item 4", "Item 5"
    };
    @Override
    public void onContentChanged() {
        isOnContentChangedCalled = true;
        super.onContentChanged();
    }
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        isOnRestoreInstanceStateCalled = true;
        super.onRestoreInstanceState(state);
    }
}

public class ListWithEmptyView extends ListActivity {
    private class CarefulAdapter<T> extends ArrayAdapter<T> {
        public CarefulAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }
        @Override
        public long getItemId(int position) {
            if (position <  0 || position >= this.getCount()) {
                throw new ArrayIndexOutOfBoundsException();
            }
            return super.getItemId(position);
        }
    }
    public static final int MENU_ADD = Menu.FIRST + 1;
    public static final int MENU_REMOVE = Menu.FIRST + 2;
    private CarefulAdapter<String> mAdapter;
    private int mNextItem = 0;
    private View mEmptyView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new CarefulAdapter<String>(this,
                android.R.layout.simple_list_item_1);
        setContentView(R.layout.list_with_empty_view);
        setListAdapter(mAdapter);
        mEmptyView = findViewById(R.id.empty);
        getListView().setEmptyView(mEmptyView);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_ADD, 0, R.string.menu_add)
                .setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, MENU_REMOVE, 0, R.string.menu_remove)
                .setIcon(android.R.drawable.ic_menu_delete);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD:
                String str = "Item + " + mNextItem++;
                mAdapter.add(str);
                return true;
            case MENU_REMOVE:
                if (mAdapter.getCount() > 0) {
                    mAdapter.remove(mAdapter.getItem(0));
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public View getEmptyView() {
        return mEmptyView;
    }
}

public class MenuScenario extends Activity implements MenuItem.OnMenuItemClickListener {
    private Params mParams = new Params();
    private Menu mMenu;
    private MenuItem[] mItems;
    private boolean[] mWasItemClicked;
    private MenuAdapter[] mMenuAdapters = new MenuAdapter[MenuBuilder.NUM_TYPES];
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        dispatchInitParams();
    }
    private void dispatchInitParams() {
        onInitParams(mParams);
        onParamsChanged();
    }
    public void setParams(Params params) {
        mParams = params;
        onParamsChanged();
    }
    public void onParamsChanged() {
        mItems = new MenuItem[mParams.numItems];
        mWasItemClicked = new boolean[mParams.numItems];
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        if (!mParams.shouldShowMenu) return false;
        MenuItem item;
        for (int i = 0; i < mParams.numItems; i++) {
            if ((item = onAddMenuItem(menu, i)) == null) {
                CharSequence givenTitle = mParams.itemTitles.get(i);
                item = menu.add(0, 0, 0, (givenTitle != null) ? givenTitle : ("Item " + i));
            }
            if (item != null) {
                mItems[i] = item;
                if (mParams.listenForClicks) {
                    item.setOnMenuItemClickListener(this);
                }
            }
        }
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mMenu = menu;
        return mParams.shouldShowMenu;
    }
    protected MenuItem onAddMenuItem(Menu menu, int itemPosition) {
        return null;
    }
    protected void onInitParams(Params params) {
    }
    public Menu getMenu() {
        return mMenu;
    }
    public boolean onMenuItemClick(MenuItem item) {
        final int position = findItemPosition(item);
        if (position < 0) return false;
        mWasItemClicked[position] = true;
        return true;
    }
    public boolean wasItemClicked(int position) {
        return mWasItemClicked[position];
    }
    public int findItemPosition(MenuItem item) {
        for (int i = 0; i < mParams.numItems; i++) {
            if (mItems[i] == item) return i;
        }
        return -1;
    }
    public MenuAdapter getMenuAdapter(int menuType) {
        if (mMenuAdapters[menuType] == null) {
            mMenuAdapters[menuType] = ((MenuBuilder) mMenu).getMenuAdapter(menuType);
        }
        return mMenuAdapters[menuType];
    }
    public View getMenuView(int menuType) {
        return ((MenuBuilder) mMenu).getMenuView(menuType, null);
    }
    public View getItemView(int menuType, int position) {
        return getMenuAdapter(menuType).getView(position, null, null);
    }
    public static class Params {
        private boolean shouldShowMenu = true;
        private int numItems = 10;
        private boolean listenForClicks = true;
        private SparseArray<CharSequence> itemTitles = new SparseArray<CharSequence>();
        public Params setShouldShowMenu(boolean shouldShowMenu) {
            this.shouldShowMenu = shouldShowMenu;
            return this;
        }
        public Params setNumItems(int numItems) {
            this.numItems = numItems;
            return this;
        }
        public Params setListenForClicks(boolean listenForClicks) {
            this.listenForClicks = listenForClicks;
            return this;
        }
        public Params setItemTitle(int itemPos, CharSequence title) {
            itemTitles.put(itemPos, title);
            return this;
        }
    }
}

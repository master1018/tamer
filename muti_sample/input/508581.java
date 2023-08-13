public class MenuBuilder implements Menu {
    private static final String LOGTAG = "MenuBuilder";
    public static final int NUM_TYPES = 3;
    public static final int TYPE_ICON = 0;
    public static final int TYPE_EXPANDED = 1;
    public static final int TYPE_DIALOG = 2;
    private static final String VIEWS_TAG = "android:views";
    static final int THEME_RES_FOR_TYPE[] = new int[] {
        com.android.internal.R.style.Theme_IconMenu,
        com.android.internal.R.style.Theme_ExpandedMenu,
        0,
    };
    static final int LAYOUT_RES_FOR_TYPE[] = new int[] {
        com.android.internal.R.layout.icon_menu_layout,
        com.android.internal.R.layout.expanded_menu_layout,
        0,
    };
    static final int ITEM_LAYOUT_RES_FOR_TYPE[] = new int[] {
        com.android.internal.R.layout.icon_menu_item_layout,
        com.android.internal.R.layout.list_menu_item_layout,
        com.android.internal.R.layout.list_menu_item_layout,
    };
    private static final int[]  sCategoryToOrder = new int[] {
        1, 
        4, 
        5, 
        3, 
        2, 
        0, 
    };
    private final Context mContext;
    private final Resources mResources;
    private boolean mQwertyMode;
    private boolean mShortcutsVisible;
    private Callback mCallback;
    private ArrayList<MenuItemImpl> mItems;
    private ArrayList<MenuItemImpl> mVisibleItems;
    private boolean mIsVisibleItemsStale;
    private ContextMenuInfo mCurrentMenuInfo;
    CharSequence mHeaderTitle;
    Drawable mHeaderIcon;
    View mHeaderView;
    private SparseArray<Parcelable> mFrozenViewStates;
    private boolean mPreventDispatchingItemsChanged = false;
    private boolean mOptionalIconsVisible = false;
    private MenuType[] mMenuTypes;
    class MenuType {
        private int mMenuType;
        private LayoutInflater mInflater;
        private WeakReference<MenuView> mMenuView;
        MenuType(int menuType) {
            mMenuType = menuType;
        }
        LayoutInflater getInflater() {
            if (mInflater == null) {
                Context wrappedContext = new ContextThemeWrapper(mContext,
                        THEME_RES_FOR_TYPE[mMenuType]); 
                mInflater = (LayoutInflater) wrappedContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            return mInflater;
        }
        MenuView getMenuView(ViewGroup parent) {
            if (LAYOUT_RES_FOR_TYPE[mMenuType] == 0) {
                return null;
            }
            synchronized (this) {
                MenuView menuView = mMenuView != null ? mMenuView.get() : null;
                if (menuView == null) {
                    menuView = (MenuView) getInflater().inflate(
                            LAYOUT_RES_FOR_TYPE[mMenuType], parent, false);
                    menuView.initialize(MenuBuilder.this, mMenuType);
                    mMenuView = new WeakReference<MenuView>(menuView);
                    if (mFrozenViewStates != null) {
                        View view = (View) menuView;
                        view.restoreHierarchyState(mFrozenViewStates);
                        mFrozenViewStates.remove(view.getId());
                    }
                }
                return menuView;
            }
        }
        boolean hasMenuView() {
            return mMenuView != null && mMenuView.get() != null;
        }
    }
    public interface Callback {
        public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item);
        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing);
        public boolean onSubMenuSelected(SubMenuBuilder subMenu);
        public void onCloseSubMenu(SubMenuBuilder menu);
        public void onMenuModeChange(MenuBuilder menu);
    }
    public interface ItemInvoker {
        public boolean invokeItem(MenuItemImpl item);
    }
    public MenuBuilder(Context context) {
        mMenuTypes = new MenuType[NUM_TYPES];
        mContext = context;
        mResources = context.getResources();
        mItems = new ArrayList<MenuItemImpl>();
        mVisibleItems = new ArrayList<MenuItemImpl>();
        mIsVisibleItemsStale = true;
        mShortcutsVisible =
                (mResources.getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS);
    }
    public void setCallback(Callback callback) {
        mCallback = callback;
    }
    MenuType getMenuType(int menuType) {
        if (mMenuTypes[menuType] == null) {
            mMenuTypes[menuType] = new MenuType(menuType);
        }
        return mMenuTypes[menuType];
    }
    public View getMenuView(int menuType, ViewGroup parent) {
        if (menuType == TYPE_EXPANDED
                && (mMenuTypes[TYPE_ICON] == null || !mMenuTypes[TYPE_ICON].hasMenuView())) {
            getMenuType(TYPE_ICON).getMenuView(parent);
        }
        return (View) getMenuType(menuType).getMenuView(parent);
    }
    private int getNumIconMenuItemsShown() {
        ViewGroup parent = null;
        if (!mMenuTypes[TYPE_ICON].hasMenuView()) {
            if (mMenuTypes[TYPE_EXPANDED].hasMenuView()) {
                View expandedMenuView = (View) mMenuTypes[TYPE_EXPANDED].getMenuView(null);
                parent = (ViewGroup) expandedMenuView.getParent();
            }
        }
        return ((IconMenuView) getMenuView(TYPE_ICON, parent)).getNumActualItemsShown(); 
    }
    public void clearMenuViews() {
        for (int i = NUM_TYPES - 1; i >= 0; i--) {
            if (mMenuTypes[i] != null) {
                mMenuTypes[i].mMenuView = null;
            }
        }
        for (int i = mItems.size() - 1; i >= 0; i--) {
            MenuItemImpl item = mItems.get(i);
            if (item.hasSubMenu()) {
                ((SubMenuBuilder) item.getSubMenu()).clearMenuViews();
            }
            item.clearItemViews();
        }
    }
    private MenuItem addInternal(int group, int id, int categoryOrder, CharSequence title) {
        final int ordering = getOrdering(categoryOrder);
        final MenuItemImpl item = new MenuItemImpl(this, group, id, categoryOrder, ordering, title);
        if (mCurrentMenuInfo != null) {
            item.setMenuInfo(mCurrentMenuInfo);
        }
        mItems.add(findInsertIndex(mItems, ordering), item);
        onItemsChanged(false);
        return item;
    }
    public MenuItem add(CharSequence title) {
        return addInternal(0, 0, 0, title);
    }
    public MenuItem add(int titleRes) {
        return addInternal(0, 0, 0, mResources.getString(titleRes));
    }
    public MenuItem add(int group, int id, int categoryOrder, CharSequence title) {
        return addInternal(group, id, categoryOrder, title);
    }
    public MenuItem add(int group, int id, int categoryOrder, int title) {
        return addInternal(group, id, categoryOrder, mResources.getString(title));
    }
    public SubMenu addSubMenu(CharSequence title) {
        return addSubMenu(0, 0, 0, title);
    }
    public SubMenu addSubMenu(int titleRes) {
        return addSubMenu(0, 0, 0, mResources.getString(titleRes));
    }
    public SubMenu addSubMenu(int group, int id, int categoryOrder, CharSequence title) {
        final MenuItemImpl item = (MenuItemImpl) addInternal(group, id, categoryOrder, title);
        final SubMenuBuilder subMenu = new SubMenuBuilder(mContext, this, item);
        item.setSubMenu(subMenu);
        return subMenu;
    }
    public SubMenu addSubMenu(int group, int id, int categoryOrder, int title) {
        return addSubMenu(group, id, categoryOrder, mResources.getString(title));
    }
    public int addIntentOptions(int group, int id, int categoryOrder, ComponentName caller,
            Intent[] specifics, Intent intent, int flags, MenuItem[] outSpecificItems) {
        PackageManager pm = mContext.getPackageManager();
        final List<ResolveInfo> lri =
                pm.queryIntentActivityOptions(caller, specifics, intent, 0);
        final int N = lri != null ? lri.size() : 0;
        if ((flags & FLAG_APPEND_TO_GROUP) == 0) {
            removeGroup(group);
        }
        for (int i=0; i<N; i++) {
            final ResolveInfo ri = lri.get(i);
            Intent rintent = new Intent(
                ri.specificIndex < 0 ? intent : specifics[ri.specificIndex]);
            rintent.setComponent(new ComponentName(
                    ri.activityInfo.applicationInfo.packageName,
                    ri.activityInfo.name));
            final MenuItem item = add(group, id, categoryOrder, ri.loadLabel(pm))
                    .setIcon(ri.loadIcon(pm))
                    .setIntent(rintent);
            if (outSpecificItems != null && ri.specificIndex >= 0) {
                outSpecificItems[ri.specificIndex] = item;
            }
        }
        return N;
    }
    public void removeItem(int id) {
        removeItemAtInt(findItemIndex(id), true);
    }
    public void removeGroup(int group) {
        final int i = findGroupIndex(group);
        if (i >= 0) {
            final int maxRemovable = mItems.size() - i;
            int numRemoved = 0;
            while ((numRemoved++ < maxRemovable) && (mItems.get(i).getGroupId() == group)) {
                removeItemAtInt(i, false);
            }
            onItemsChanged(false);
        }
    }
    private void removeItemAtInt(int index, boolean updateChildrenOnMenuViews) {
        if ((index < 0) || (index >= mItems.size())) return;
        mItems.remove(index);
        if (updateChildrenOnMenuViews) onItemsChanged(false);
    }
    public void removeItemAt(int index) {
        removeItemAtInt(index, true);
    }
    public void clearAll() {
        mPreventDispatchingItemsChanged = true;
        clear();
        clearHeader();
        mPreventDispatchingItemsChanged = false;
        onItemsChanged(true);
    }
    public void clear() {
        mItems.clear();
        onItemsChanged(true);
    }
    void setExclusiveItemChecked(MenuItem item) {
        final int group = item.getGroupId();
        final int N = mItems.size();
        for (int i = 0; i < N; i++) {
            MenuItemImpl curItem = mItems.get(i);
            if (curItem.getGroupId() == group) {
                if (!curItem.isExclusiveCheckable()) continue;
                if (!curItem.isCheckable()) continue;
                curItem.setCheckedInt(curItem == item);
            }
        }
    }
    public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {
        final int N = mItems.size();
        for (int i = 0; i < N; i++) {
            MenuItemImpl item = mItems.get(i);
            if (item.getGroupId() == group) {
                item.setExclusiveCheckable(exclusive);
                item.setCheckable(checkable);
            }
        }
    }
    public void setGroupVisible(int group, boolean visible) {
        final int N = mItems.size();
        boolean changedAtLeastOneItem = false;
        for (int i = 0; i < N; i++) {
            MenuItemImpl item = mItems.get(i);
            if (item.getGroupId() == group) {
                if (item.setVisibleInt(visible)) changedAtLeastOneItem = true;
            }
        }
        if (changedAtLeastOneItem) onItemsChanged(false);
    }
    public void setGroupEnabled(int group, boolean enabled) {
        final int N = mItems.size();
        for (int i = 0; i < N; i++) {
            MenuItemImpl item = mItems.get(i);
            if (item.getGroupId() == group) {
                item.setEnabled(enabled);
            }
        }
    }
    public boolean hasVisibleItems() {
        final int size = size();
        for (int i = 0; i < size; i++) {
            MenuItemImpl item = mItems.get(i);
            if (item.isVisible()) {
                return true;
            }
        }
        return false;
    }
    public MenuItem findItem(int id) {
        final int size = size();
        for (int i = 0; i < size; i++) {
            MenuItemImpl item = mItems.get(i);
            if (item.getItemId() == id) {
                return item;
            } else if (item.hasSubMenu()) {
                MenuItem possibleItem = item.getSubMenu().findItem(id);
                if (possibleItem != null) {
                    return possibleItem;
                }
            }
        }
        return null;
    }
    public int findItemIndex(int id) {
        final int size = size();
        for (int i = 0; i < size; i++) {
            MenuItemImpl item = mItems.get(i);
            if (item.getItemId() == id) {
                return i;
            }
        }
        return -1;
    }
    public int findGroupIndex(int group) {
        return findGroupIndex(group, 0);
    }
    public int findGroupIndex(int group, int start) {
        final int size = size();
        if (start < 0) {
            start = 0;
        }
        for (int i = start; i < size; i++) {
            final MenuItemImpl item = mItems.get(i);
            if (item.getGroupId() == group) {
                return i;
            }
        }
        return -1;
    }
    public int size() {
        return mItems.size();
    }
    public MenuItem getItem(int index) {
        return mItems.get(index);
    }
    public boolean isShortcutKey(int keyCode, KeyEvent event) {
        return findItemWithShortcutForKey(keyCode, event) != null;
    }
    public void setQwertyMode(boolean isQwerty) {
        mQwertyMode = isQwerty;
        refreshShortcuts(isShortcutsVisible(), isQwerty);
    }
    private static int getOrdering(int categoryOrder)
    {
        final int index = (categoryOrder & CATEGORY_MASK) >> CATEGORY_SHIFT;
        if (index < 0 || index >= sCategoryToOrder.length) {
            throw new IllegalArgumentException("order does not contain a valid category.");
        }
        return (sCategoryToOrder[index] << CATEGORY_SHIFT) | (categoryOrder & USER_MASK);
    }
    boolean isQwertyMode() {
        return mQwertyMode;
    }
    private void refreshShortcuts(boolean shortcutsVisible, boolean qwertyMode) {
        MenuItemImpl item;
        for (int i = mItems.size() - 1; i >= 0; i--) {
            item = mItems.get(i);
            if (item.hasSubMenu()) {
                ((MenuBuilder) item.getSubMenu()).refreshShortcuts(shortcutsVisible, qwertyMode);
            }
            item.refreshShortcutOnItemViews(shortcutsVisible, qwertyMode);
        }
    }
    public void setShortcutsVisible(boolean shortcutsVisible) {
        if (mShortcutsVisible == shortcutsVisible) return;
        mShortcutsVisible =
            (mResources.getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
            && shortcutsVisible;
        refreshShortcuts(mShortcutsVisible, isQwertyMode());
    }
    public boolean isShortcutsVisible() {
        return mShortcutsVisible;
    }
    Resources getResources() {
        return mResources;
    }
    public Callback getCallback() {
        return mCallback;
    }
    public Context getContext() {
        return mContext;
    }
    private static int findInsertIndex(ArrayList<MenuItemImpl> items, int ordering) {
        for (int i = items.size() - 1; i >= 0; i--) {
            MenuItemImpl item = items.get(i);
            if (item.getOrdering() <= ordering) {
                return i + 1;
            }
        }
        return 0;
    }
    public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
        final MenuItemImpl item = findItemWithShortcutForKey(keyCode, event);
        boolean handled = false;
        if (item != null) {
            handled = performItemAction(item, flags);
        }
        if ((flags & FLAG_ALWAYS_PERFORM_CLOSE) != 0) {
            close(true);
        }
        return handled;
    }
    List<MenuItemImpl> findItemsWithShortcutForKey(int keyCode, KeyEvent event) {
        final boolean qwerty = isQwertyMode();
        final int metaState = event.getMetaState();
        final KeyCharacterMap.KeyData possibleChars = new KeyCharacterMap.KeyData();
        final boolean isKeyCodeMapped = event.getKeyData(possibleChars);
        if (!isKeyCodeMapped && (keyCode != KeyEvent.KEYCODE_DEL)) {
            return null;
        }
        Vector<MenuItemImpl> items = new Vector();
        final int N = mItems.size();
        for (int i = 0; i < N; i++) {
            MenuItemImpl item = mItems.get(i);
            if (item.hasSubMenu()) {
                List<MenuItemImpl> subMenuItems = ((MenuBuilder)item.getSubMenu())
                    .findItemsWithShortcutForKey(keyCode, event);
                items.addAll(subMenuItems);
            }
            final char shortcutChar = qwerty ? item.getAlphabeticShortcut() : item.getNumericShortcut();
            if (((metaState & (KeyEvent.META_SHIFT_ON | KeyEvent.META_SYM_ON)) == 0) &&
                  (shortcutChar != 0) &&
                  (shortcutChar == possibleChars.meta[0]
                      || shortcutChar == possibleChars.meta[2]
                      || (qwerty && shortcutChar == '\b' &&
                          keyCode == KeyEvent.KEYCODE_DEL)) &&
                  item.isEnabled()) {
                items.add(item);
            }
        }
        return items;
    }
    MenuItemImpl findItemWithShortcutForKey(int keyCode, KeyEvent event) {
        List<MenuItemImpl> items = findItemsWithShortcutForKey(keyCode, event);
        if (items == null) {
            return null;
        }
        final int metaState = event.getMetaState();
        final KeyCharacterMap.KeyData possibleChars = new KeyCharacterMap.KeyData();
        event.getKeyData(possibleChars);
        if (items.size() == 1) {
            return items.get(0);
        }
        final boolean qwerty = isQwertyMode();
        for (MenuItemImpl item : items) {
            final char shortcutChar = qwerty ? item.getAlphabeticShortcut() : item.getNumericShortcut();
            if ((shortcutChar == possibleChars.meta[0] &&
                    (metaState & KeyEvent.META_ALT_ON) == 0)
                || (shortcutChar == possibleChars.meta[2] &&
                    (metaState & KeyEvent.META_ALT_ON) != 0)
                || (qwerty && shortcutChar == '\b' &&
                    keyCode == KeyEvent.KEYCODE_DEL)) {
                return item;
            }
        }
        return null;
    }
    public boolean performIdentifierAction(int id, int flags) {
        return performItemAction(findItem(id), flags);           
    }
    public boolean performItemAction(MenuItem item, int flags) {
        MenuItemImpl itemImpl = (MenuItemImpl) item;
        if (itemImpl == null || !itemImpl.isEnabled()) {
            return false;
        }        
        boolean invoked = itemImpl.invoke();
        if (item.hasSubMenu()) {
            close(false);
            if (mCallback != null) {
                invoked = mCallback.onSubMenuSelected((SubMenuBuilder) item.getSubMenu())
                        || invoked;
            }
        } else {
            if ((flags & FLAG_PERFORM_NO_CLOSE) == 0) {
                close(true);
            }
        }
        return invoked;
    }
    final void close(boolean allMenusAreClosing) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.onCloseMenu(this, allMenusAreClosing);
        }
    }
    public void close() {
        close(true);
    }
    private void onItemsChanged(boolean cleared) {
        if (!mPreventDispatchingItemsChanged) {
            if (mIsVisibleItemsStale == false) mIsVisibleItemsStale = true;
            MenuType[] menuTypes = mMenuTypes;
            for (int i = 0; i < NUM_TYPES; i++) {
                if ((menuTypes[i] != null) && (menuTypes[i].hasMenuView())) {
                    MenuView menuView = menuTypes[i].mMenuView.get();
                    menuView.updateChildren(cleared);
                }
            }
        }
    }
    void onItemVisibleChanged(MenuItemImpl item) {
        onItemsChanged(false);
    }
    ArrayList<MenuItemImpl> getVisibleItems() {
        if (!mIsVisibleItemsStale) return mVisibleItems;
        mVisibleItems.clear();
        final int itemsSize = mItems.size(); 
        MenuItemImpl item;
        for (int i = 0; i < itemsSize; i++) {
            item = mItems.get(i);
            if (item.isVisible()) mVisibleItems.add(item);
        }
        mIsVisibleItemsStale = false;
        return mVisibleItems;
    }
    public void clearHeader() {
        mHeaderIcon = null;
        mHeaderTitle = null;
        mHeaderView = null;
        onItemsChanged(false);
    }
    private void setHeaderInternal(final int titleRes, final CharSequence title, final int iconRes,
            final Drawable icon, final View view) {
        final Resources r = getResources();
        if (view != null) {
            mHeaderView = view;
            mHeaderTitle = null;
            mHeaderIcon = null;
        } else {
            if (titleRes > 0) {
                mHeaderTitle = r.getText(titleRes);
            } else if (title != null) {
                mHeaderTitle = title;
            }
            if (iconRes > 0) {
                mHeaderIcon = r.getDrawable(iconRes);
            } else if (icon != null) {
                mHeaderIcon = icon;
            }
            mHeaderView = null;
        }
        onItemsChanged(false);
    }
    protected MenuBuilder setHeaderTitleInt(CharSequence title) {
        setHeaderInternal(0, title, 0, null, null);
        return this;
    }
    protected MenuBuilder setHeaderTitleInt(int titleRes) {
        setHeaderInternal(titleRes, null, 0, null, null);
        return this;
    }
    protected MenuBuilder setHeaderIconInt(Drawable icon) {
        setHeaderInternal(0, null, 0, icon, null);
        return this;
    }
    protected MenuBuilder setHeaderIconInt(int iconRes) {
        setHeaderInternal(0, null, iconRes, null, null);
        return this;
    }
    protected MenuBuilder setHeaderViewInt(View view) {
        setHeaderInternal(0, null, 0, null, view);
        return this;
    }
    public CharSequence getHeaderTitle() {
        return mHeaderTitle;
    }
    public Drawable getHeaderIcon() {
        return mHeaderIcon;
    }
    public View getHeaderView() {
        return mHeaderView;
    }
    public MenuBuilder getRootMenu() {
        return this;
    }
    public void setCurrentMenuInfo(ContextMenuInfo menuInfo) {
        mCurrentMenuInfo = menuInfo;
    }
    public MenuAdapter getMenuAdapter(int menuType) {
        return new MenuAdapter(menuType);
    }
    void setOptionalIconsVisible(boolean visible) {
        mOptionalIconsVisible = visible;
    }
    boolean getOptionalIconsVisible() {
        return mOptionalIconsVisible;
    }
    public void saveHierarchyState(Bundle outState) {
        SparseArray<Parcelable> viewStates = new SparseArray<Parcelable>();
        MenuType[] menuTypes = mMenuTypes;
        for (int i = NUM_TYPES - 1; i >= 0; i--) {
            if (menuTypes[i] == null) {
                continue;
            }
            if (menuTypes[i].hasMenuView()) {
                ((View) menuTypes[i].getMenuView(null)).saveHierarchyState(viewStates);
            }
        }
        outState.putSparseParcelableArray(VIEWS_TAG, viewStates);
    }
    public void restoreHierarchyState(Bundle inState) {
        SparseArray<Parcelable> viewStates = mFrozenViewStates = inState
                .getSparseParcelableArray(VIEWS_TAG);
        MenuType[] menuTypes = mMenuTypes;
        for (int i = NUM_TYPES - 1; i >= 0; i--) {
            if (menuTypes[i] == null) {
                continue;
            }
            if (menuTypes[i].hasMenuView()) {
                ((View) menuTypes[i].getMenuView(null)).restoreHierarchyState(viewStates);
            }
        }
    }
    public class MenuAdapter extends BaseAdapter {
        private int mMenuType;
        public MenuAdapter(int menuType) {
            mMenuType = menuType;
        }
        public int getOffset() {
            if (mMenuType == TYPE_EXPANDED) {
                return getNumIconMenuItemsShown(); 
            } else {
                return 0;
            }
        }
        public int getCount() {
            return getVisibleItems().size() - getOffset();
        }
        public MenuItemImpl getItem(int position) {
            return getVisibleItems().get(position + getOffset());
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            return ((MenuItemImpl) getItem(position)).getItemView(mMenuType, parent);
        }
    }
}

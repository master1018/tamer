public final class MenuItemImpl implements MenuItem {
    private static final String TAG = "MenuItemImpl";
    private final int mId;
    private final int mGroup;
    private final int mCategoryOrder;
    private final int mOrdering;
    private CharSequence mTitle;
    private CharSequence mTitleCondensed;
    private Intent mIntent;
    private char mShortcutNumericChar;
    private char mShortcutAlphabeticChar;
    private Drawable mIconDrawable;
    private int mIconResId = NO_ICON;
    private WeakReference<ItemView> mItemViews[];
    private MenuBuilder mMenu;
    private SubMenuBuilder mSubMenu;
    private Runnable mItemCallback;
    private MenuItem.OnMenuItemClickListener mClickListener;
    private int mFlags = ENABLED;
    private static final int CHECKABLE      = 0x00000001;
    private static final int CHECKED        = 0x00000002;
    private static final int EXCLUSIVE      = 0x00000004;
    private static final int HIDDEN         = 0x00000008;
    private static final int ENABLED        = 0x00000010;
    static final int NO_ICON = 0;
    private ContextMenuInfo mMenuInfo;
    private static String sPrependShortcutLabel;
    private static String sEnterShortcutLabel;
    private static String sDeleteShortcutLabel;
    private static String sSpaceShortcutLabel;
    MenuItemImpl(MenuBuilder menu, int group, int id, int categoryOrder, int ordering,
            CharSequence title) {
        if (sPrependShortcutLabel == null) {
            sPrependShortcutLabel = menu.getContext().getResources().getString(
                    com.android.internal.R.string.prepend_shortcut_label);
            sEnterShortcutLabel = menu.getContext().getResources().getString(
                    com.android.internal.R.string.menu_enter_shortcut_label);
            sDeleteShortcutLabel = menu.getContext().getResources().getString(
                    com.android.internal.R.string.menu_delete_shortcut_label);
            sSpaceShortcutLabel = menu.getContext().getResources().getString(
                    com.android.internal.R.string.menu_space_shortcut_label);
        }
        mItemViews = new WeakReference[MenuBuilder.NUM_TYPES];
        mMenu = menu;
        mId = id;
        mGroup = group;
        mCategoryOrder = categoryOrder;
        mOrdering = ordering;
        mTitle = title;
    }
    public boolean invoke() {
        if (mClickListener != null &&
            mClickListener.onMenuItemClick(this)) {
            return true;
        }
        MenuBuilder.Callback callback = mMenu.getCallback(); 
        if (callback != null &&
            callback.onMenuItemSelected(mMenu.getRootMenu(), this)) {
            return true;
        }
        if (mItemCallback != null) {
            mItemCallback.run();
            return true;
        }
        if (mIntent != null) {
            try {
                mMenu.getContext().startActivity(mIntent);
                return true;
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "Can't find activity to handle intent; ignoring", e);
            }
        }
        return false;
    }
    private boolean hasItemView(int menuType) {
        return mItemViews[menuType] != null && mItemViews[menuType].get() != null;
    }
    public boolean isEnabled() {
        return (mFlags & ENABLED) != 0;
    }
    public MenuItem setEnabled(boolean enabled) {
        if (enabled) {
            mFlags |= ENABLED;
        } else {
            mFlags &= ~ENABLED;
        }
        for (int i = MenuBuilder.NUM_TYPES - 1; i >= 0; i--) {
            if (hasItemView(i)) {
                mItemViews[i].get().setEnabled(enabled);
            }
        }
        return this;
    }
    public int getGroupId() {
        return mGroup;
    }
    @ViewDebug.CapturedViewProperty
    public int getItemId() {
        return mId;
    }
    public int getOrder() {
        return mCategoryOrder;
    }
    public int getOrdering() {
        return mOrdering; 
    }
    public Intent getIntent() {
        return mIntent;
    }
    public MenuItem setIntent(Intent intent) {
        mIntent = intent;
        return this;
    }
    Runnable getCallback() {
        return mItemCallback;
    }
    public MenuItem setCallback(Runnable callback) {
        mItemCallback = callback;
        return this;
    }
    public char getAlphabeticShortcut() {
        return mShortcutAlphabeticChar;
    }
    public MenuItem setAlphabeticShortcut(char alphaChar) {
        if (mShortcutAlphabeticChar == alphaChar) return this;
        mShortcutAlphabeticChar = Character.toLowerCase(alphaChar);
        refreshShortcutOnItemViews();
        return this;
    }
    public char getNumericShortcut() {
        return mShortcutNumericChar;
    }
    public MenuItem setNumericShortcut(char numericChar) {
        if (mShortcutNumericChar == numericChar) return this;
        mShortcutNumericChar = numericChar;
        refreshShortcutOnItemViews();
        return this;
    }
    public MenuItem setShortcut(char numericChar, char alphaChar) {
        mShortcutNumericChar = numericChar;
        mShortcutAlphabeticChar = Character.toLowerCase(alphaChar);
        refreshShortcutOnItemViews();
        return this;
    }
    char getShortcut() {
        return (mMenu.isQwertyMode() ? mShortcutAlphabeticChar : mShortcutNumericChar);
    }
    String getShortcutLabel() {
        char shortcut = getShortcut();
        if (shortcut == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(sPrependShortcutLabel);
        switch (shortcut) {
            case '\n':
                sb.append(sEnterShortcutLabel);
                break;
            case '\b':
                sb.append(sDeleteShortcutLabel);
                break;
            case ' ':
                sb.append(sSpaceShortcutLabel);
                break;
            default:
                sb.append(shortcut);
                break;
        }
        return sb.toString();
    }
    boolean shouldShowShortcut() {
        return mMenu.isShortcutsVisible() && (getShortcut() != 0);
    }
    private void refreshShortcutOnItemViews() {
        refreshShortcutOnItemViews(mMenu.isShortcutsVisible(), mMenu.isQwertyMode());
    }
    void refreshShortcutOnItemViews(boolean menuShortcutShown, boolean isQwertyMode) {
        final char shortcutKey = (isQwertyMode) ? mShortcutAlphabeticChar : mShortcutNumericChar;
        final boolean showShortcut = menuShortcutShown && (shortcutKey != 0);
        for (int i = MenuBuilder.NUM_TYPES - 1; i >= 0; i--) {
            if (hasItemView(i)) {
                mItemViews[i].get().setShortcut(showShortcut, shortcutKey);
            }
        }
    }
    public SubMenu getSubMenu() {
        return mSubMenu;
    }
    public boolean hasSubMenu() {
        return mSubMenu != null;
    }
    void setSubMenu(SubMenuBuilder subMenu) {
        if ((mMenu != null) && (mMenu instanceof SubMenu)) {
            throw new UnsupportedOperationException(
            "Attempt to add a sub-menu to a sub-menu.");
        }
        mSubMenu = subMenu;
        subMenu.setHeaderTitle(getTitle());
    }
    @ViewDebug.CapturedViewProperty
    public CharSequence getTitle() {
        return mTitle;
    }
    CharSequence getTitleForItemView(MenuView.ItemView itemView) {
        return ((itemView != null) && itemView.prefersCondensedTitle())
                ? getTitleCondensed()
                : getTitle();
    }
    public MenuItem setTitle(CharSequence title) {
        mTitle = title;
        for (int i = MenuBuilder.NUM_TYPES - 1; i >= 0; i--) {
            if (!hasItemView(i)) {
                continue;
            }
            ItemView itemView = mItemViews[i].get(); 
            if (!itemView.prefersCondensedTitle() || mTitleCondensed == null) {
                itemView.setTitle(title);
            }
        }
        if (mSubMenu != null) {
            mSubMenu.setHeaderTitle(title);
        }
        return this;
    }
    public MenuItem setTitle(int title) {
        return setTitle(mMenu.getContext().getString(title));
    }
    public CharSequence getTitleCondensed() {
        return mTitleCondensed != null ? mTitleCondensed : mTitle;
    }
    public MenuItem setTitleCondensed(CharSequence title) {
        mTitleCondensed = title;
        if (title == null) {
            title = mTitle;
        }
        for (int i = MenuBuilder.NUM_TYPES - 1; i >= 0; i--) {
            if (hasItemView(i) && (mItemViews[i].get().prefersCondensedTitle())) {
                mItemViews[i].get().setTitle(title);
            }
        }
        return this;
    }
    public Drawable getIcon() {
        if (mIconDrawable != null) {
            return mIconDrawable;
        }
        if (mIconResId != NO_ICON) {
            return mMenu.getResources().getDrawable(mIconResId);
        }
        return null;
    }
    public MenuItem setIcon(Drawable icon) {
        mIconResId = NO_ICON;
        mIconDrawable = icon;
        setIconOnViews(icon);
        return this;
    }
    public MenuItem setIcon(int iconResId) {
        mIconDrawable = null;
        mIconResId = iconResId;
        if (haveAnyOpenedIconCapableItemViews()) {
            Drawable drawable = iconResId != NO_ICON ? mMenu.getResources().getDrawable(iconResId)
                    : null;
            setIconOnViews(drawable);
        }
        return this;
    }
    private void setIconOnViews(Drawable icon) {
        for (int i = MenuBuilder.NUM_TYPES - 1; i >= 0; i--) {
            if (hasItemView(i) && mItemViews[i].get().showsIcon()) {
                mItemViews[i].get().setIcon(icon);
            }
        }
    }
    private boolean haveAnyOpenedIconCapableItemViews() {
        for (int i = MenuBuilder.NUM_TYPES - 1; i >= 0; i--) {
            if (hasItemView(i) && mItemViews[i].get().showsIcon()) {
                return true;
            }
        }
        return false;
    }
    public boolean isCheckable() {
        return (mFlags & CHECKABLE) == CHECKABLE;
    }
    public MenuItem setCheckable(boolean checkable) {
        final int oldFlags = mFlags;
        mFlags = (mFlags & ~CHECKABLE) | (checkable ? CHECKABLE : 0);
        if (oldFlags != mFlags) {
            for (int i = MenuBuilder.NUM_TYPES - 1; i >= 0; i--) {
                if (hasItemView(i)) {
                    mItemViews[i].get().setCheckable(checkable);
                }
            }
        }
        return this;
    }
    public void setExclusiveCheckable(boolean exclusive)
    {
        mFlags = (mFlags&~EXCLUSIVE) | (exclusive ? EXCLUSIVE : 0);
    }
    public boolean isExclusiveCheckable() {
        return (mFlags & EXCLUSIVE) != 0;
    }
    public boolean isChecked() {
        return (mFlags & CHECKED) == CHECKED;
    }
    public MenuItem setChecked(boolean checked) {
        if ((mFlags & EXCLUSIVE) != 0) {
            mMenu.setExclusiveItemChecked(this);
        } else {
            setCheckedInt(checked);
        }
        return this;
    }
    void setCheckedInt(boolean checked) {
        final int oldFlags = mFlags;
        mFlags = (mFlags & ~CHECKED) | (checked ? CHECKED : 0);
        if (oldFlags != mFlags) {
            for (int i = MenuBuilder.NUM_TYPES - 1; i >= 0; i--) {
                if (hasItemView(i)) {
                    mItemViews[i].get().setChecked(checked);
                }
            }
        }
    }
    public boolean isVisible() {
        return (mFlags & HIDDEN) == 0;
    }
    boolean setVisibleInt(boolean shown) {
        final int oldFlags = mFlags;
        mFlags = (mFlags & ~HIDDEN) | (shown ? 0 : HIDDEN);
        return oldFlags != mFlags;
    }
    public MenuItem setVisible(boolean shown) {
        if (setVisibleInt(shown)) mMenu.onItemVisibleChanged(this);
        return this;
    }
   public MenuItem setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener clickListener) {
        mClickListener = clickListener;
        return this;
    }
    View getItemView(int menuType, ViewGroup parent) {
        if (!hasItemView(menuType)) {
            mItemViews[menuType] = new WeakReference<ItemView>(createItemView(menuType, parent));
        }
        return (View) mItemViews[menuType].get();
    }
    private MenuView.ItemView createItemView(int menuType, ViewGroup parent) {
        MenuView.ItemView itemView = (MenuView.ItemView) getLayoutInflater(menuType)
                .inflate(MenuBuilder.ITEM_LAYOUT_RES_FOR_TYPE[menuType], parent, false);
        itemView.initialize(this, menuType);
        return itemView;
    }
    void clearItemViews() {
        for (int i = mItemViews.length - 1; i >= 0; i--) {
            mItemViews[i] = null;
        }
    }
    @Override
    public String toString() {
        return mTitle.toString();
    }
    void setMenuInfo(ContextMenuInfo menuInfo) {
        mMenuInfo = menuInfo;
    }
    public ContextMenuInfo getMenuInfo() {
        return mMenuInfo;
    }
    public LayoutInflater getLayoutInflater(int menuType) {
        return mMenu.getMenuType(menuType).getInflater();
    }
    public boolean shouldShowIcon(int menuType) {
        return menuType == MenuBuilder.TYPE_ICON || mMenu.getOptionalIconsVisible();
    }
}

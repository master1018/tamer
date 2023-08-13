public final class ExpandedMenuView extends ListView implements ItemInvoker, MenuView, OnItemClickListener {
    private MenuBuilder mMenu;
    private int mAnimations;
    public ExpandedMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.MenuView, 0, 0);
        mAnimations = a.getResourceId(com.android.internal.R.styleable.MenuView_windowAnimationStyle, 0);
        a.recycle();
        setOnItemClickListener(this);
    }
    public void initialize(MenuBuilder menu, int menuType) {
        mMenu = menu;
        setAdapter(menu.new MenuAdapter(menuType));
    }
    public void updateChildren(boolean cleared) {
        ListAdapter adapter = getAdapter();
        if (adapter != null) {
            if (cleared) {
                ((BaseAdapter)adapter).notifyDataSetInvalidated();
            }
            else {
                ((BaseAdapter)adapter).notifyDataSetChanged();
            }
        }
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setChildrenDrawingCacheEnabled(false);
    }
    @Override
    protected boolean recycleOnMeasure() {
        return false;
    }
    public boolean invokeItem(MenuItemImpl item) {
        return mMenu.performItemAction(item, 0);
    }
    public void onItemClick(AdapterView parent, View v, int position, long id) {
        invokeItem((MenuItemImpl) getAdapter().getItem(position));
    }
    public int getWindowAnimations() {
        return mAnimations;
    }
}

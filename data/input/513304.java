public final class IconMenuItemView extends TextView implements MenuView.ItemView {
    private static final int NO_ALPHA = 0xFF;
    private IconMenuView mIconMenuView;
    private ItemInvoker mItemInvoker;
    private MenuItemImpl mItemData; 
    private Drawable mIcon;
    private int mTextAppearance;
    private Context mTextAppearanceContext;
    private float mDisabledAlpha;
    private Rect mPositionIconAvailable = new Rect();
    private Rect mPositionIconOutput = new Rect();
    private boolean mShortcutCaptionMode;
    private String mShortcutCaption;
    private static String sPrependShortcutLabel;
    public IconMenuItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        if (sPrependShortcutLabel == null) {
            sPrependShortcutLabel = getResources().getString(
                    com.android.internal.R.string.prepend_shortcut_label);
        }
        TypedArray a =
            context.obtainStyledAttributes(
                attrs, com.android.internal.R.styleable.MenuView, defStyle, 0);
        mDisabledAlpha = a.getFloat(
                com.android.internal.R.styleable.MenuView_itemIconDisabledAlpha, 0.8f);
        mTextAppearance = a.getResourceId(com.android.internal.R.styleable.
                                          MenuView_itemTextAppearance, -1);
        mTextAppearanceContext = context;
        a.recycle();
    }
    public IconMenuItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    void initialize(CharSequence title, Drawable icon) {
        setClickable(true);
        setFocusable(true);
        if (mTextAppearance != -1) {
            setTextAppearance(mTextAppearanceContext, mTextAppearance);
        }
        setTitle(title);
        setIcon(icon);
    }
    public void initialize(MenuItemImpl itemData, int menuType) {
        mItemData = itemData;
        initialize(itemData.getTitleForItemView(this), itemData.getIcon());
        setVisibility(itemData.isVisible() ? View.VISIBLE : View.GONE);
        setEnabled(itemData.isEnabled());
    }
    @Override
    public boolean performClick() {
        if (super.performClick()) {
            return true;
        }
        if ((mItemInvoker != null) && (mItemInvoker.invokeItem(mItemData))) {
            playSoundEffect(SoundEffectConstants.CLICK);
            return true;
        } else {
            return false;
        }
    }
    public void setTitle(CharSequence title) {
        if (mShortcutCaptionMode) {
            setCaptionMode(true);
        } else if (title != null) {
            setText(title);
        }
    }
    void setCaptionMode(boolean shortcut) {
        if (mItemData == null) {
            return;
        }
        mShortcutCaptionMode = shortcut && (mItemData.shouldShowShortcut());
        CharSequence text = mItemData.getTitleForItemView(this);
        if (mShortcutCaptionMode) {
            if (mShortcutCaption == null) {
                mShortcutCaption = mItemData.getShortcutLabel();
            }
            text = mShortcutCaption;
        }
        setText(text);
    }
    public void setIcon(Drawable icon) {
        mIcon = icon;
        if (icon != null) {
            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            setCompoundDrawables(null, icon, null, null);
            setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            requestLayout();
        } else {
            setCompoundDrawables(null, null, null, null);
            setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        }
    }
    public void setItemInvoker(ItemInvoker itemInvoker) {
        mItemInvoker = itemInvoker;
    }
    @ViewDebug.CapturedViewProperty(retrieveReturn = true)
    public MenuItemImpl getItemData() {
        return mItemData;
    }
    @Override
    public void setVisibility(int v) {
        super.setVisibility(v);
        if (mIconMenuView != null) {
            mIconMenuView.markStaleChildren();
        }
    }
    void setIconMenuView(IconMenuView iconMenuView) {
        mIconMenuView = iconMenuView;
    }
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mItemData != null && mIcon != null) {
            final boolean isInAlphaState = !mItemData.isEnabled() && (isPressed() || !isFocused());
            mIcon.setAlpha(isInAlphaState ? (int) (mDisabledAlpha * NO_ALPHA) : NO_ALPHA);
        }
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        positionIcon();
    }
    @Override
    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        super.onTextChanged(text, start, before, after);
        setLayoutParams(getTextAppropriateLayoutParams());
    }
    IconMenuView.LayoutParams getTextAppropriateLayoutParams() {
        IconMenuView.LayoutParams lp = (IconMenuView.LayoutParams) getLayoutParams();
        if (lp == null) {
            lp = new IconMenuView.LayoutParams(
                    IconMenuView.LayoutParams.MATCH_PARENT, IconMenuView.LayoutParams.MATCH_PARENT);
        }
        lp.desiredWidth = (int) Layout.getDesiredWidth(getText(), getPaint());
        return lp;
    }
    private void positionIcon() {
        if (mIcon == null) {
            return;
        }
        Rect tmpRect = mPositionIconOutput;
        getLineBounds(0, tmpRect);
        mPositionIconAvailable.set(0, 0, getWidth(), tmpRect.top);
        Gravity.apply(Gravity.CENTER_VERTICAL | Gravity.LEFT, mIcon.getIntrinsicWidth(), mIcon
                .getIntrinsicHeight(), mPositionIconAvailable, mPositionIconOutput);
        mIcon.setBounds(mPositionIconOutput);
    }
    public void setCheckable(boolean checkable) {
    }
    public void setChecked(boolean checked) {
    }
    public void setShortcut(boolean showShortcut, char shortcutKey) {
        if (mShortcutCaptionMode) {
            mShortcutCaption = null;
            setCaptionMode(true);
        }
    }
    public boolean prefersCondensedTitle() {
        return true;
    }
    public boolean showsIcon() {
        return true;
    }
}

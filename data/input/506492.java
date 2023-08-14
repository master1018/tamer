public class BlockedContactView extends LinearLayout {
    private ImageView mAvatar;
    private ImageView mBlockedIcon;
    private TextView  mLine1;
    private TextView  mLine2;
    public BlockedContactView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mBlockedIcon = (ImageView)findViewById(R.id.blocked);
        mLine1  = (TextView) findViewById(R.id.line1);
        mLine2  = (TextView) findViewById(R.id.line2);
    }
    public void bind(Cursor cursor) {
        long providerId = cursor.getLong(BlockedContactsActivity.PROVIDER_COLUMN);
        String username = cursor.getString(BlockedContactsActivity.USERNAME_COLUMN);
        String nickname = cursor.getString(BlockedContactsActivity.NICKNAME_COLUMN);
        Drawable avatar = DatabaseUtils.getAvatarFromCursor(cursor,
                BlockedContactsActivity.AVATAR_COLUMN);
        if (avatar != null) {
            mAvatar.setImageDrawable(avatar);
        } else {
            mAvatar.setImageResource(R.drawable.avatar_unknown);
        }
        ImApp app = ImApp.getApplication((Activity)mContext);
        BrandingResources brandingRes = app.getBrandingResource(providerId);
        mBlockedIcon.setImageDrawable(brandingRes.getDrawable(BrandingResourceIDs.DRAWABLE_BLOCK));
        mLine1.setText(nickname);
        mLine2.setText(ImpsAddressUtils.getDisplayableAddress(username));
    }
}

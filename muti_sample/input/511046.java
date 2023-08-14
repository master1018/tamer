public class GeolocationPermissionsPrompt extends LinearLayout {
    private LinearLayout mInner;
    private TextView mMessage;
    private Button mShareButton;
    private Button mDontShareButton;
    private CheckBox mRemember;
    private GeolocationPermissions.Callback mCallback;
    private String mOrigin;
    public GeolocationPermissionsPrompt(Context context) {
        this(context, null);
    }
    public GeolocationPermissionsPrompt(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    void init() {
        mInner = (LinearLayout) findViewById(R.id.inner);
        mMessage = (TextView) findViewById(R.id.message);
        mShareButton = (Button) findViewById(R.id.share_button);
        mDontShareButton = (Button) findViewById(R.id.dont_share_button);
        mRemember = (CheckBox) findViewById(R.id.remember);
        final GeolocationPermissionsPrompt me = this;
        mShareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                me.handleButtonClick(true);
            }
        });
        mDontShareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                me.handleButtonClick(false);
            }
        });
    }
    public void show(String origin, GeolocationPermissions.Callback callback) {
        mOrigin = origin;
        mCallback = callback;
        Uri uri = Uri.parse(mOrigin);
        setMessage("http".equals(uri.getScheme()) ?  mOrigin.substring(7) : mOrigin);
        mRemember.setChecked(true);
        showDialog(true);
    }
    public void hide() {
        showDialog(false);
    }
    private void handleButtonClick(boolean allow) {
        showDialog(false);
        boolean remember = mRemember.isChecked();
        if (remember) {
            Toast toast = Toast.makeText(
                    getContext(),
                    allow ? R.string.geolocation_permissions_prompt_toast_allowed :
                            R.string.geolocation_permissions_prompt_toast_disallowed,
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.show();
        }
        mCallback.invoke(mOrigin, allow, remember);
    }
    private void setMessage(CharSequence origin) {
        mMessage.setText(String.format(
            getResources().getString(R.string.geolocation_permissions_prompt_message),
            origin));
    }
    private void showDialog(boolean shown) {
        mInner.setVisibility(shown ? View.VISIBLE : View.GONE);
    }
}

public class ProviderPreference extends Preference {
    private Drawable mProviderIcon;
    private ImageView mProviderIconView;
    private CharSequence mProviderName;
    private String mAccountType;
    public ProviderPreference(Context context, String accountType, Drawable icon, CharSequence providerName) {
        super(context);
        mAccountType = accountType;
        mProviderIcon = icon;
        mProviderName = providerName;
        setLayoutResource(R.layout.provider_preference);
        setPersistent(false);
        setTitle(mProviderName);
    }
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mProviderIconView = (ImageView) view.findViewById(R.id.providerIcon);
        mProviderIconView.setImageDrawable(mProviderIcon);
        setTitle(mProviderName);
    }
    public String getAccountType() {
        return mAccountType;
    }
}

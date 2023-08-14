public class BluetoothDevicePreference extends Preference implements CachedBluetoothDevice.Callback {
    private static final String TAG = "BluetoothDevicePreference";
    private static int sDimAlpha = Integer.MIN_VALUE;
    private CachedBluetoothDevice mCachedDevice;
    private boolean mIsBusy;
    public BluetoothDevicePreference(Context context, CachedBluetoothDevice cachedDevice) {
        super(context);
        if (sDimAlpha == Integer.MIN_VALUE) {
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.disabledAlpha, outValue, true);
            sDimAlpha = (int) (outValue.getFloat() * 255);
        }
        mCachedDevice = cachedDevice;
        setLayoutResource(R.layout.preference_bluetooth);
        cachedDevice.registerCallback(this);
        onDeviceAttributesChanged(cachedDevice);
    }
    public CachedBluetoothDevice getCachedDevice() {
        return mCachedDevice;
    }
    @Override
    protected void onPrepareForRemoval() {
        super.onPrepareForRemoval();
        mCachedDevice.unregisterCallback(this);
    }
    public void onDeviceAttributesChanged(CachedBluetoothDevice cachedDevice) {
        setTitle(mCachedDevice.getName());
        setSummary(mCachedDevice.getSummary());
        mIsBusy = mCachedDevice.isBusy();
        notifyChanged();
        notifyHierarchyChanged();
    }
    @Override
    public boolean isEnabled() {
        setEnabled(true);
        return super.isEnabled() && !mIsBusy;
    }
    @Override
    protected void onBindView(View view) {
        if (null != findPreferenceInHierarchy("bt_checkbox")){
            setDependency("bt_checkbox");
        }
        super.onBindView(view);
        ImageView btClass = (ImageView) view.findViewById(R.id.btClass);
        btClass.setImageResource(mCachedDevice.getBtClassDrawable());
        btClass.setAlpha(isEnabled() ? 255 : sDimAlpha);
    }
    @Override
    public int compareTo(Preference another) {
        if (!(another instanceof BluetoothDevicePreference)) {
            return 1;
        }
        return mCachedDevice.compareTo(((BluetoothDevicePreference) another).mCachedDevice);
    }
}

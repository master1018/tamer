public class VpnEditor extends PreferenceActivity {
    private static final int MENU_SAVE = Menu.FIRST;
    private static final int MENU_CANCEL = Menu.FIRST + 1;
    private static final String KEY_PROFILE = "profile";
    private static final String KEY_ORIGINAL_PROFILE_NAME = "orig_profile_name";
    private VpnProfileEditor mProfileEditor;
    private boolean mAddingProfile;
    private byte[] mOriginalProfileData;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VpnProfile p = (VpnProfile) ((savedInstanceState == null)
                ? getIntent().getParcelableExtra(VpnSettings.KEY_VPN_PROFILE)
                : savedInstanceState.getParcelable(KEY_PROFILE));
        mProfileEditor = getEditor(p);
        mAddingProfile = TextUtils.isEmpty(p.getName());
        addPreferencesFromResource(R.xml.vpn_edit);
        initViewFor(p);
        Parcel parcel = Parcel.obtain();
        p.writeToParcel(parcel, 0);
        mOriginalProfileData = parcel.marshall();
    }
    @Override
    protected synchronized void onSaveInstanceState(Bundle outState) {
        if (mProfileEditor == null) return;
        outState.putParcelable(KEY_PROFILE, getProfile());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_SAVE, 0, R.string.vpn_menu_done)
            .setIcon(android.R.drawable.ic_menu_save);
        menu.add(0, MENU_CANCEL, 0,
                mAddingProfile ? R.string.vpn_menu_cancel
                               : R.string.vpn_menu_revert)
            .setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SAVE:
                if (validateAndSetResult()) finish();
                return true;
            case MENU_CANCEL:
                if (profileChanged()) {
                    showCancellationConfirmDialog();
                } else {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (validateAndSetResult()) finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void initViewFor(VpnProfile profile) {
        setTitle(profile);
        mProfileEditor.loadPreferencesTo(getPreferenceScreen());
    }
    private void setTitle(VpnProfile profile) {
        String formatString = mAddingProfile
                ? getString(R.string.vpn_edit_title_add)
                : getString(R.string.vpn_edit_title_edit);
        setTitle(String.format(formatString,
                profile.getType().getDisplayName()));
    }
    private boolean validateAndSetResult() {
        String errorMsg = mProfileEditor.validate();
        if (errorMsg != null) {
            Util.showErrorMessage(this, errorMsg);
            return false;
        }
        if (profileChanged()) setResult(getProfile());
        return true;
    }
    private void setResult(VpnProfile p) {
        Intent intent = new Intent(this, VpnSettings.class);
        intent.putExtra(VpnSettings.KEY_VPN_PROFILE, (Parcelable) p);
        setResult(RESULT_OK, intent);
    }
    private VpnProfileEditor getEditor(VpnProfile p) {
        switch (p.getType()) {
            case L2TP_IPSEC:
                return new L2tpIpsecEditor((L2tpIpsecProfile) p);
            case L2TP_IPSEC_PSK:
                return new L2tpIpsecPskEditor((L2tpIpsecPskProfile) p);
            case L2TP:
                return new L2tpEditor((L2tpProfile) p);
            case PPTP:
                return new PptpEditor((PptpProfile) p);
            default:
                return new VpnProfileEditor(p);
        }
    }
    private void showCancellationConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle(android.R.string.dialog_alert_title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(mAddingProfile
                        ? R.string.vpn_confirm_add_profile_cancellation
                        : R.string.vpn_confirm_edit_profile_cancellation)
                .setPositiveButton(R.string.vpn_yes_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int w) {
                                finish();
                            }
                        })
                .setNegativeButton(R.string.vpn_mistake_button, null)
                .show();
    }
    private VpnProfile getProfile() {
        return mProfileEditor.getProfile();
    }
    private boolean profileChanged() {
        Parcel newParcel = Parcel.obtain();
        getProfile().writeToParcel(newParcel, 0);
        byte[] newData = newParcel.marshall();
        if (mOriginalProfileData.length == newData.length) {
            for (int i = 0, n = mOriginalProfileData.length; i < n; i++) {
                if (mOriginalProfileData[i] != newData[i]) return true;
            }
            return false;
        }
        return true;
    }
}

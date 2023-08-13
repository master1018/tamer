public class AccountSelectionUtil {
    private static final String LOG_TAG = "AccountSelectionUtil";
    public static boolean mVCardShare = false;
    public static Uri mPath;
    public static class AccountSelectedListener
            implements DialogInterface.OnClickListener {
        final private Context mContext;
        final private int mResId;
        final protected List<Account> mAccountList;
        public AccountSelectedListener(Context context, List<Account> accountList, int resId) {
            if (accountList == null || accountList.size() == 0) {
                Log.e(LOG_TAG, "The size of Account list is 0.");
            }
            mContext = context;
            mAccountList = accountList;
            mResId = resId;
        }
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            doImport(mContext, mResId, mAccountList.get(which));
        }
    }
    public static Dialog getSelectAccountDialog(Context context, int resId) {
        return getSelectAccountDialog(context, resId, null, null);
    }
    public static Dialog getSelectAccountDialog(Context context, int resId,
            DialogInterface.OnClickListener onClickListener) {
        return getSelectAccountDialog(context, resId, onClickListener, null);
    }
    public static Dialog getSelectAccountDialog(Context context, int resId,
            DialogInterface.OnClickListener onClickListener,
            DialogInterface.OnCancelListener onCancelListener) {
        final Sources sources = Sources.getInstance(context);
        final List<Account> writableAccountList = sources.getAccounts(true);
        final Context dialogContext = new ContextThemeWrapper(
                context, android.R.style.Theme_Light);
        final LayoutInflater dialogInflater = (LayoutInflater)dialogContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ArrayAdapter<Account> accountAdapter =
            new ArrayAdapter<Account>(context, android.R.layout.simple_list_item_2,
                    writableAccountList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = dialogInflater.inflate(
                            android.R.layout.simple_list_item_2,
                            parent, false);
                }
                final TextView text1 =
                        (TextView)convertView.findViewById(android.R.id.text1);
                final TextView text2 =
                        (TextView)convertView.findViewById(android.R.id.text2);
                final Account account = this.getItem(position);
                final ContactsSource source =
                    sources.getInflatedSource(account.type,
                            ContactsSource.LEVEL_SUMMARY);
                final Context context = getContext();
                text1.setText(account.name);
                text2.setText(source.getDisplayLabel(context));
                return convertView;
            }
        };
        if (onClickListener == null) {
            AccountSelectedListener accountSelectedListener =
                new AccountSelectedListener(context, writableAccountList, resId);
            onClickListener = accountSelectedListener;
        }
        if (onCancelListener == null) {
            onCancelListener = new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                }
            };
        }
        return new AlertDialog.Builder(context)
            .setTitle(R.string.dialog_new_contact_account)
            .setSingleChoiceItems(accountAdapter, 0, onClickListener)
            .setOnCancelListener(onCancelListener)
            .create();
    }
    public static void doImport(Context context, int resId, Account account) {
        switch (resId) {
            case R.string.import_from_sim: {
                doImportFromSim(context, account);
                break;
            }
            case R.string.import_from_sdcard: {
                doImportFromSdCard(context, account);
                break;
            }
        }
    }
    public static void doImportFromSim(Context context, Account account) {
        if (account != null) {
            GoogleSource.createMyContactsIfNotExist(account, context);
        }
        Intent importIntent = new Intent(Intent.ACTION_VIEW);
        importIntent.setType("vnd.android.cursor.item/sim-contact");
        if (account != null) {
            importIntent.putExtra("account_name", account.name);
            importIntent.putExtra("account_type", account.type);
        }
        importIntent.setClassName("com.android.phone", "com.android.phone.SimContacts");
        context.startActivity(importIntent);
    }
    public static void doImportFromSdCard(Context context, Account account) {
        if (account != null) {
            GoogleSource.createMyContactsIfNotExist(account, context);
        }
        Intent importIntent = new Intent(context, ImportVCardActivity.class);
        if (account != null) {
            importIntent.putExtra("account_name", account.name);
            importIntent.putExtra("account_type", account.type);
        }
        if (mVCardShare) {
            importIntent.setAction(Intent.ACTION_VIEW);
            importIntent.setData(mPath);
        }
        mVCardShare = false;
        mPath = null;
        context.startActivity(importIntent);
    }
}

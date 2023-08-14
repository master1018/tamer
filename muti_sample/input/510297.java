public class ContactsSearchManager {
    public static final String ORIGINAL_ACTION_EXTRA_KEY = "originalAction";
    public static final String ORIGINAL_COMPONENT_EXTRA_KEY = "originalComponent";
    public static void startSearch(Activity context, String initialQuery) {
        context.startActivity(buildIntent(context, initialQuery));
    }
    public static void startSearchForResult(Activity context, String initialQuery,
            int requestCode) {
        context.startActivityForResult(buildIntent(context, initialQuery), requestCode);
    }
    private static Intent buildIntent(Activity context, String initialQuery) {
        Intent intent = new Intent();
        intent.setData(ContactsContract.Contacts.CONTENT_URI);
        intent.setAction(UI.FILTER_CONTACTS_ACTION);
        Intent originalIntent = context.getIntent();
        Bundle originalExtras = originalIntent.getExtras();
        if (originalExtras != null) {
            intent.putExtras(originalExtras);
        }
        intent.putExtra(UI.FILTER_TEXT_EXTRA_KEY, initialQuery);
        intent.putExtra(ORIGINAL_ACTION_EXTRA_KEY, originalIntent.getAction());
        intent.putExtra(ORIGINAL_COMPONENT_EXTRA_KEY, originalIntent.getComponent().getClassName());
        return intent;
    }
}

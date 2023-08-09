public class CalendarSyncEnablerTest extends AccountTestCase {
    private HashMap<Account, Boolean> origCalendarSyncStates = new HashMap<Account, Boolean>();
    private static final String EAT = Email.EXCHANGE_ACCOUNT_MANAGER_TYPE;
    public CalendarSyncEnablerTest() {
        super();
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        deleteTemporaryAccountManagerAccounts();
        for (Account account : AccountManager.get(getContext()).getAccounts()) {
            origCalendarSyncStates.put(account,
                    ContentResolver.getSyncAutomatically(account, Calendar.AUTHORITY));
        }
    }
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        deleteTemporaryAccountManagerAccounts();
        for (Account account : getExchangeAccounts()) {
            Boolean state = origCalendarSyncStates.get(account);
            if (state == null) continue; 
            ContentResolver.setSyncAutomatically(account, Calendar.AUTHORITY, state);
        }
    }
    public void testEnableEasCalendarSync() {
        final Account[] baseAccounts = getExchangeAccounts();
        String a1 = getTestAccountEmailAddress("1");
        String a2 = getTestAccountEmailAddress("2");
        CalendarSyncEnabler enabler = new CalendarSyncEnabler(getContext());
        createAccountManagerAccount(a1);
        String emailAddresses = enabler.enableEasCalendarSyncInternal();
        verifyCalendarSyncState();
        checkNotificationEmailAddresses(emailAddresses, baseAccounts, a1);
        deleteTemporaryAccountManagerAccounts();
        enabler = new CalendarSyncEnabler(getContext());
        createAccountManagerAccount(a1);
        createAccountManagerAccount(a2);
        emailAddresses = enabler.enableEasCalendarSyncInternal();
        verifyCalendarSyncState();
        checkNotificationEmailAddresses(emailAddresses, baseAccounts, a1, a2);
    }
    private static void checkNotificationEmailAddresses(String actual, Account[] baseAccounts,
            String... addedAddresses) {
        final String[] actualArray = TextUtils.split(actual, " ");
        Arrays.sort(actualArray);
        ArrayList<String> expected = new ArrayList<String>();
        for (Account account : baseAccounts) {
            expected.add(account.name);
        }
        for (String address : addedAddresses) {
            expected.add(address);
        }
        final String[] expectedArray = new String[expected.size()];
        expected.toArray(expectedArray);
        Arrays.sort(expectedArray);
        MoreAsserts.assertEquals(expectedArray, actualArray);
    }
    private void verifyCalendarSyncState() {
        for (Account account : AccountManager.get(getContext()).getAccounts()) {
            String message = "account=" + account.name + "(" + account.type + ")";
            boolean enabled = ContentResolver.getSyncAutomatically(account, Calendar.AUTHORITY);
            int syncable = ContentResolver.getIsSyncable(account, Calendar.AUTHORITY);
            if (EAT.equals(account.type)) {
            } else {
                assertEquals(message, origCalendarSyncStates.get(account), (Boolean) enabled);
            }
        }
    }
    public void testEnableEasCalendarSyncWithNoExchangeAccounts() {
        if (AccountManager.get(getContext()).getAccountsByType(EAT).length > 0) {
            Log.w(Email.LOG_TAG, "testEnableEasCalendarSyncWithNoExchangeAccounts skipped:"
                    + " It only runs when there's no Exchange account on the device.");
            return;
        }
        CalendarSyncEnabler enabler = new CalendarSyncEnabler(getContext());
        String emailAddresses = enabler.enableEasCalendarSyncInternal();
        verifyCalendarSyncState();
        assertEquals(0, emailAddresses.length());
    }
    public void testShowNotification() {
        CalendarSyncEnabler enabler = new CalendarSyncEnabler(getContext());
        enabler.showNotification("a@b.com");
        ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE))
                .cancel(MailService.NOTIFICATION_ID_EXCHANGE_CALENDAR_ADDED);
    }
}

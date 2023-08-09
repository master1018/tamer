public class NewChatActivity extends Activity {
    private static final int REQUEST_PICK_CONTACTS = RESULT_FIRST_USER + 1;
    ImApp mApp;
    ChatView mChatView;
    SimpleAlertHandler mHandler;
    private AlertDialog mSmileyDialog;
    private ChatSwitcher mChatSwitcher;
    private LayoutInflater mInflater;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.chat_view);
        mChatView = (ChatView) findViewById(R.id.chatView);
        mHandler = mChatView.mHandler;
        mInflater = LayoutInflater.from(this);
        mApp = ImApp.getApplication(this);
        mChatSwitcher = new ChatSwitcher(this, mHandler, mApp, mInflater, null);
        final Handler handler = new Handler();
        mApp.callWhenServiceConnected(handler, new Runnable() {
            public void run() {
                resolveIntent(getIntent());
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        mChatView.onResume();
    }
    @Override
    protected void onPause() {
        mChatView.onPause();
        super.onPause();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        resolveIntent(intent);
    }
    void resolveIntent(Intent intent) {
        if (requireOpenDashboardOnStart(intent)) {
            long providerId = intent.getLongExtra(EXTRA_INTENT_PROVIDER_ID, -1L);
            final long accountId = intent.getLongExtra(EXTRA_INTENT_ACCOUNT_ID, -1L);
            if (providerId == -1L || accountId == -1L) {
                finish();
            } else {
                mChatSwitcher.open();
            }
            return;
        }
        if (ACTION_MANAGE_SUBSCRIPTION.equals(intent.getAction())) {
            long providerId = intent.getLongExtra(EXTRA_INTENT_PROVIDER_ID, -1);
            String from = intent.getStringExtra(EXTRA_INTENT_FROM_ADDRESS);
            if ((providerId == -1) || (from == null)) {
                finish();
            } else {
                mChatView.bindSubscription(providerId, from);
            }
        } else {
            Uri data = intent.getData();
            String type = getContentResolver().getType(data);
            if (Imps.Chats.CONTENT_ITEM_TYPE.equals(type)) {
                mChatView.bindChat(ContentUris.parseId(data));
            } else if (Imps.Invitation.CONTENT_ITEM_TYPE.equals(type)) {
                mChatView.bindInvitation(ContentUris.parseId(data));
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_screen_menu, menu);
        long providerId = mChatView.getProviderId();
        BrandingResources brandingRes = mApp.getBrandingResource(providerId);
        menu.findItem(R.id.menu_view_friend_list).setTitle(
                brandingRes.getString(BrandingResourceIDs.STRING_MENU_CONTACT_LIST));
        menu.findItem(R.id.menu_switch_chats).setTitle(
                brandingRes.getString(BrandingResourceIDs.STRING_MENU_SWITCH_CHATS));
        menu.findItem(R.id.menu_insert_smiley).setTitle(
                brandingRes.getString(BrandingResourceIDs.STRING_MENU_INSERT_SMILEY));
        menu.findItem(R.id.menu_end_conversation).setTitle(
                brandingRes.getString(BrandingResourceIDs.STRING_MENU_END_CHAT));
        menu.findItem(R.id.menu_view_profile).setTitle(
                brandingRes.getString(BrandingResourceIDs.STRING_MENU_VIEW_PROFILE));
        menu.findItem(R.id.menu_block_contact).setTitle(
                brandingRes.getString(BrandingResourceIDs.STRING_MENU_BLOCK_CONTACT));
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_invite_contact).setVisible(false);
        ProviderDef provider = mApp.getProvider(mChatView.getProviderId());
        if ((provider != null) && Imps.ProviderNames.YAHOO.equals(provider.mName)) {
            if (Imps.Contacts.TYPE_TEMPORARY != mChatView.mType) {
                menu.findItem(R.id.menu_block_contact).setVisible(false);
            }
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_view_friend_list:
                finish();
                showRosterScreen();
                return true;
            case R.id.menu_insert_smiley:
                showSmileyDialog();
                return true;
            case R.id.menu_end_conversation:
                mChatView.closeChatSession();
                return true;
            case R.id.menu_switch_chats:
                if (mChatSwitcher.isOpen()) {
                    mChatSwitcher.close();
                } else {
                    mChatSwitcher.open();
                }
                return true;
            case R.id.menu_invite_contact:
                startContactPicker();
                return true;
            case R.id.menu_view_profile:
                mChatView.viewProfile();
                return true;
            case R.id.menu_block_contact:
                mChatView.blockContact();
                return true;
            case R.id.menu_prev_chat:
                switchChat(-1);
                return true;
            case R.id.menu_next_chat:
                switchChat(1);
                return true;
            case R.id.menu_quick_switch_0:
            case R.id.menu_quick_switch_1:
            case R.id.menu_quick_switch_2:
            case R.id.menu_quick_switch_3:
            case R.id.menu_quick_switch_4:
            case R.id.menu_quick_switch_5:
            case R.id.menu_quick_switch_6:
            case R.id.menu_quick_switch_7:
            case R.id.menu_quick_switch_8:
            case R.id.menu_quick_switch_9:
                mChatSwitcher.handleShortcut(item.getAlphabeticShortcut());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            mChatView.closeChatSessionIfInactive();
        }
        return super.dispatchKeyEvent(event);
    }
    private boolean requireOpenDashboardOnStart(Intent intent) {
        return intent.getBooleanExtra(EXTRA_INTENT_SHOW_MULTIPLE, false);
    }
    private void showRosterScreen() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(this, ContactListActivity.class);
        intent.putExtra(EXTRA_INTENT_ACCOUNT_ID, mChatView.getAccountId());
        startActivity(intent);
    }
    private void showSmileyDialog() {
        if (mSmileyDialog == null) {
            long providerId = mChatView.getProviderId();
            final BrandingResources brandingRes = mApp.getBrandingResource(providerId);
            int[] icons = brandingRes.getSmileyIcons();
            String[] names = brandingRes.getStringArray(
                    BrandingResourceIDs.STRING_ARRAY_SMILEY_NAMES);
            final String[] texts = brandingRes.getStringArray(
                    BrandingResourceIDs.STRING_ARRAY_SMILEY_TEXTS);
            final int N = names.length;
            List<Map<String, ?>> entries = new ArrayList<Map<String, ?>>();
            for (int i = 0; i < N; i++) {
                boolean added = false;
                for (int j = 0; j < i; j++) {
                    if (icons[i] == icons[j]) {
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    HashMap<String, Object> entry = new HashMap<String, Object>();
                    entry. put("icon", icons[i]);
                    entry. put("name", names[i]);
                    entry.put("text", texts[i]);
                    entries.add(entry);
                }
            }
            final SimpleAdapter a = new SimpleAdapter(
                    this,
                    entries,
                    R.layout.smiley_menu_item,
                    new String[] {"icon", "name", "text"},
                    new int[] {R.id.smiley_icon, R.id.smiley_name, R.id.smiley_text});
            SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder() {
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if (view instanceof ImageView) {
                        Drawable img = brandingRes.getSmileyIcon((Integer)data);
                        ((ImageView)view).setImageDrawable(img);
                        return true;
                    }
                    return false;
                }
            };
            a.setViewBinder(viewBinder);
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle(brandingRes.getString(
                    BrandingResourceIDs.STRING_MENU_INSERT_SMILEY));
            b.setCancelable(true);
            b.setAdapter(a, new DialogInterface.OnClickListener() {
                public final void onClick(DialogInterface dialog, int which) {
                    HashMap<String, Object> item = (HashMap<String, Object>) a.getItem(which);
                    mChatView.insertSmiley((String)item.get("text"));
                }
            });
            mSmileyDialog = b.create();
        }
        mSmileyDialog.show();
    }
    private void switchChat(int delta) {
        long providerId = mChatView.getProviderId();
        long accountId = mChatView.getAccountId();
        String contact = mChatView.getUserName();
        mChatSwitcher.rotateChat(delta, contact, accountId, providerId);
    }
    private void startContactPicker() {
        Uri.Builder builder = Imps.Contacts.CONTENT_URI_ONLINE_CONTACTS_BY.buildUpon();
        ContentUris.appendId(builder, mChatView.getProviderId());
        ContentUris.appendId(builder, mChatView.getAccountId());
        Uri data = builder.build();
        try {
            Intent i = new Intent(Intent.ACTION_PICK, data);
            i.putExtra(ContactsPickerActivity.EXTRA_EXCLUDED_CONTACTS,
                    mChatView.getCurrentChatSession().getPariticipants());
            startActivityForResult(i, REQUEST_PICK_CONTACTS);
        } catch (RemoteException e) {
            mHandler.showServiceErrorAlert();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_CONTACTS) {
                String username = data.getStringExtra(
                        ContactsPickerActivity.EXTRA_RESULT_USERNAME);
                try {
                    IChatSession chatSession = mChatView.getCurrentChatSession();
                    if (chatSession.isGroupChatSession()) {
                        chatSession.inviteContact(username);
                        showInvitationHasSent(username);
                    } else {
                        chatSession.convertToGroupChat();
                        new ContactInvitor(chatSession, username).start();
                    }
                } catch (RemoteException e) {
                    mHandler.showServiceErrorAlert();
                }
            }
        }
    }
    void showInvitationHasSent(String contact) {
        Toast.makeText(NewChatActivity.this,
                getString(R.string.invitation_sent_prompt, contact),
                Toast.LENGTH_SHORT).show();
    }
    private class ContactInvitor extends ChatListenerAdapter {
        private final IChatSession mChatSession;
        String mContact;
        public ContactInvitor(IChatSession session, String data) {
            mChatSession = session;
            mContact = data;
        }
        @Override
        public void onConvertedToGroupChat(IChatSession ses) {
            try {
                final long chatId = mChatSession.getId();
                mChatSession.inviteContact(mContact);
                mHandler.post(new Runnable(){
                    public void run() {
                        mChatView.bindChat(chatId);
                        showInvitationHasSent(mContact);
                    }
                });
                mChatSession.unregisterChatListener(this);
            } catch (RemoteException e) {
                mHandler.showServiceErrorAlert();
            }
        }
        public void start() throws RemoteException {
            mChatSession.registerChatListener(this);
        }
    }
}

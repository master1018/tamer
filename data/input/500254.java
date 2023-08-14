public class MessageComposeInstrumentationTests
        extends ActivityInstrumentationTestCase2<MessageCompose> {
    private MultiAutoCompleteTextView mToView;
    private MultiAutoCompleteTextView mCcView;
    private EditText mSubjectView;
    private EditText mMessageView;
    private long mCreatedAccountId = -1;
    private String mSignature;
    private static final String SENDER = "sender@android.com";
    private static final String REPLYTO = "replyto@android.com";
    private static final String RECIPIENT_TO = "recipient-to@android.com";
    private static final String RECIPIENT_CC = "recipient-cc@android.com";
    private static final String RECIPIENT_BCC = "recipient-bcc@android.com";
    private static final String SUBJECT = "This is the subject";
    private static final String BODY = "This is the body.  This is also the body.";
    private static final String REPLY_BODY_SHORT = "\n\n" + SENDER + " wrote:\n\n";
    private static final String REPLY_BODY = REPLY_BODY_SHORT + ">" + BODY;
    private static final String SIGNATURE = "signature";
    private static final String FROM = "Fred From <from@google.com>";
    private static final String TO1 = "First To <first.to@google.com>";
    private static final String TO2 = "Second To <second.to@google.com>";
    private static final String TO3 = "CopyFirst Cc <first.cc@google.com>";
    private static final String CC1 = "First Cc <first.cc@google.com>";
    private static final String CC2 = "Second Cc <second.cc@google.com>";
    private static final String CC3 = "Third Cc <third.cc@google.com>";
    private static final String CC4 = "CopySecond To <second.to@google.com>";
    private static final String UTF16_SENDER =
            "\u3042\u3044\u3046 \u3048\u304A <sender@android.com>";
    private static final String UTF16_REPLYTO =
            "\u3042\u3044\u3046\u3048\u304A <replyto@android.com>";
    private static final String UTF16_RECIPIENT_TO =
            "\"\u3042\u3044\u3046,\u3048\u304A\" <recipient-to@android.com>";
    private static final String UTF16_RECIPIENT_CC =
            "\u30A2\u30AB \u30B5\u30BF\u30CA <recipient-cc@android.com>";
    private static final String UTF16_RECIPIENT_BCC =
            "\"\u30A2\u30AB,\u30B5\u30BF\u30CA\" <recipient-bcc@android.com>";
    private static final String UTF16_SUBJECT = "\u304A\u5BFF\u53F8\u306B\u3059\u308B\uFF1F";
    private static final String UTF16_BODY = "\u65E5\u672C\u8A9E\u306E\u6587\u7AE0";
    private static final String UTF32_SENDER =
            "\uD834\uDF01\uD834\uDF46 \uD834\uDF22 <sender@android.com>";
    private static final String UTF32_REPLYTO =
            "\uD834\uDF01\uD834\uDF46\uD834\uDF22 <replyto@android.com>";
    private static final String UTF32_RECIPIENT_TO =
            "\"\uD834\uDF01\uD834\uDF46,\uD834\uDF22\" <recipient-to@android.com>";
    private static final String UTF32_RECIPIENT_CC =
            "\uD834\uDF22 \uD834\uDF01\uD834\uDF46 <recipient-cc@android.com>";
    private static final String UTF32_RECIPIENT_BCC =
            "\"\uD834\uDF22,\uD834\uDF01\uD834\uDF46\" <recipient-bcc@android.com>";
    private static final String UTF32_SUBJECT = "\uD834\uDF01\uD834\uDF46";
    private static final String UTF32_BODY = "\uD834\uDF01\uD834\uDF46";
    private static final String ACTION_REPLY = "com.android.email.intent.action.REPLY";
    private static final String ACTION_REPLY_ALL = "com.android.email.intent.action.REPLY_ALL";
    private static final String ACTION_FORWARD = "com.android.email.intent.action.FORWARD";
    private static final String ACTION_EDIT_DRAFT = "com.android.email.intent.action.EDIT_DRAFT";
    public MessageComposeInstrumentationTests() {
        super(MessageCompose.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Context context = getInstrumentation().getTargetContext();
        long accountId = Account.getDefaultAccountId(context);
        if (accountId == -1) {
            Account account = new Account();
            account.mSenderName = "Bob Sender";
            account.mEmailAddress = "bob@sender.com";
            account.save(context);
            accountId = account.mId;
            mCreatedAccountId = accountId;
        }
        Account account = Account.restoreAccountWithId(context, accountId);
        mSignature = account.getSignature();
        Email.setServicesEnabled(context);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        setActivityIntent(intent);
        final MessageCompose a = getActivity();
        mToView = (MultiAutoCompleteTextView) a.findViewById(R.id.to);
        mCcView = (MultiAutoCompleteTextView) a.findViewById(R.id.cc);
        mSubjectView = (EditText) a.findViewById(R.id.subject);
        mMessageView = (EditText) a.findViewById(R.id.message_content);
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Context context = getInstrumentation().getTargetContext();
        if (mCreatedAccountId > -1) {
            context.getContentResolver().delete(
                    ContentUris.withAppendedId(Account.CONTENT_URI, mCreatedAccountId), null, null);
        }
    }
    public void testPreconditions() {
        assertNotNull(mToView);
        assertEquals(0, mToView.length());
        assertNotNull(mSubjectView);
        assertEquals(0, mSubjectView.length());
        assertNotNull(mMessageView);
        assertEquals(0, mMessageView.length());
    }
    public void testProcessSourceMessageReply() throws MessagingException, Throwable {
        final Message message = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        Intent intent = new Intent(ACTION_REPLY);
        final MessageCompose a = getActivity();
        a.setIntent(intent);
        runTestOnUiThread(new Runnable() {
            public void run() {
                a.processSourceMessage(message, null);
                checkFields(SENDER + ", ", null, null, "Re: " + SUBJECT, null, null);
                checkFocused(mMessageView);
            }
        });
        message.mFrom = null;
        message.mReplyTo = Address.parseAndPack(REPLYTO);
        runTestOnUiThread(new Runnable() {
            public void run() {
                resetViews();
                a.processSourceMessage(message, null);
                checkFields(REPLYTO + ", ", null, null, "Re: " + SUBJECT, null, null);
                checkFocused(mMessageView);
            }
        });
    }
    public void testProcessSourceMessageReplyUtf16() throws MessagingException, Throwable {
        final Message message = buildTestMessage(UTF16_RECIPIENT_TO, UTF16_SENDER,
                UTF16_SUBJECT, UTF16_BODY);
        Intent intent = new Intent(ACTION_REPLY);
        final MessageCompose a = getActivity();
        a.setIntent(intent);
        runTestOnUiThread(new Runnable() {
            public void run() {
                a.processSourceMessage(message, null);
                checkFields(UTF16_SENDER + ", ", null, null, "Re: " + UTF16_SUBJECT, null, null);
                checkFocused(mMessageView);
            }
        });
        message.mFrom = null;
        message.mReplyTo = Address.parseAndPack(UTF16_REPLYTO);
        runTestOnUiThread(new Runnable() {
            public void run() {
                resetViews();
                a.processSourceMessage(message, null);
                checkFields(UTF16_REPLYTO + ", ", null, null, "Re: " + UTF16_SUBJECT, null, null);
                checkFocused(mMessageView);
            }
        });
    }
    public void testProcessSourceMessageReplyUtf32() throws MessagingException, Throwable {
        final Message message = buildTestMessage(UTF32_RECIPIENT_TO, UTF32_SENDER,
                UTF32_SUBJECT, UTF32_BODY);
        Intent intent = new Intent(ACTION_REPLY);
        final MessageCompose a = getActivity();
        a.setIntent(intent);
        runTestOnUiThread(new Runnable() {
            public void run() {
                a.processSourceMessage(message, null);
                checkFields(UTF32_SENDER + ", ", null, null, "Re: " + UTF32_SUBJECT, null, null);
                checkFocused(mMessageView);
            }
        });
        message.mFrom = null;
        message.mReplyTo = Address.parseAndPack(UTF32_REPLYTO);
        runTestOnUiThread(new Runnable() {
            public void run() {
                resetViews();
                a.processSourceMessage(message, null);
                checkFields(UTF32_REPLYTO + ", ", null, null, "Re: " + UTF32_SUBJECT, null, null);
                checkFocused(mMessageView);
            }
        });
    }
    public void testProcessSourceMessageForward() throws MessagingException, Throwable {
        final Message message = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        Intent intent = new Intent(ACTION_FORWARD);
        final MessageCompose a = getActivity();
        a.setIntent(intent);
        runTestOnUiThread(new Runnable() {
            public void run() {
                a.processSourceMessage(message, null);
                checkFields(null, null, null, "Fwd: " + SUBJECT, null, null);
                checkFocused(mToView);
            }
        });
    }
    public void testProcessSourceMessageDraft() throws MessagingException, Throwable {
        final Message message = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        Intent intent = new Intent(ACTION_EDIT_DRAFT);
        final MessageCompose a = getActivity();
        a.setIntent(intent);
        runTestOnUiThread(new Runnable() {
            public void run() {
                a.processSourceMessage(message, null);
                checkFields(RECIPIENT_TO + ", ", null, null, SUBJECT, BODY, null);
                checkFocused(mMessageView);
            }
        });
        message.mSubject = "";
        runTestOnUiThread(new Runnable() {
            public void run() {
                resetViews();
                a.processSourceMessage(message, null);
                checkFields(RECIPIENT_TO + ", ", null, null, null, BODY, null);
                checkFocused(mSubjectView);
            }
        });
    }
    public void testProcessSourceMessageDraftWithUtf16() throws MessagingException, Throwable {
        final Message message = buildTestMessage(UTF16_RECIPIENT_TO, UTF16_SENDER,
                UTF16_SUBJECT, UTF16_BODY);
        Intent intent = new Intent(ACTION_EDIT_DRAFT);
        final MessageCompose a = getActivity();
        a.setIntent(intent);
        runTestOnUiThread(new Runnable() {
            public void run() {
                a.processSourceMessage(message, null);
                checkFields(UTF16_RECIPIENT_TO + ", ",
                        null, null, UTF16_SUBJECT, UTF16_BODY, null);
                checkFocused(mMessageView);
            }
        });
        message.mSubject = "";
        runTestOnUiThread(new Runnable() {
            public void run() {
                resetViews();
                a.processSourceMessage(message, null);
                checkFields(UTF16_RECIPIENT_TO + ", ", null, null, null, UTF16_BODY, null);
                checkFocused(mSubjectView);
            }
        });
    }
    public void testProcessSourceMessageDraftWithUtf32() throws MessagingException, Throwable {
        final Message message = buildTestMessage(UTF32_RECIPIENT_TO, UTF32_SENDER,
                UTF32_SUBJECT, UTF32_BODY);
        Intent intent = new Intent(ACTION_EDIT_DRAFT);
        final MessageCompose a = getActivity();
        a.setIntent(intent);
        runTestOnUiThread(new Runnable() {
            public void run() {
                a.processSourceMessage(message, null);
                checkFields(UTF32_RECIPIENT_TO + ", ",
                        null, null, UTF32_SUBJECT, UTF32_BODY, null);
                checkFocused(mMessageView);
            }
        });
        message.mSubject = "";
        runTestOnUiThread(new Runnable() {
            public void run() {
                resetViews();
                a.processSourceMessage(message, null);
                checkFields(UTF32_RECIPIENT_TO + ", ", null, null, null, UTF32_BODY, null);
                checkFocused(mSubjectView);
            }
        });
    }
    public void testReplyAddresses() throws Throwable {
        final MessageCompose a = getActivity();
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        Message msg = new Message();
        final Account account = new Account();
        msg.mFrom = Address.parseAndPack(FROM);
        msg.mTo = Address.parseAndPack(TO1 + ',' + TO2);
        msg.mCc = Address.parseAndPack(CC1 + ',' + CC2 + ',' + CC3);
        final Message message = msg;
        account.mEmailAddress = "FiRsT.tO@gOoGlE.cOm";
        runTestOnUiThread(new Runnable() {
            public void run() {
                a.initFromIntent(intent);
                a.setupAddressViews(message, account, mToView, mCcView, false);
                assertEquals("", mCcView.getText().toString());
                String result = Address.parseAndPack(mToView.getText().toString());
                String expected = Address.parseAndPack(FROM);
                assertEquals(expected, result);
            }
        });
    }
    public void testReplyAllAddresses1() throws Throwable {
        final MessageCompose a = getActivity();
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        Message msg = new Message();
        final Account account = new Account();
        msg.mFrom = Address.parseAndPack(FROM);
        msg.mTo = Address.parseAndPack(TO1 + ',' + TO2);
        msg.mCc = Address.parseAndPack(CC1 + ',' + CC2 + ',' + CC3);
        final Message message = msg;
        account.mEmailAddress = "FiRsT.tO@gOoGlE.cOm";
        runTestOnUiThread(new Runnable() {
            public void run() {
                a.initFromIntent(intent);
                a.setupAddressViews(message, account, mToView, mCcView, true);
                String result = Address.parseAndPack(mToView.getText().toString());
                String expected = Address.parseAndPack(FROM + ',' + TO2);
                assertEquals(expected, result);
                result = Address.parseAndPack(mCcView.getText().toString());
                expected = Address.parseAndPack(CC1 + ',' + CC2 + ',' + CC3);
                assertEquals(expected, result);
            }
        });
    }
    public void testReplyAllAddresses2() throws Throwable {
        final MessageCompose a = getActivity();
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        Message msg = new Message();
        final Account account = new Account();
        msg.mFrom = Address.parseAndPack(FROM);
        msg.mTo = Address.parseAndPack(TO1 + ',' + TO2);
        msg.mCc = Address.parseAndPack(CC1 + ',' + CC2 + ',' + CC3);
        final Message message = msg;
        account.mEmailAddress = "sEcOnD.cC@gOoGlE.cOm";
        runTestOnUiThread(new Runnable() {
            public void run() {
                a.initFromIntent(intent);
                a.setupAddressViews(message, account, mToView, mCcView, true);
                String result = Address.parseAndPack(mToView.getText().toString());
                String expected = Address.parseAndPack(FROM + ',' + TO1 + ',' + TO2);
                assertEquals(expected, result);
                result = Address.parseAndPack(mCcView.getText().toString());
                expected = Address.parseAndPack(CC1 + ',' + CC3);
                assertEquals(expected, result);
            }
        });
    }
    public void testReplyAllAddresses3() throws Throwable {
        final MessageCompose a = getActivity();
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        Message msg = new Message();
        final Account account = new Account();
        msg.mFrom = Address.parseAndPack(FROM);
        msg.mTo = Address.parseAndPack(TO1 + ',' + TO2 + ',' + TO3);
        msg.mCc = Address.parseAndPack(CC1 + ',' + CC2 + ',' + CC3 + ',' + CC4);
        final Message message = msg;
        account.mEmailAddress = "sEcOnD.cC@gOoGlE.cOm";
        runTestOnUiThread(new Runnable() {
            public void run() {
                a.initFromIntent(intent);
                a.setupAddressViews(message, account, mToView, mCcView, true);
                String result = Address.parseAndPack(mToView.getText().toString());
                String expected = Address.parseAndPack(FROM + ',' + TO1 + ',' + TO2 + ',' + TO3);
                assertEquals(expected, result);
                result = Address.parseAndPack(mCcView.getText().toString());
                expected = Address.parseAndPack(CC3);
                assertEquals(expected, result);
            }
        });
    }
    public void testIntentHeaderExtras() throws MessagingException, Throwable {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { RECIPIENT_TO });
        intent.putExtra(Intent.EXTRA_CC, new String[] { RECIPIENT_CC });
        intent.putExtra(Intent.EXTRA_BCC, new String[] { RECIPIENT_BCC });
        intent.putExtra(Intent.EXTRA_SUBJECT, SUBJECT);
        final MessageCompose a = getActivity();
        final Intent i2 = new Intent(intent);
        runTestOnUiThread(new Runnable() {
            public void run() {
                a.initFromIntent(i2);
                checkFields(RECIPIENT_TO + ", ", RECIPIENT_CC, RECIPIENT_BCC, SUBJECT, null, null);
                checkFocused(mMessageView);
            }
        });
    }
    public void testIntentHeaderExtrasWithUtf16() throws MessagingException, Throwable {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { UTF16_RECIPIENT_TO });
        intent.putExtra(Intent.EXTRA_CC, new String[] { UTF16_RECIPIENT_CC });
        intent.putExtra(Intent.EXTRA_BCC, new String[] { UTF16_RECIPIENT_BCC });
        intent.putExtra(Intent.EXTRA_SUBJECT, UTF16_SUBJECT);
        final MessageCompose a = getActivity();
        final Intent i2 = new Intent(intent);
        runTestOnUiThread(new Runnable() {
            public void run() {
                a.initFromIntent(i2);
                checkFields(UTF16_RECIPIENT_TO + ", ",
                        UTF16_RECIPIENT_CC, UTF16_RECIPIENT_BCC, UTF16_SUBJECT, null, null);
                checkFocused(mMessageView);
            }
        });
    }
    public void testIntentHeaderExtrasWithUtf32() throws MessagingException, Throwable {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { UTF32_RECIPIENT_TO });
        intent.putExtra(Intent.EXTRA_CC, new String[] { UTF32_RECIPIENT_CC });
        intent.putExtra(Intent.EXTRA_BCC, new String[] { UTF32_RECIPIENT_BCC });
        intent.putExtra(Intent.EXTRA_SUBJECT, UTF32_SUBJECT);
        final MessageCompose a = getActivity();
        final Intent i2 = new Intent(intent);
        runTestOnUiThread(new Runnable() {
            public void run() {
                a.initFromIntent(i2);
                checkFields(UTF32_RECIPIENT_TO + ", ",
                        UTF32_RECIPIENT_CC, UTF32_RECIPIENT_BCC, UTF32_SUBJECT, null, null);
                checkFocused(mMessageView);
            }
        });
    }
    public void testIntentSendPlainText() throws MessagingException, Throwable {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, BODY);
        final MessageCompose a = getActivity();
        final Intent i2 = new Intent(intent);
        runTestOnUiThread(new Runnable() {
            public void run() {
                a.initFromIntent(i2);
                checkFields(null, null, null, null, BODY, null);
                checkFocused(mToView);
            }
        });
    }
    public void testBrowserMailToIntent() throws MessagingException, Throwable {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("mailto:" + RECIPIENT_TO + "?subject=This%20is%20the%20subject");
        intent.setData(uri);
        final MessageCompose a = getActivity();
        final Intent i2 = new Intent(intent);
        runTestOnUiThread(new Runnable() {
            public void run() {
                a.initFromIntent(i2);
                checkFields(RECIPIENT_TO + ", ", null, null, "This is the subject", null, null);
                checkFocused(mMessageView);
            }
        });
    }
    private void checkFields(String to, String cc, String bcc, String subject, String content,
            String signature) {
        String toText = mToView.getText().toString();
        if (to == null) {
            assertEquals(0, toText.length());
        } else {
            assertEquals(to, toText);
        }
        String subjectText = mSubjectView.getText().toString();
        if (subject == null) {
            assertEquals(0, subjectText.length());
        } else {
            assertEquals(subject, subjectText);
        }
        String contentText = mMessageView.getText().toString();
        if (content == null) {
            assertEquals(0, contentText.length());
        } else {
            if (signature != null) {
                int textLength = content.length();
                if (textLength == 0 || content.charAt(textLength - 1) != '\n') {
                    content += "\n";
                }
                content += signature;
            }
            assertEquals(content, contentText);
        }
    }
    private void checkFocused(View focused) {
        assertEquals(focused == mToView, mToView.isFocused());
        assertEquals(focused == mSubjectView, mSubjectView.isFocused());
        assertEquals(focused == mMessageView, mMessageView.isFocused());
    }
    private void resetViews() {
        mToView.setText(null);
        mSubjectView.setText(null);
        mMessageView.setText(null);
    }
    private Message buildTestMessage(String to, String sender, String subject, String content)
            throws MessagingException {
        Message message = new Message();
        if (to != null) {
            message.mTo = Address.parseAndPack(to);
        }
        if (sender != null) {
            Address[] addresses = Address.parse(sender);
            assertTrue("from address", addresses.length > 0);
            message.mFrom = addresses[0].pack();
        }
        message.mSubject = subject;
        if (content != null) {
            message.mText = content;
        }
        return message;
    }
    @UiThreadTest
    public void testAddressTextView() {
        MessageCompose messageCompose = getActivity();
        mToView.setValidator(new EmailAddressValidator());
        mToView.setText("foo");
        mToView.performValidation();
        assertNotNull(mToView.getError());
        assertFalse(messageCompose.isAddressAllValid());
        assertEquals("foo, ", mToView.getText().toString());
        mToView.setText("a@b.c");
        mToView.performValidation();
        assertNull(mToView.getError());
        assertTrue(messageCompose.isAddressAllValid());
        mToView.setText("a@b.c, foo");
        mToView.performValidation();
        assertNotNull(mToView.getError());
        assertFalse(messageCompose.isAddressAllValid());
        assertEquals("a@b.c, foo, ", mToView.getText().toString());
    }
    public void testSetInitialComposeTextAndSelection() throws MessagingException, Throwable {
        final Message msg = buildTestMessage(null, null, null, BODY);
        final Intent intent = new Intent(ACTION_EDIT_DRAFT);
        final Account account = new Account();
        final MessageCompose a = getActivity();
        a.setIntent(intent);
        runTestOnUiThread(new Runnable() {
            public void run() {
                resetViews();
                a.setInitialComposeText(BODY, SIGNATURE);
                checkFields(null, null, null, null, BODY, SIGNATURE);
                a.setMessageContentSelection(SIGNATURE);
                assertEquals(BODY.length(), mMessageView.getSelectionStart());
            }
        });
        runTestOnUiThread(new Runnable() {
            public void run() {
                resetViews();
                a.setInitialComposeText(BODY, null);
                checkFields(null, null, null, null, BODY, null);
                a.setMessageContentSelection(null);
                assertEquals(BODY.length(), mMessageView.getSelectionStart());
            }
        });
        runTestOnUiThread(new Runnable() {
            public void run() {
                resetViews();
                final String body2 = BODY + "\n\na\n\n";
                a.setInitialComposeText(body2, SIGNATURE);
                checkFields(null, null, null, null, body2, SIGNATURE);
                a.setMessageContentSelection(SIGNATURE);
                assertEquals(BODY.length() + 3, mMessageView.getSelectionStart());
            }
        });
    }
    public void testCommaInserting() throws Throwable {
        checkCommaInsert("a", "", false);
        checkCommaInsert("a@", "", false);
        checkCommaInsert("a@b", "", false);
        checkCommaInsert("a@b.", "", true); 
        checkCommaInsert("a@b.c", "", true);
        checkCommaInsert("me@foo.com, you", " they@bar.com", false);
        checkCommaInsert("me@foo.com, you@", "they@bar.com", false);
        checkCommaInsert("me@foo.com, you@bar", " they@bar.com", false);
        checkCommaInsert("me@foo.com, you@bar.", " they@bar.com", true); 
        checkCommaInsert("me@foo.com, you@bar.com", " they@bar.com", true);
        checkCommaInsert("me.myself@foo", "", false);
        checkCommaInsert("me.myself@foo.com", "", true);
        checkCommaInsert("me@foo.co.uk", "", true);
        checkCommaInsert("a@b.c,", "", false);
        checkCommaInsert("me@foo.com, you@bar.com,", " they@bar.com", false);
        checkCommaInsert("me.myself@foo.com,", "", false);
        checkCommaInsert("me@foo.co.uk,", "", false);
    }
    private void checkCommaInsert(final String before, final String after, boolean expectComma)
            throws Throwable {
        String expect = new String(before + (expectComma ? ", " : " ") + after);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mToView.setText(before + after);
                mToView.setSelection(before.length());
            }
        });
        getInstrumentation().sendStringSync(" ");
        String result = mToView.getText().toString();
        assertEquals(expect, result);
     }
}

public class ProviderTestUtils extends Assert {
    private ProviderTestUtils() {
    }
    public static Account setupAccount(String name, boolean saveIt, Context context) {
        Account account = new Account();
        account.mDisplayName = name;
        account.mEmailAddress = name + "@android.com";
        account.mSyncKey = "sync-key-" + name;
        account.mSyncLookback = 1;
        account.mSyncInterval = EmailContent.Account.CHECK_INTERVAL_NEVER;
        account.mHostAuthKeyRecv = 0;
        account.mHostAuthKeySend = 0;
        account.mFlags = 4;
        account.mIsDefault = true;
        account.mCompatibilityUuid = "test-uid-" + name;
        account.mSenderName = name;
        account.mRingtoneUri = "content:
        account.mProtocolVersion = "2.5" + name;
        account.mNewMessageCount = 5 + name.length();
        account.mSecurityFlags = 7;
        account.mSecuritySyncKey = "sec-sync-key-" + name;
        account.mSignature = "signature-" + name;
        if (saveIt) {
            account.save(context);
        }
        return account;
    }
    public static HostAuth setupHostAuth(String name, long accountId, boolean saveIt,
            Context context) {
        return setupHostAuth("protocol", name, accountId, saveIt, context);
    }
    public static HostAuth setupHostAuth(String protocol, String name, long accountId,
            boolean saveIt, Context context) {
        HostAuth hostAuth = new HostAuth();
        hostAuth.mProtocol = protocol + "-" + name;
        hostAuth.mAddress = "address-" + name;
        hostAuth.mPort = 100;
        hostAuth.mFlags = 200;
        hostAuth.mLogin = "login-" + name;
        hostAuth.mPassword = "password-" + name;
        hostAuth.mDomain = "domain-" + name;
        hostAuth.mAccountKey = accountId;
        if (saveIt) {
            hostAuth.save(context);
        }
        return hostAuth;
    }
    public static Mailbox setupMailbox(String name, long accountId, boolean saveIt,
            Context context) {
        return setupMailbox(name, accountId, saveIt, context, Mailbox.TYPE_MAIL);
    }
    public static Mailbox setupMailbox(String name, long accountId, boolean saveIt,
            Context context, int type) {
        Mailbox box = new Mailbox();
        box.mDisplayName = name;
        box.mServerId = "serverid-" + name;
        box.mParentServerId = "parent-serverid-" + name;
        box.mAccountKey = accountId;
        box.mType = type;
        box.mDelimiter = 1;
        box.mSyncKey = "sync-key-" + name;
        box.mSyncLookback = 2;
        box.mSyncInterval = EmailContent.Account.CHECK_INTERVAL_NEVER;
        box.mSyncTime = 3;
        box.mUnreadCount = 0;
        box.mFlagVisible = true;
        box.mFlags = 5;
        box.mVisibleLimit = 6;
        if (saveIt) {
            box.save(context);
        }
        return box;
    }
    public static Message setupMessage(String name, long accountId, long mailboxId,
            boolean addBody, boolean saveIt, Context context) {
        Message message = new Message();
        message.mDisplayName = name;
        message.mTimeStamp = 100 + name.length();
        message.mSubject = "subject " + name;
        message.mFlagRead = true;
        message.mFlagLoaded = Message.FLAG_LOADED_UNLOADED;
        message.mFlagFavorite = true;
        message.mFlagAttachment = true;
        message.mFlags = 0;
        message.mServerId = "serverid " + name;
        message.mServerTimeStamp = 300 + name.length();
        message.mClientId = "clientid " + name;
        message.mMessageId = "messageid " + name;
        message.mMailboxKey = mailboxId;
        message.mAccountKey = accountId;
        message.mFrom = "from " + name;
        message.mTo = "to " + name;
        message.mCc = "cc " + name;
        message.mBcc = "bcc " + name;
        message.mReplyTo = "replyto " + name;
        message.mMeetingInfo = "123" + accountId + mailboxId + name.length();
        if (addBody) {
            message.mText = "body text " + name;
            message.mHtml = "body html " + name;
            message.mTextReply = "reply text " + name;
            message.mHtmlReply = "reply html " + name;
            message.mSourceKey = 400 + name.length();
            message.mIntroText = "intro text " + name;
        }
        if (saveIt) {
            message.save(context);
        }
        return message;
    }
    public static Attachment setupAttachment(long messageId, String fileName, long length,
            boolean saveIt, Context context) {
        Attachment att = new Attachment();
        att.mSize = length;
        att.mFileName = fileName;
        att.mContentId = "contentId " + fileName;
        att.mContentUri = "contentUri " + fileName;
        att.mMessageKey = messageId;
        att.mMimeType = "mimeType " + fileName;
        att.mLocation = "location " + fileName;
        att.mEncoding = "encoding " + fileName;
        att.mContent = "content " + fileName;
        att.mFlags = 0;
        att.mContentBytes = Utility.toUtf8("content " + fileName);
        if (saveIt) {
            att.save(context);
        }
        return att;
    }
    private static void assertEmailContentEqual(String caller, EmailContent expect,
            EmailContent actual) {
        if (expect == actual) {
            return;
        }
        assertEquals(caller + " mId", expect.mId, actual.mId);
        assertEquals(caller + " mBaseUri", expect.mBaseUri, actual.mBaseUri);
    }
    public static void assertAccountEqual(String caller, Account expect, Account actual) {
        if (expect == actual) {
            return;
        }
        assertEmailContentEqual(caller, expect, actual);
        assertEquals(caller + " mDisplayName", expect.mDisplayName, actual.mDisplayName);
        assertEquals(caller + " mEmailAddress", expect.mEmailAddress, actual.mEmailAddress);
        assertEquals(caller + " mSyncKey", expect.mSyncKey, actual.mSyncKey);
        assertEquals(caller + " mSyncLookback", expect.mSyncLookback, actual.mSyncLookback);
        assertEquals(caller + " mSyncInterval", expect.mSyncInterval, actual.mSyncInterval);
        assertEquals(caller + " mHostAuthKeyRecv", expect.mHostAuthKeyRecv,
                actual.mHostAuthKeyRecv);
        assertEquals(caller + " mHostAuthKeySend", expect.mHostAuthKeySend,
                actual.mHostAuthKeySend);
        assertEquals(caller + " mFlags", expect.mFlags, actual.mFlags);
        assertEquals(caller + " mIsDefault", expect.mIsDefault, actual.mIsDefault);
        assertEquals(caller + " mCompatibilityUuid", expect.mCompatibilityUuid,
                actual.mCompatibilityUuid);
        assertEquals(caller + " mSenderName", expect.mSenderName, actual.mSenderName);
        assertEquals(caller + " mRingtoneUri", expect.mRingtoneUri, actual.mRingtoneUri);
        assertEquals(caller + " mProtocolVersion", expect.mProtocolVersion,
                actual.mProtocolVersion);
        assertEquals(caller + " mNewMessageCount", expect.mNewMessageCount,
                actual.mNewMessageCount);
        assertEquals(caller + " mSecurityFlags", expect.mSecurityFlags, actual.mSecurityFlags);
        assertEquals(caller + " mSecuritySyncKey", expect.mSecuritySyncKey,
                actual.mSecuritySyncKey);
        assertEquals(caller + " mSignature", expect.mSignature, actual.mSignature);
    }
    public static void assertHostAuthEqual(String caller, HostAuth expect, HostAuth actual) {
        if (expect == actual) {
            return;
        }
        assertEmailContentEqual(caller, expect, actual);
        assertEquals(caller + " mProtocol", expect.mProtocol, actual.mProtocol);
        assertEquals(caller + " mAddress", expect.mAddress, actual.mAddress);
        assertEquals(caller + " mPort", expect.mPort, actual.mPort);
        assertEquals(caller + " mFlags", expect.mFlags, actual.mFlags);
        assertEquals(caller + " mLogin", expect.mLogin, actual.mLogin);
        assertEquals(caller + " mPassword", expect.mPassword, actual.mPassword);
        assertEquals(caller + " mDomain", expect.mDomain, actual.mDomain);
    }
    public static void assertMailboxEqual(String caller, Mailbox expect, Mailbox actual) {
        if (expect == actual) {
            return;
        }
        assertEmailContentEqual(caller, expect, actual);
        assertEquals(caller + " mDisplayName", expect.mDisplayName, actual.mDisplayName);
        assertEquals(caller + " mServerId", expect.mServerId, actual.mServerId);
        assertEquals(caller + " mParentServerId", expect.mParentServerId, actual.mParentServerId);
        assertEquals(caller + " mAccountKey", expect.mAccountKey, actual.mAccountKey);
        assertEquals(caller + " mType", expect.mType, actual.mType);
        assertEquals(caller + " mDelimiter", expect.mDelimiter, actual.mDelimiter);
        assertEquals(caller + " mSyncKey", expect.mSyncKey, actual.mSyncKey);
        assertEquals(caller + " mSyncLookback", expect.mSyncLookback, actual.mSyncLookback);
        assertEquals(caller + " mSyncInterval", expect.mSyncInterval, actual.mSyncInterval);
        assertEquals(caller + " mSyncTime", expect.mSyncTime, actual.mSyncTime);
        assertEquals(caller + " mUnreadCount", expect.mUnreadCount, actual.mUnreadCount);
        assertEquals(caller + " mFlagVisible", expect.mFlagVisible, actual.mFlagVisible);
        assertEquals(caller + " mFlags", expect.mFlags, actual.mFlags);
        assertEquals(caller + " mVisibleLimit", expect.mVisibleLimit, actual.mVisibleLimit);
    }
    public static void assertMessageEqual(String caller, Message expect, Message actual) {
        if (expect == actual) {
            return;
        }
        assertEmailContentEqual(caller, expect, actual);
        assertEquals(caller + " mDisplayName", expect.mDisplayName, actual.mDisplayName);
        assertEquals(caller + " mTimeStamp", expect.mTimeStamp, actual.mTimeStamp);
        assertEquals(caller + " mSubject", expect.mSubject, actual.mSubject);
        assertEquals(caller + " mFlagRead = false", expect.mFlagRead, actual.mFlagRead);
        assertEquals(caller + " mFlagLoaded", expect.mFlagLoaded, actual.mFlagLoaded);
        assertEquals(caller + " mFlagFavorite", expect.mFlagFavorite, actual.mFlagFavorite);
        assertEquals(caller + " mFlagAttachment", expect.mFlagAttachment, actual.mFlagAttachment);
        assertEquals(caller + " mFlags", expect.mFlags, actual.mFlags);
        assertEquals(caller + " mServerId", expect.mServerId, actual.mServerId);
        assertEquals(caller + " mServerTimeStamp", expect.mServerTimeStamp,actual.mServerTimeStamp);
        assertEquals(caller + " mClientId", expect.mClientId, actual.mClientId);
        assertEquals(caller + " mMessageId", expect.mMessageId, actual.mMessageId);
        assertEquals(caller + " mMailboxKey", expect.mMailboxKey, actual.mMailboxKey);
        assertEquals(caller + " mAccountKey", expect.mAccountKey, actual.mAccountKey);
        assertEquals(caller + " mFrom", expect.mFrom, actual.mFrom);
        assertEquals(caller + " mTo", expect.mTo, actual.mTo);
        assertEquals(caller + " mCc", expect.mCc, actual.mCc);
        assertEquals(caller + " mBcc", expect.mBcc, actual.mBcc);
        assertEquals(caller + " mReplyTo", expect.mReplyTo, actual.mReplyTo);
        assertEquals(caller + " mMeetingInfo", expect.mMeetingInfo, actual.mMeetingInfo);
        assertEquals(caller + " mText", expect.mText, actual.mText);
        assertEquals(caller + " mHtml", expect.mHtml, actual.mHtml);
        assertEquals(caller + " mTextReply", expect.mTextReply, actual.mTextReply);
        assertEquals(caller + " mHtmlReply", expect.mHtmlReply, actual.mHtmlReply);
        assertEquals(caller + " mSourceKey", expect.mSourceKey, actual.mSourceKey);
        assertEquals(caller + " mIntroText", expect.mIntroText, actual.mIntroText);
    }
    public static void assertAttachmentEqual(String caller, Attachment expect, Attachment actual) {
        if (expect == actual) {
            return;
        }
        assertEmailContentEqual(caller, expect, actual);
        assertEquals(caller + " mSize", expect.mSize, actual.mSize);
        assertEquals(caller + " mFileName", expect.mFileName, actual.mFileName);
        assertEquals(caller + " mContentId", expect.mContentId, actual.mContentId);
        assertEquals(caller + " mContentUri", expect.mContentUri, actual.mContentUri);
        assertEquals(caller + " mMessageKey", expect.mMessageKey, actual.mMessageKey);
        assertEquals(caller + " mMimeType", expect.mMimeType, actual.mMimeType);
        assertEquals(caller + " mLocation", expect.mLocation, actual.mLocation);
        assertEquals(caller + " mEncoding", expect.mEncoding, actual.mEncoding);
        assertEquals(caller + " mContent", expect.mContent, actual.mContent);
        assertEquals(caller + " mFlags", expect.mFlags, actual.mFlags);
        MoreAsserts.assertEquals(caller + " mContentBytes",
                expect.mContentBytes, actual.mContentBytes);
    }
}

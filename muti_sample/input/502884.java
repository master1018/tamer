public class ImServiceConstants {
    public static final ComponentName IM_SERVICE_COMPONENT = new ComponentName(
            "com.android.im",
            "com.android.im.service.RemoteImService");
    public static final String ACTION_AVATAR_CHANGED =
            "android.intent.action.IM_AVATAR_CHANGED";
    public static final String ACTION_MANAGE_SUBSCRIPTION =
            "android.intent.action.IM_MANAGE_SUBSCRIPTION";
    public static final String EXTRA_INTENT_FROM_ADDRESS = "from";
    public static final String EXTRA_INTENT_PROVIDER_ID = "providerId";
    public static final String EXTRA_INTENT_ACCOUNT_ID = "accountId";
    public static final String EXTRA_INTENT_LIST_NAME = "listName";
    public static final String EXTRA_CHECK_AUTO_LOGIN = "autologin";
    public static final String EXTRA_INTENT_SHOW_MULTIPLE = "show_multiple";
}

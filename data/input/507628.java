public class ContactPresenceIconUtil {
    public static Drawable getPresenceIcon (Context context, int status) {
        switch(status) {
            case StatusUpdates.AVAILABLE:
            case StatusUpdates.IDLE:
            case StatusUpdates.AWAY:
            case StatusUpdates.DO_NOT_DISTURB:
            case StatusUpdates.INVISIBLE:
                return context.getResources().getDrawable(
                        StatusUpdates.getPresenceIconResourceId(status));
            case StatusUpdates.OFFLINE:
            default:
                return null;
        }
    }
}

public class Notification implements Parcelable
{
    public static final int DEFAULT_ALL = ~0;
    public static final int DEFAULT_SOUND = 1;
    public static final int DEFAULT_VIBRATE = 2;
    public static final int DEFAULT_LIGHTS = 4;
    public long when;
    public int icon;
    public int number;
    public PendingIntent contentIntent;
    public PendingIntent deleteIntent;
    public CharSequence tickerText;
    public RemoteViews contentView;
    public int iconLevel;
    public Uri sound;
    public static final int STREAM_DEFAULT = -1;
    public int audioStreamType = STREAM_DEFAULT;
    public long[] vibrate;
    public int ledARGB;
    public int ledOnMS;
    public int ledOffMS;
    public int defaults;
    public static final int FLAG_SHOW_LIGHTS        = 0x00000001;
    public static final int FLAG_ONGOING_EVENT      = 0x00000002;
    public static final int FLAG_INSISTENT          = 0x00000004;
    public static final int FLAG_ONLY_ALERT_ONCE    = 0x00000008;
    public static final int FLAG_AUTO_CANCEL        = 0x00000010;
    public static final int FLAG_NO_CLEAR           = 0x00000020;
    public static final int FLAG_FOREGROUND_SERVICE = 0x00000040;
    public int flags;
    public Notification()
    {
        this.when = System.currentTimeMillis();
    }
    public Notification(Context context, int icon, CharSequence tickerText, long when,
            CharSequence contentTitle, CharSequence contentText, Intent contentIntent)
    {
        this.when = when;
        this.icon = icon;
        this.tickerText = tickerText;
        setLatestEventInfo(context, contentTitle, contentText,
                PendingIntent.getActivity(context, 0, contentIntent, 0));
    }
    public Notification(int icon, CharSequence tickerText, long when)
    {
        this.icon = icon;
        this.tickerText = tickerText;
        this.when = when;
    }
    public Notification(Parcel parcel)
    {
        int version = parcel.readInt();
        when = parcel.readLong();
        icon = parcel.readInt();
        number = parcel.readInt();
        if (parcel.readInt() != 0) {
            contentIntent = PendingIntent.CREATOR.createFromParcel(parcel);
        }
        if (parcel.readInt() != 0) {
            deleteIntent = PendingIntent.CREATOR.createFromParcel(parcel);
        }
        if (parcel.readInt() != 0) {
            tickerText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
        }
        if (parcel.readInt() != 0) {
            contentView = RemoteViews.CREATOR.createFromParcel(parcel);
        }
        defaults = parcel.readInt();
        flags = parcel.readInt();
        if (parcel.readInt() != 0) {
            sound = Uri.CREATOR.createFromParcel(parcel);
        }
        audioStreamType = parcel.readInt();
        vibrate = parcel.createLongArray();
        ledARGB = parcel.readInt();
        ledOnMS = parcel.readInt();
        ledOffMS = parcel.readInt();
        iconLevel = parcel.readInt();
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeInt(1);
        parcel.writeLong(when);
        parcel.writeInt(icon);
        parcel.writeInt(number);
        if (contentIntent != null) {
            parcel.writeInt(1);
            contentIntent.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        if (deleteIntent != null) {
            parcel.writeInt(1);
            deleteIntent.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        if (tickerText != null) {
            parcel.writeInt(1);
            TextUtils.writeToParcel(tickerText, parcel, flags);
        } else {
            parcel.writeInt(0);
        }
        if (contentView != null) {
            parcel.writeInt(1);
            contentView.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeInt(defaults);
        parcel.writeInt(this.flags);
        if (sound != null) {
            parcel.writeInt(1);
            sound.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeInt(audioStreamType);
        parcel.writeLongArray(vibrate);
        parcel.writeInt(ledARGB);
        parcel.writeInt(ledOnMS);
        parcel.writeInt(ledOffMS);
        parcel.writeInt(iconLevel);
    }
    public static final Parcelable.Creator<Notification> CREATOR
            = new Parcelable.Creator<Notification>()
    {
        public Notification createFromParcel(Parcel parcel)
        {
            return new Notification(parcel);
        }
        public Notification[] newArray(int size)
        {
            return new Notification[size];
        }
    };
    public void setLatestEventInfo(Context context,
            CharSequence contentTitle, CharSequence contentText, PendingIntent contentIntent) {
        RemoteViews contentView = new RemoteViews(context.getPackageName(),
                com.android.internal.R.layout.status_bar_latest_event_content);
        if (this.icon != 0) {
            contentView.setImageViewResource(com.android.internal.R.id.icon, this.icon);
        }
        if (contentTitle != null) {
            contentView.setTextViewText(com.android.internal.R.id.title, contentTitle);
        }
        if (contentText != null) {
            contentView.setTextViewText(com.android.internal.R.id.text, contentText);
        }
        if (this.when != 0) {
            contentView.setLong(com.android.internal.R.id.time, "setTime", when);
        }
        this.contentView = contentView;
        this.contentIntent = contentIntent;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Notification(vibrate=");
        if (this.vibrate != null) {
            int N = this.vibrate.length-1;
            sb.append("[");
            for (int i=0; i<N; i++) {
                sb.append(this.vibrate[i]);
                sb.append(',');
            }
            if (N != -1) {
                sb.append(this.vibrate[N]);
            }
            sb.append("]");
        } else if ((this.defaults & DEFAULT_VIBRATE) != 0) {
            sb.append("default");
        } else {
            sb.append("null");
        }
        sb.append(",sound=");
        if (this.sound != null) {
            sb.append(this.sound.toString());
        } else if ((this.defaults & DEFAULT_SOUND) != 0) {
            sb.append("default");
        } else {
            sb.append("null");
        }
        sb.append(",defaults=0x");
        sb.append(Integer.toHexString(this.defaults));
        sb.append(")");
        return sb.toString();
    }
}

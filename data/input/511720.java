public final class Message implements Parcelable {
    public int what;
    public int arg1; 
    public int arg2;
    public Object obj;
    public Messenger replyTo;
     long when;
     Bundle data;
     Handler target;     
     Runnable callback;   
     Message next;
    private static Object mPoolSync = new Object();
    private static Message mPool;
    private static int mPoolSize = 0;
    private static final int MAX_POOL_SIZE = 10;
    public static Message obtain() {
        synchronized (mPoolSync) {
            if (mPool != null) {
                Message m = mPool;
                mPool = m.next;
                m.next = null;
                return m;
            }
        }
        return new Message();
    }
    public static Message obtain(Message orig) {
        Message m = obtain();
        m.what = orig.what;
        m.arg1 = orig.arg1;
        m.arg2 = orig.arg2;
        m.obj = orig.obj;
        m.replyTo = orig.replyTo;
        if (orig.data != null) {
            m.data = new Bundle(orig.data);
        }
        m.target = orig.target;
        m.callback = orig.callback;
        return m;
    }
    public static Message obtain(Handler h) {
        Message m = obtain();
        m.target = h;
        return m;
    }
    public static Message obtain(Handler h, Runnable callback) {
        Message m = obtain();
        m.target = h;
        m.callback = callback;
        return m;
    }
    public static Message obtain(Handler h, int what) {
        Message m = obtain();
        m.target = h;
        m.what = what;
        return m;
    }
    public static Message obtain(Handler h, int what, Object obj) {
        Message m = obtain();
        m.target = h;
        m.what = what;
        m.obj = obj;
        return m;
    }
    public static Message obtain(Handler h, int what, int arg1, int arg2) {
        Message m = obtain();
        m.target = h;
        m.what = what;
        m.arg1 = arg1;
        m.arg2 = arg2;
        return m;
    }
    public static Message obtain(Handler h, int what, 
            int arg1, int arg2, Object obj) {
        Message m = obtain();
        m.target = h;
        m.what = what;
        m.arg1 = arg1;
        m.arg2 = arg2;
        m.obj = obj;
        return m;
    }
    public void recycle() {
        synchronized (mPoolSync) {
            if (mPoolSize < MAX_POOL_SIZE) {
                clearForRecycle();
                next = mPool;
                mPool = this;
            }
        }
    }
    public void copyFrom(Message o) {
        this.what = o.what;
        this.arg1 = o.arg1;
        this.arg2 = o.arg2;
        this.obj = o.obj;
        this.replyTo = o.replyTo;
        if (o.data != null) {
            this.data = (Bundle) o.data.clone();
        } else {
            this.data = null;
        }
    }
    public long getWhen() {
        return when;
    }
    public void setTarget(Handler target) {
        this.target = target;
    }
    public Handler getTarget() {
        return target;
    }
    public Runnable getCallback() {
        return callback;
    }
    public Bundle getData() {
        if (data == null) {
            data = new Bundle();
        }
        return data;
    }
    public Bundle peekData() {
        return data;
    }
    public void setData(Bundle data) {
        this.data = data;
    }
    public void sendToTarget() {
        target.sendMessage(this);
    }
     void clearForRecycle() {
        what = 0;
        arg1 = 0;
        arg2 = 0;
        obj = null;
        replyTo = null;
        when = 0;
        target = null;
        callback = null;
        data = null;
    }
    public Message() {
    }
    public String toString() {
        StringBuilder   b = new StringBuilder();
        b.append("{ what=");
        b.append(what);
        b.append(" when=");
        b.append(when);
        if (arg1 != 0) {
            b.append(" arg1=");
            b.append(arg1);
        }
        if (arg2 != 0) {
            b.append(" arg2=");
            b.append(arg2);
        }
        if (obj != null) {
            b.append(" obj=");
            b.append(obj);
        }
        b.append(" }");
        return b.toString();
    }
    public static final Parcelable.Creator<Message> CREATOR
            = new Parcelable.Creator<Message>() {
        public Message createFromParcel(Parcel source) {
            Message msg = Message.obtain();
            msg.readFromParcel(source);
            return msg;
        }
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        if (callback != null) {
            throw new RuntimeException(
                "Can't marshal callbacks across processes.");
        }
        dest.writeInt(what);
        dest.writeInt(arg1);
        dest.writeInt(arg2);
        if (obj != null) {
            try {
                Parcelable p = (Parcelable)obj;
                dest.writeInt(1);
                dest.writeParcelable(p, flags);
            } catch (ClassCastException e) {
                throw new RuntimeException(
                    "Can't marshal non-Parcelable objects across processes.");
            }
        } else {
            dest.writeInt(0);
        }
        dest.writeLong(when);
        dest.writeBundle(data);
        Messenger.writeMessengerOrNullToParcel(replyTo, dest);
    }
    private final void readFromParcel(Parcel source) {
        what = source.readInt();
        arg1 = source.readInt();
        arg2 = source.readInt();
        if (source.readInt() != 0) {
            obj = source.readParcelable(getClass().getClassLoader());
        }
        when = source.readLong();
        data = source.readBundle();
        replyTo = Messenger.readMessengerOrNullFromParcel(source);
    }
}

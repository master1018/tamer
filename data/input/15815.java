class EngineArgs {
    ByteBuffer netData;
    ByteBuffer [] appData;
    private int offset;         
    private int len;
    private int netPos;
    private int netLim;
    private int [] appPoss;
    private int [] appLims;
    private int appRemaining = 0;
    private boolean wrapMethod;
    EngineArgs(ByteBuffer [] appData, int offset, int len,
            ByteBuffer netData) {
        this.wrapMethod = true;
        init(netData, appData, offset, len);
    }
    EngineArgs(ByteBuffer netData, ByteBuffer [] appData, int offset,
            int len) {
        this.wrapMethod = false;
        init(netData, appData, offset, len);
    }
    private void init(ByteBuffer netData, ByteBuffer [] appData,
            int offset, int len) {
        if ((netData == null) || (appData == null)) {
            throw new IllegalArgumentException("src/dst is null");
        }
        if ((offset < 0) || (len < 0) || (offset > appData.length - len)) {
            throw new IndexOutOfBoundsException();
        }
        if (wrapMethod && netData.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        netPos = netData.position();
        netLim = netData.limit();
        appPoss = new int [appData.length];
        appLims = new int [appData.length];
        for (int i = offset; i < offset + len; i++) {
            if (appData[i] == null) {
                throw new IllegalArgumentException(
                    "appData[" + i + "] == null");
            }
            if (!wrapMethod && appData[i].isReadOnly()) {
                throw new ReadOnlyBufferException();
            }
            appRemaining += appData[i].remaining();
            appPoss[i] = appData[i].position();
            appLims[i] = appData[i].limit();
        }
        this.netData = netData;
        this.appData = appData;
        this.offset = offset;
        this.len = len;
    }
    void gather(int spaceLeft) {
        for (int i = offset; (i < (offset + len)) && (spaceLeft > 0); i++) {
            int amount = Math.min(appData[i].remaining(), spaceLeft);
            appData[i].limit(appData[i].position() + amount);
            netData.put(appData[i]);
            spaceLeft -= amount;
        }
    }
    void scatter(ByteBuffer readyData) {
        int amountLeft = readyData.remaining();
        for (int i = offset; (i < (offset + len)) && (amountLeft > 0);
                i++) {
            int amount = Math.min(appData[i].remaining(), amountLeft);
            readyData.limit(readyData.position() + amount);
            appData[i].put(readyData);
            amountLeft -= amount;
        }
        assert(readyData.remaining() == 0);
    }
    int getAppRemaining() {
        return appRemaining;
    }
    int deltaNet() {
        return (netData.position() - netPos);
    }
    int deltaApp() {
        int sum = 0;    
        for (int i = offset; i < offset + len; i++) {
            sum += appData[i].position() - appPoss[i];
        }
        return sum;
    }
    void resetPos() {
        netData.position(netPos);
        for (int i = offset; i < offset + len; i++) {
            appData[i].position(appPoss[i]);
        }
    }
    void resetLim() {
        netData.limit(netLim);
        for (int i = offset; i < offset + len; i++) {
            appData[i].limit(appLims[i]);
        }
    }
}

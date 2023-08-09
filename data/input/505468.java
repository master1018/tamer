final class Device implements IDevice {
    final static String RE_EMULATOR_SN = "emulator-(\\d+)"; 
    private String mSerialNumber = null;
    private String mAvdName = null;
    private DeviceState mState = null;
    private final Map<String, String> mProperties = new HashMap<String, String>();
    private final Map<String, String> mMountPoints = new HashMap<String, String>();
    private final ArrayList<Client> mClients = new ArrayList<Client>();
    private DeviceMonitor mMonitor;
    private static final String LOG_TAG = "Device";
    private SocketChannel mSocketChannel;
    private static final class InstallReceiver extends MultiLineReceiver {
        private static final String SUCCESS_OUTPUT = "Success"; 
        private static final Pattern FAILURE_PATTERN = Pattern.compile("Failure\\s+\\[(.*)\\]"); 
        private String mErrorMessage = null;
        public InstallReceiver() {
        }
        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                if (line.length() > 0) {
                    if (line.startsWith(SUCCESS_OUTPUT)) {
                        mErrorMessage = null;
                    } else {
                        Matcher m = FAILURE_PATTERN.matcher(line);
                        if (m.matches()) {
                            mErrorMessage = m.group(1);
                        }
                    }
                }
            }
        }
        public boolean isCancelled() {
            return false;
        }
        public String getErrorMessage() {
            return mErrorMessage;
        }
    }
    public String getSerialNumber() {
        return mSerialNumber;
    }
    public String getAvdName() {
        return mAvdName;
    }
    void setAvdName(String avdName) {
        if (isEmulator() == false) {
            throw new IllegalArgumentException(
                    "Cannot set the AVD name of the device is not an emulator");
        }
        mAvdName = avdName;
    }
    public DeviceState getState() {
        return mState;
    }
    void setState(DeviceState state) {
        mState = state;
    }
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(mProperties);
    }
    public int getPropertyCount() {
        return mProperties.size();
    }
    public String getProperty(String name) {
        return mProperties.get(name);
    }
    public String getMountPoint(String name) {
        return mMountPoints.get(name);
    }
    @Override
    public String toString() {
        return mSerialNumber;
    }
    public boolean isOnline() {
        return mState == DeviceState.ONLINE;
    }
    public boolean isEmulator() {
        return mSerialNumber.matches(RE_EMULATOR_SN);
    }
    public boolean isOffline() {
        return mState == DeviceState.OFFLINE;
    }
    public boolean isBootLoader() {
        return mState == DeviceState.BOOTLOADER;
    }
    public boolean hasClients() {
        return mClients.size() > 0;
    }
    public Client[] getClients() {
        synchronized (mClients) {
            return mClients.toArray(new Client[mClients.size()]);
        }
    }
    public Client getClient(String applicationName) {
        synchronized (mClients) {
            for (Client c : mClients) {
                if (applicationName.equals(c.getClientData().getClientDescription())) {
                    return c;
                }
            }
        }
        return null;
    }
    public SyncService getSyncService() throws IOException {
        SyncService syncService = new SyncService(AndroidDebugBridge.sSocketAddr, this);
        if (syncService.openSync()) {
            return syncService;
         }
        return null;
    }
    public FileListingService getFileListingService() {
        return new FileListingService(this);
    }
    public RawImage getScreenshot() throws IOException {
        return AdbHelper.getFrameBuffer(AndroidDebugBridge.sSocketAddr, this);
    }
    public void executeShellCommand(String command, IShellOutputReceiver receiver)
            throws IOException {
        AdbHelper.executeRemoteCommand(AndroidDebugBridge.sSocketAddr, command, this,
                receiver);
    }
    public void runEventLogService(LogReceiver receiver) throws IOException {
        AdbHelper.runEventLogService(AndroidDebugBridge.sSocketAddr, this, receiver);
    }
    public void runLogService(String logname,
            LogReceiver receiver) throws IOException {
        AdbHelper.runLogService(AndroidDebugBridge.sSocketAddr, this, logname, receiver);
    }
    public boolean createForward(int localPort, int remotePort) {
        try {
            return AdbHelper.createForward(AndroidDebugBridge.sSocketAddr, this,
                    localPort, remotePort);
        } catch (IOException e) {
            Log.e("adb-forward", e); 
            return false;
        }
    }
    public boolean removeForward(int localPort, int remotePort) {
        try {
            return AdbHelper.removeForward(AndroidDebugBridge.sSocketAddr, this,
                    localPort, remotePort);
        } catch (IOException e) {
            Log.e("adb-remove-forward", e); 
            return false;
        }
    }
    public String getClientName(int pid) {
        synchronized (mClients) {
            for (Client c : mClients) {
                if (c.getClientData().getPid() == pid) {
                    return c.getClientData().getClientDescription();
                }
            }
        }
        return null;
    }
    Device(DeviceMonitor monitor, String serialNumber, DeviceState deviceState) {
        mMonitor = monitor;
        mSerialNumber = serialNumber;
        mState = deviceState;
    }
    DeviceMonitor getMonitor() {
        return mMonitor;
    }
    void addClient(Client client) {
        synchronized (mClients) {
            mClients.add(client);
        }
    }
    List<Client> getClientList() {
        return mClients;
    }
    boolean hasClient(int pid) {
        synchronized (mClients) {
            for (Client client : mClients) {
                if (client.getClientData().getPid() == pid) {
                    return true;
                }
            }
        }
        return false;
    }
    void clearClientList() {
        synchronized (mClients) {
            mClients.clear();
        }
    }
    void setClientMonitoringSocket(SocketChannel socketChannel) {
        mSocketChannel = socketChannel;
    }
    SocketChannel getClientMonitoringSocket() {
        return mSocketChannel;
    }
    void removeClient(Client client, boolean notify) {
        mMonitor.addPortToAvailableList(client.getDebuggerListenPort());
        synchronized (mClients) {
            mClients.remove(client);
        }
        if (notify) {
            mMonitor.getServer().deviceChanged(this, CHANGE_CLIENT_LIST);
        }
    }
    void update(int changeMask) {
        mMonitor.getServer().deviceChanged(this, changeMask);
    }
    void update(Client client, int changeMask) {
        mMonitor.getServer().clientChanged(client, changeMask);
    }
    void addProperty(String label, String value) {
        mProperties.put(label, value);
    }
    void setMountingPoint(String name, String value) {
        mMountPoints.put(name, value);
    }
    public String installPackage(String packageFilePath, boolean reinstall)
           throws IOException {
       String remoteFilePath = syncPackageToDevice(packageFilePath);
       String result = installRemotePackage(remoteFilePath, reinstall);
       removeRemotePackage(remoteFilePath);
       return result;
    }
    public String syncPackageToDevice(String localFilePath)
            throws IOException {
        try {
            String packageFileName = getFileName(localFilePath);
            String remoteFilePath = String.format("/data/local/tmp/%1$s", packageFileName); 
            Log.d(packageFileName, String.format("Uploading %1$s onto device '%2$s'",
                    packageFileName, getSerialNumber()));
            SyncService sync = getSyncService();
            if (sync != null) {
                String message = String.format("Uploading file onto device '%1$s'",
                        getSerialNumber());
                Log.d(LOG_TAG, message);
                SyncResult result = sync.pushFile(localFilePath, remoteFilePath,
                        SyncService.getNullProgressMonitor());
                if (result.getCode() != SyncService.RESULT_OK) {
                    throw new IOException(String.format("Unable to upload file: %1$s",
                            result.getMessage()));
                }
            } else {
                throw new IOException("Unable to open sync connection!");
            }
            return remoteFilePath;
        } catch (IOException e) {
            Log.e(LOG_TAG, String.format("Unable to open sync connection! reason: %1$s",
                    e.getMessage()));
            throw e;
        }
    }
    private String getFileName(String filePath) {
        return new File(filePath).getName();
    }
    public String installRemotePackage(String remoteFilePath, boolean reinstall)
            throws IOException {
        InstallReceiver receiver = new InstallReceiver();
        String cmd = String.format(reinstall ? "pm install -r \"%1$s\"" : "pm install \"%1$s\"",
                            remoteFilePath);
        executeShellCommand(cmd, receiver);
        return receiver.getErrorMessage();
    }
    public void removeRemotePackage(String remoteFilePath) throws IOException {
        try {
            executeShellCommand("rm " + remoteFilePath, new NullOutputReceiver());
        } catch (IOException e) {
            Log.e(LOG_TAG, String.format("Failed to delete temporary package: %1$s",
                    e.getMessage()));
            throw e;
        }
    }
    public String uninstallPackage(String packageName) throws IOException {
        InstallReceiver receiver = new InstallReceiver();
        executeShellCommand("pm uninstall " + packageName, receiver);
        return receiver.getErrorMessage();
    }
}

public class WifiCommand extends Svc.Command {
    public WifiCommand() {
        super("wifi");
    }
    public String shortHelp() {
        return "Control the Wi-Fi manager";
    }
    public String longHelp() {
        return shortHelp() + "\n"
                + "\n"
                + "usage: svc wifi [enable|disable]\n"
                + "         Turn Wi-Fi on or off.\n\n"
                + "       svc wifi prefer\n"
                + "          Set Wi-Fi as the preferred data network\n";
    }
    public void run(String[] args) {
        boolean validCommand = false;
        if (args.length >= 2) {
            boolean flag = false;
            if ("enable".equals(args[1])) {
                flag = true;
                validCommand = true;
            } else if ("disable".equals(args[1])) {
                flag = false;
                validCommand = true;
            } else if ("prefer".equals(args[1])) {
                IConnectivityManager connMgr =
                        IConnectivityManager.Stub.asInterface(ServiceManager.getService(Context.CONNECTIVITY_SERVICE));
                try {
                    connMgr.setNetworkPreference(ConnectivityManager.TYPE_WIFI);
                } catch (RemoteException e) {
                    System.err.println("Failed to set preferred network: " + e);
                }
                return;
            }
            if (validCommand) {
                IWifiManager wifiMgr
                        = IWifiManager.Stub.asInterface(ServiceManager.getService(Context.WIFI_SERVICE));
                try {
                    wifiMgr.setWifiEnabled(flag);
                }
                catch (RemoteException e) {
                    System.err.println("Wi-Fi operation failed: " + e);
                }
                return;
            }
        }
        System.err.println(longHelp());
    }
}
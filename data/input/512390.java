public class DataCommand extends Svc.Command {
    public DataCommand() {
        super("data");
    }
    public String shortHelp() {
        return "Control mobile data connectivity";
    }
    public String longHelp() {
        return shortHelp() + "\n"
                + "\n"
                + "usage: svc data [enable|disable]\n"
                + "         Turn mobile data on or off.\n\n"
                + "       svc data prefer\n"
                + "          Set mobile as the preferred data network\n";
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
                    connMgr.setNetworkPreference(ConnectivityManager.TYPE_MOBILE);
                } catch (RemoteException e) {
                    System.err.println("Failed to set preferred network: " + e);
                }
                return;
            }
            if (validCommand) {
                ITelephony phoneMgr
                        = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
                try {
                    if (flag) {
                        phoneMgr.enableDataConnectivity();
                    } else
                        phoneMgr.disableDataConnectivity();
                }
                catch (RemoteException e) {
                    System.err.println("Mobile data operation failed: " + e);
                }
                return;
            }
        }
        System.err.println(longHelp());
    }
}
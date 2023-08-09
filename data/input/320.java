public class Klist {
    Object target;
    char[] options = new char[4];
    String name;       
    char action;       
    private static boolean DEBUG = Krb5.DEBUG;
    public static void main(String[] args) {
        Klist klist = new Klist();
        if ((args == null) || (args.length == 0)) {
            klist.action = 'c'; 
        } else {
            klist.processArgs(args);
        }
        switch (klist.action) {
        case 'c':
            if (klist.name == null) {
                klist.target = CredentialsCache.getInstance();
                klist.name = CredentialsCache.cacheName();
            } else
                klist.target = CredentialsCache.getInstance(klist.name);
            if (klist.target != null)  {
                klist.displayCache();
            } else {
                klist.displayMessage("Credentials cache");
                System.exit(-1);
            }
            break;
        case 'k':
            try {
                KeyTab ktab = KeyTab.getInstance(klist.name);
                klist.target = ktab;
                klist.name = ktab.tabName();
            } catch (Exception e) {
                klist.displayMessage("KeyTab");
                System.exit(-1);
            }
            klist.displayTab();
            break;
        default:
            if (klist.name != null) {
                klist.printHelp();
                System.exit(-1);
            } else {
                klist.target = CredentialsCache.getInstance();
                klist.name = CredentialsCache.cacheName();
                if (klist.target != null) {
                    klist.displayCache();
                } else {
                    klist.displayMessage("Credentials cache");
                    System.exit(-1);
                }
            }
        }
    }
    void processArgs(String[] args) {
        Character arg;
        for (int i = 0; i < args.length; i++) {
            if ((args[i].length() >= 2) && (args[i].startsWith("-"))) {
                arg = new Character(args[i].charAt(1));
                switch (arg.charValue()) {
                case 'c':
                    action = 'c';
                    break;
                case 'k':
                    action = 'k';
                    break;
                case 'a':
                    options[2] = 'a';
                    break;
                case 'n':
                    options[3] = 'n';
                    break;
                case 'f':
                    options[1] = 'f';
                    break;
                case 'e':
                    options[0] = 'e';
                    break;
                case 'K':
                    options[1] = 'K';
                    break;
                case 't':
                    options[2] = 't';
                    break;
                default:
                    printHelp();
                    System.exit(-1);
                }
            } else {
                if (!args[i].startsWith("-") && (i == args.length - 1)) {
                    name = args[i];
                    arg = null;
                } else {
                    printHelp(); 
                    System.exit(-1);
                }
            }
        }
    }
    void displayTab() {
        KeyTab table = (KeyTab)target;
        KeyTabEntry[] entries = table.getEntries();
        if (entries.length == 0) {
            System.out.println("\nKey tab: " + name +
                               ", " + " 0 entries found.\n");
        } else {
            if (entries.length == 1)
                System.out.println("\nKey tab: " + name +
                                   ", " + entries.length + " entry found.\n");
            else
                System.out.println("\nKey tab: " + name + ", " +
                                   entries.length + " entries found.\n");
            for (int i = 0; i < entries.length; i++) {
                System.out.println("[" + (i + 1) + "] " +
                                   "Service principal: "  +
                                   entries[i].getService().toString());
                System.out.println("\t KVNO: " +
                                   entries[i].getKey().getKeyVersionNumber());
                if (options[0] == 'e') {
                    EncryptionKey key = entries[i].getKey();
                    System.out.println("\t Key type: " +
                                       key.getEType());
                }
                if (options[1] == 'K') {
                    EncryptionKey key = entries[i].getKey();
                    System.out.println("\t Key: " +
                                       entries[i].getKeyString());
                }
                if (options[2] == 't') {
                    System.out.println("\t Time stamp: " +
                            reformat(entries[i].getTimeStamp().toDate().toString()));
                }
            }
        }
    }
    void displayCache() {
        CredentialsCache cache = (CredentialsCache)target;
        sun.security.krb5.internal.ccache.Credentials[] creds =
            cache.getCredsList();
        if (creds == null) {
            System.out.println ("No credentials available in the cache " +
                                name);
            System.exit(-1);
        }
        System.out.println("\nCredentials cache: " +  name);
        String defaultPrincipal = cache.getPrimaryPrincipal().toString();
        int num = creds.length;
        if (num == 1)
            System.out.println("\nDefault principal: " +
                               defaultPrincipal + ", " +
                               creds.length + " entry found.\n");
        else
            System.out.println("\nDefault principal: " +
                               defaultPrincipal + ", " +
                               creds.length + " entries found.\n");
        String starttime = null;
        String endtime = null;
        String servicePrincipal = null;
        String etype = null;
        if (creds != null) {
            for (int i = 0; i < creds.length; i++) {
                try {
                    starttime =
                        reformat(creds[i].getAuthTime().toDate().toString());
                    endtime =
                        reformat(creds[i].getEndTime().toDate().toString());
                    servicePrincipal =
                        creds[i].getServicePrincipal().toString();
                    System.out.println("[" + (i + 1) + "] " +
                                       " Service Principal:  " +
                                       servicePrincipal);
                    System.out.println("     Valid starting:  " + starttime);
                    System.out.println("     Expires:         " + endtime);
                    if (options[0] == 'e') {
                        etype = EType.toString(creds[i].getEType());
                        System.out.println("     Encryption type: " + etype);
                    }
                    if (options[1] == 'f') {
                        System.out.println("     Flags:           " +
                                           creds[i].getTicketFlags().toString());
                    }
                    if (options[2] == 'a') {
                        boolean first = true;
                        InetAddress[] caddr
                                = creds[i].setKrbCreds().getClientAddresses();
                        if (caddr != null) {
                            for (InetAddress ia: caddr) {
                                String out;
                                if (options[3] == 'n') {
                                    out = ia.getHostAddress();
                                } else {
                                    out = ia.getCanonicalHostName();
                                }
                                System.out.println("     " +
                                        (first?"Addresses:":"          ") +
                                        "       " + out);
                                first = false;
                            }
                        } else {
                            System.out.println("     [No host addresses info]");
                        }
                    }
                } catch (RealmException e) {
                    System.out.println("Error reading principal from "+
                                       "the entry.");
                    if (DEBUG) {
                        e.printStackTrace();
                    }
                    System.exit(-1);
                }
            }
        } else {
            System.out.println("\nNo entries found.");
        }
    }
    void displayMessage(String target) {
        if (name == null) {
            System.out.println("Default " + target + " not found.");
        } else {
            System.out.println(target + " " + name + " not found.");
        }
    }
    String reformat(String date) {
        return (date.substring(4, 7) + " " + date.substring(8, 10) +
                ", " + date.substring(24)
                + " " + date.substring(11, 16));
    }
    void printHelp() {
        System.out.println("\nUsage: klist " +
                           "[[-c] [-f] [-e] [-a [-n]]] [-k [-t] [-K]] [name]");
        System.out.println("   name\t name of credentials cache or " +
                           " keytab with the prefix. File-based cache or "
                           + "keytab's prefix is FILE:.");
        System.out.println("   -c specifes that credential cache is to be " +
                           "listed");
        System.out.println("   -k specifies that key tab is to be listed");
        System.out.println("   options for credentials caches:");
        System.out.println("\t-f \t shows credentials flags");
        System.out.println("\t-e \t shows the encryption type");
        System.out.println("\t-a \t shows addresses");
        System.out.println("\t  -n \t   do not reverse-resolve addresses");
        System.out.println("   options for keytabs:");
        System.out.println("\t-t \t shows keytab entry timestamps");
        System.out.println("\t-K \t shows keytab entry key value");
        System.out.println("\t-e \t shows keytab entry key type");
        System.out.println("\nUsage: java sun.security.krb5.tools.Klist " +
                           "-help for help.");
    }
}

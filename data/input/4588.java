public class SunCommandLineLauncher extends AbstractLauncher implements LaunchingConnector {
    static private final String ARG_HOME = "home";
    static private final String ARG_OPTIONS = "options";
    static private final String ARG_MAIN = "main";
    static private final String ARG_INIT_SUSPEND = "suspend";
    static private final String ARG_QUOTE = "quote";
    static private final String ARG_VM_EXEC = "vmexec";
    TransportService transportService;
    Transport transport;
    boolean usingSharedMemory = false;
    TransportService transportService() {
        return transportService;
    }
    public Transport transport() {
        return transport;
    }
    public SunCommandLineLauncher() {
        super();
        try {
            Class c = Class.forName("com.sun.tools.jdi.SharedMemoryTransportService");
            transportService = (TransportService)c.newInstance();
            transport = new Transport() {
                public String name() {
                    return "dt_shmem";
                }
            };
            usingSharedMemory = true;
        } catch (ClassNotFoundException x) {
        } catch (UnsatisfiedLinkError x) {
        } catch (InstantiationException x) {
        } catch (IllegalAccessException x) {
        };
        if (transportService == null) {
            transportService = new SocketTransportService();
            transport = new Transport() {
                public String name() {
                    return "dt_socket";
                }
            };
        }
        addStringArgument(
                ARG_HOME,
                getString("sun.home.label"),
                getString("sun.home"),
                System.getProperty("java.home"),
                false);
        addStringArgument(
                ARG_OPTIONS,
                getString("sun.options.label"),
                getString("sun.options"),
                "",
                false);
        addStringArgument(
                ARG_MAIN,
                getString("sun.main.label"),
                getString("sun.main"),
                "",
                true);
        addBooleanArgument(
                ARG_INIT_SUSPEND,
                getString("sun.init_suspend.label"),
                getString("sun.init_suspend"),
                true,
                false);
        addStringArgument(
                ARG_QUOTE,
                getString("sun.quote.label"),
                getString("sun.quote"),
                "\"",
                true);
        addStringArgument(
                ARG_VM_EXEC,
                getString("sun.vm_exec.label"),
                getString("sun.vm_exec"),
                "java",
                true);
    }
    static boolean hasWhitespace(String string) {
        int length = string.length();
        for (int i = 0; i < length; i++) {
            if (Character.isWhitespace(string.charAt(i))) {
                return true;
            }
        }
        return false;
    }
    public VirtualMachine
        launch(Map<String,? extends Connector.Argument> arguments)
        throws IOException, IllegalConnectorArgumentsException,
               VMStartException
    {
        VirtualMachine vm;
        String home = argument(ARG_HOME, arguments).value();
        String options = argument(ARG_OPTIONS, arguments).value();
        String mainClassAndArgs = argument(ARG_MAIN, arguments).value();
        boolean wait = ((BooleanArgumentImpl)argument(ARG_INIT_SUSPEND,
                                                  arguments)).booleanValue();
        String quote = argument(ARG_QUOTE, arguments).value();
        String exe = argument(ARG_VM_EXEC, arguments).value();
        String exePath = null;
        if (quote.length() > 1) {
            throw new IllegalConnectorArgumentsException("Invalid length",
                                                         ARG_QUOTE);
        }
        if ((options.indexOf("-Djava.compiler=") != -1) &&
            (options.toLowerCase().indexOf("-djava.compiler=none") == -1)) {
            throw new IllegalConnectorArgumentsException("Cannot debug with a JIT compiler",
                                                         ARG_OPTIONS);
        }
        TransportService.ListenKey listenKey;
        if (usingSharedMemory) {
            Random rr = new Random();
            int failCount = 0;
            while(true) {
                try {
                    String address = "javadebug" +
                        String.valueOf(rr.nextInt(100000));
                    listenKey = transportService().startListening(address);
                    break;
                } catch (IOException ioe) {
                    if (++failCount > 5) {
                        throw ioe;
                    }
                }
            }
        } else {
            listenKey = transportService().startListening();
        }
        String address = listenKey.address();
        try {
            if (home.length() > 0) {
                String os_arch = System.getProperty("os.arch");
                if ("SunOS".equals(System.getProperty("os.name")) &&
                   ("sparcv9".equals(os_arch) || "amd64".equals(os_arch))) {
                    exePath = home + File.separator + "bin" + File.separator +
                        os_arch + File.separator + exe;
                } else {
                    exePath = home + File.separator + "bin" + File.separator + exe;
                }
            } else {
                exePath = exe;
            }
            if (hasWhitespace(exe)) {
                exePath = quote + exePath + quote;
            }
            String xrun = "transport=" + transport().name() +
                          ",address=" + address +
                          ",suspend=" + (wait? 'y' : 'n');
            if (hasWhitespace(xrun)) {
                xrun = quote + xrun + quote;
            }
            String command = exePath + ' ' +
                             options + ' ' +
                             "-Xdebug " +
                             "-Xrunjdwp:" + xrun + ' ' +
                             mainClassAndArgs;
            vm = launch(tokenizeCommand(command, quote.charAt(0)), address, listenKey,
                        transportService());
        } finally {
            transportService().stopListening(listenKey);
        }
        return vm;
    }
    public String name() {
        return "com.sun.jdi.CommandLineLaunch";
    }
    public String description() {
        return getString("sun.description");
    }
}

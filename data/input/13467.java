public class SimpleLaunchingConnector implements LaunchingConnector {
    TransportService ts;
    String ARG_NAME = "class";
    static class StringArgumentImpl implements Connector.StringArgument {
        String name;
        String label;
        String description;
        String value;
        StringArgumentImpl(String name, String label, String description, String value) {
            this.name = name;
            this.label = label;
            this.description = description;
            this.value = value;
        }
        public String name() {
            return name;
        }
        public String label() {
            return label;
        }
        public String description() {
            return description;
        }
        public String value() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
        public boolean isValid(String value) {
            if (value.length() > 0) {
                return true;
            }
            return false;
        }
        public boolean mustSpecify() {
            return true;
        }
    }
    public SimpleLaunchingConnector() {
        try {
            Class c = Class.forName("com.sun.tools.jdi.SocketTransportService");
            ts = (TransportService)c.newInstance();
        } catch (Exception x) {
            throw new Error(x);
        }
    }
    public String name() {
        return "SimpleLaunchingConnector";
    }
    public String description() {
        return "SimpleLaunchingConnector";
    }
    public Transport transport() {
        return new Transport() {
            public String name() {
                return ts.name();
            }
        };
    }
    public Map defaultArguments() {
        HashMap map = new HashMap();
        map.put(ARG_NAME,
                new StringArgumentImpl(ARG_NAME, "class name", "class name", ""));
        return map;
    }
    public VirtualMachine launch(Map<String, ? extends Connector.Argument> arguments) throws
                              IOException,
                              IllegalConnectorArgumentsException,
                              VMStartException {
        String className = ((StringArgumentImpl)arguments.get(ARG_NAME)).value();
        if (className.length() == 0) {
            throw new IllegalConnectorArgumentsException("class name missing", ARG_NAME);
        }
        TransportService.ListenKey key = ts.startListening();
        String exe = System.getProperty("java.home") + File.separator + "bin" +
            File.separator;
        String arch = System.getProperty("os.arch");
        String osname = System.getProperty("os.name");
        if (osname.equals("SunOS") && arch.equals("sparcv9")) {
            exe += "sparcv9/java";
        } else if (osname.equals("SunOS") && arch.equals("amd64")) {
            exe += "amd64/java";
        } else {
            exe += "java";
        }
        String cmd = exe + " -Xdebug -Xrunjdwp:transport=dt_socket,timeout=15000,address=" +
            key.address() +
            " -classpath " + System.getProperty("test.classes") +
            " " + className;
        Process process = Runtime.getRuntime().exec(cmd);
        Connection conn = ts.accept(key, 30*1000, 9*1000);
        ts.stopListening(key);
        return Bootstrap.virtualMachineManager().createVirtualMachine(conn);
    }
}

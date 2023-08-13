public class Svc {
    public static abstract class Command {
        private String mName;
        public Command(String name) {
            mName = name;
        }
        public String name() {
            return mName;
        }
        public abstract String shortHelp();         
        public abstract String longHelp();          
        public abstract void run(String[] args);    
    }
    public static void main(String[] args) {
        if (args.length >= 1) {
            Command c = lookupCommand(args[0]);
            if (c != null) {
                c.run(args);
                return;
            }
        }
        COMMAND_HELP.run(args);
    }
    private static Command lookupCommand(String name) {
        final int N = COMMANDS.length;
        for (int i=0; i<N; i++) {
            Command c = COMMANDS[i];
            if (c.name().equals(name)) {
                return c;
            }
        }
        return null;
    }
    public static final Command COMMAND_HELP = new Command("help") {
        public String shortHelp() {
            return "Show information about the subcommands";
        }
        public String longHelp() {
            return shortHelp();
        }
        public void run(String[] args) {
            if (args.length == 2) {
                Command c = lookupCommand(args[1]);
                if (c != null) {
                    System.err.println(c.longHelp());
                    return;
                }
            }
            System.err.println("Available commands:");
            final int N = COMMANDS.length;
            int maxlen = 0;
            for (int i=0; i<N; i++) {
                Command c = COMMANDS[i];
                int len = c.name().length();
                if (maxlen < len) {
                    maxlen = len;
                }
            }
            String format = "    %-" + maxlen + "s    %s";
            for (int i=0; i<N; i++) {
                Command c = COMMANDS[i];
                System.err.println(String.format(format, c.name(), c.shortHelp()));
            }
        }
    };
    public static final Command[] COMMANDS = new Command[] {
            COMMAND_HELP,
            new PowerCommand(),
            new DataCommand(),
            new WifiCommand()
    };
}

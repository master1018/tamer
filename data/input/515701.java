final class emma
{
    public static void main (final String [] args)
        throws EMMARuntimeException
    {
        if ((args.length == 0) || args [0].startsWith ("-h"))
        {
            System.out.println (USAGE);
            return;
        }
        final String commandName = args [0];
        final String [] commandArgs = new String [args.length - 1];
        System.arraycopy (args, 1, commandArgs, 0, commandArgs.length);
        final Command command = Command.create (commandName, "emma ".concat (commandName), commandArgs);
        command.run ();
    }
    private static final String EOL = System.getProperty ("line.separator", "\n"); 
    private static final String USAGE =
    "emma usage: emma <command> [command options]," + EOL +
    "  where <command> is one of:" + EOL +
    EOL +
    "   run     application runner {same as 'emmarun' tool};" + EOL +
    "   instr   offline instrumentation processor;" + EOL +
    "   report  offline report generator;" + EOL +
    "   merge   offline data file merge processor." + EOL +
    EOL +
    "  {use '<command> -h' to see usage help for a given command}" + EOL +
    EOL +
    IAppConstants.APP_USAGE_BUILD_ID;
} 

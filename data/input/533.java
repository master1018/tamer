public class Utils {
    private static Logger logger = LogManager.getLogger("org.blue.star.registry.agent.config.utils.Utils");
    private static String cn = "org.blue.star.visualisation.registry.agent.config.utils.Utils";
    public static boolean checkIsFile(String fileName) {
        return new File(fileName).exists();
    }
    public static int setRegistryPort(String value) throws AgentConfigurationException {
        try {
            return getIntegerValueOfString(value);
        } catch (AgentConfigurationException e) {
            logger.debug(cn + ".setRegistryPort() - NumberFormatException Parsing Registry Port");
            throw new AgentConfigurationException("Registry Port Must be a Valid Integer");
        }
    }
    public static int getIntegerValueOfString(String value) throws AgentConfigurationException {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            logger.debug(cn + ".getIntegerValueOfString() - NumberFormatException");
            throw new AgentConfigurationException(e);
        }
    }
    public static int setTimeOut(String value) throws AgentConfigurationException {
        try {
            return getIntegerValueOfString(value);
        } catch (AgentConfigurationException e) {
            logger.debug(cn + ".setTimeOut() - NumberFormatException Parsing TimeOut value");
            throw new AgentConfigurationException("Timeout Value must be a Valid Integer");
        }
    }
    public static boolean setAllowCommandArguments(String value) throws AgentConfigurationException {
        try {
            return convertToBooleanValue(value);
        } catch (IllegalArgumentException e) {
            logger.debug(cn + ".setAllowCommandArguments() - Invalid Boolean Value passed");
            throw new AgentConfigurationException("allow_command_arguments Must Be Either 0 or 1");
        }
    }
    public static boolean convertToBooleanValue(String value) throws IllegalArgumentException {
        if (value.equals("1")) return true; else if (value.equals("0")) return false; else throw new IllegalArgumentException("Non-valid Boolean value");
    }
    public static String getCommandName(String value) throws AgentConfigurationException {
        int bindex = value.indexOf("[");
        int eindex = value.indexOf("]");
        if (bindex == -1 || eindex == -1) throw new AgentConfigurationException("Invalid Command Definition in definition '" + value + "'");
        return value.substring(bindex + 1, eindex);
    }
}

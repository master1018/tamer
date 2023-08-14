public class DdmConsole {
    private static IDdmConsole mConsole;
    public static void printErrorToConsole(String message) {
        if (mConsole != null) {
            mConsole.printErrorToConsole(message);
        } else {
            System.err.println(message);
        }
    }
    public static void printErrorToConsole(String[] messages) {
        if (mConsole != null) {
            mConsole.printErrorToConsole(messages);
        } else {
            for (String message : messages) {
                System.err.println(message);
            }
        }
    }
    public static void printToConsole(String message) {
        if (mConsole != null) {
            mConsole.printToConsole(message);
        } else {
            System.out.println(message);
        }
    }
    public static void printToConsole(String[] messages) {
        if (mConsole != null) {
            mConsole.printToConsole(messages);
        } else {
            for (String message : messages) {
                System.out.println(message);
            }
        }
    }
    public static void setConsole(IDdmConsole console) {
        mConsole = console;
    }
}

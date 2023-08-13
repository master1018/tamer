public class MessageOutput {
    static ResourceBundle textResources;
    private static MessageFormat messageFormat;
    static void fatalError(String messageKey) {
        System.err.println();
        System.err.println(format("Fatal error"));
        System.err.println(format(messageKey));
        Env.shutdown();
    }
    static String format(String key) {
        return (textResources.getString(key));
    }
    static String format(String key, String argument) {
        return format(key, new Object [] {argument});
    }
    static synchronized String format(String key, Object [] arguments) {
        if (messageFormat == null) {
            messageFormat = new MessageFormat (textResources.getString(key));
        } else {
            messageFormat.applyPattern (textResources.getString(key));
        }
        return (messageFormat.format (arguments));
    }
    static void printDirectln(String line) {
        System.out.println(line);
    }
    static void printDirect(String line) {
        System.out.print(line);
    }
    static void printDirect(char c) {
        System.out.print(c);
    }
    static void println() {
        System.out.println();
    }
    static void print(String key) {
        System.out.print(format(key));
    }
    static void println(String key) {
        System.out.println(format(key));
    }
    static void print(String key, String argument) {
        System.out.print(format(key, argument));
    }
    static void println(String key, String argument) {
        System.out.println(format(key, argument));
    }
    static void println(String key, Object [] arguments) {
        System.out.println(format(key, arguments));
    }
    static void lnprint(String key) {
        System.out.println();
        System.out.print(textResources.getString(key));
    }
    static void lnprint(String key, String argument) {
        System.out.println();
        System.out.print(format(key, argument));
    }
    static void lnprint(String key, Object [] arguments) {
        System.out.println();
        System.out.print(format(key, arguments));
    }
    static void printException(String key, Exception e) {
        if (key != null) {
            try {
                println(key);
            } catch (MissingResourceException mex) {
                printDirectln(key);
            }
        }
        System.out.flush();
        e.printStackTrace();
    }
    static void printPrompt() {
        ThreadInfo threadInfo = ThreadInfo.getCurrentThreadInfo();
        if (threadInfo == null) {
            System.out.print
                (MessageOutput.format("jdb prompt with no current thread"));
        } else {
            System.out.print
                (MessageOutput.format("jdb prompt thread name and current stack frame",
                                      new Object [] {
                                          threadInfo.getThread().name(),
                                          new Integer (threadInfo.getCurrentFrameIndex() + 1)}));
        }
        System.out.flush();
    }
}

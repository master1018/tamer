public class PrefsSpi {
    public static void main (String[] args) throws Exception {
        if (args.length != 1)
            throw new Exception("Usage: java PrefsSpi REGEXP");
        String className = Preferences.userRoot().getClass().getName();
        System.out.printf("className=%s%n", className);
        if (! className.matches(args[0]))
            throw new Exception("Preferences class name \"" + className
                                + "\" does not match regular expression \""
                                + args[0] + "\".");
    }
}

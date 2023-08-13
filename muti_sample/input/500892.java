public final class ProcessBuilder {
    private List<String> command;
    private File directory;
    private Map<String, String> environment;
    private boolean redirectErrorStream;
    public ProcessBuilder(String... command) {
        this(toList(command));
    }
    public ProcessBuilder(List<String> command) {
        super();
        if (command == null) {
            throw new NullPointerException();
        }
        this.command = command;
        this.environment = System.getenv();
    }
    public List<String> command() {
        return command;
    }
    public ProcessBuilder command(String... command) {
        return command(toList(command));
    }
    public ProcessBuilder command(List<String> command) {
        if (command == null) {
            throw new NullPointerException();
        }
        this.command = command;
        return this;
    }
    public File directory() {
        return directory;
    }
    public ProcessBuilder directory(File directory) {
        this.directory = directory;
        return this;
    }
    public Map<String, String> environment() {
        return environment;
    }
    public boolean redirectErrorStream() {
        return redirectErrorStream;
    }
    public ProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }
    public Process start() throws IOException {
        String[] cmdArray = command.toArray(new String[command.size()]);
        String[] envArray = new String[environment.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : environment.entrySet()) {
            envArray[i++] = entry.getKey() + "=" + entry.getValue(); 
        }
        return ProcessManager.getInstance().exec(cmdArray, envArray, directory, redirectErrorStream);
    }
    private static List<String> toList(String[] strings) {
        ArrayList<String> arrayList = new ArrayList<String>(strings.length);
        for (String string : strings) {
            arrayList.add(string);
        }
        return arrayList;
    }
}

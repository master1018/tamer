public class Zombies {
    public static void main(String[] args) throws Throwable {
        if (! new File("/usr/bin/perl").canExecute() ||
            ! new File("/bin/ps").canExecute())
            return;
        System.out.println("Looks like a Unix system.");
        final Runtime rt = Runtime.getRuntime();
        try {
            rt.exec("no-such-file");
            throw new Error("expected IOException not thrown");
        } catch (IOException _) {}
        try {
            rt.exec(".");
            throw new Error("expected IOException not thrown");
        } catch (IOException _) {}
        try {
            rt.exec("/bin/true", null, new File("no-such-dir"));
            throw new Error("expected IOException not thrown");
        } catch (IOException _) {}
        rt.exec("/bin/true").waitFor();
        final String[] zombieCounter = {
            "/usr/bin/perl", "-e",
            "exit @{[`/bin/ps -eo ppid,s` =~ /^ *@{[getppid]} +Z$/mog]}"
        };
        int zombies = rt.exec(zombieCounter).waitFor();
        if (zombies != 0) throw new Error(zombies + " zombies!");
    }
}

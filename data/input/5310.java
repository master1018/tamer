public class SleepyCat {
    private static void destroy (Process[] deathRow) {
        for (int i = 0; i < deathRow.length; ++i)
            if (deathRow[i] != null)
                deathRow[i].destroy();
    }
    static class TimeoutTask extends TimerTask {
        private Process[] deathRow;
        private boolean timedOut;
        TimeoutTask (Process[] deathRow) {
            this.deathRow = deathRow;
            this.timedOut = false;
        }
        public void run() {
            timedOut = true;
            destroy(deathRow);
        }
        public boolean timedOut() {
            return timedOut;
        }
    }
    private static boolean hang1() throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        int iterations = 20;
        int timeout = 30;
        String[] catArgs   = new String[] {"/bin/cat"};
        String[] sleepArgs = new String[] {"/bin/sleep",
                                            String.valueOf(timeout+1)};
        Process[] cats   = new Process[iterations];
        Process[] sleeps = new Process[iterations];
        Timer timer = new Timer(true);
        TimeoutTask catExecutioner = new TimeoutTask(cats);
        timer.schedule(catExecutioner, timeout * 1000);
        for (int i = 0; i < cats.length; ++i) {
            cats[i] = rt.exec(catArgs);
            java.io.OutputStream s = cats[i].getOutputStream();
            Process sleep = rt.exec(sleepArgs);
            s.close(); 
            sleeps[i] = sleep;
        }
        for (int i = 0; i < cats.length; ++i)
            cats[i].waitFor(); 
        timer.cancel();
        destroy(sleeps);
        if (catExecutioner.timedOut())
            System.out.println("Child process has a hidden writable pipe fd for its stdin.");
        return catExecutioner.timedOut();
    }
    private static boolean hang2() throws Exception {
        Runtime rt = Runtime.getRuntime();
        int iterations = 10;
        Timer timer = new Timer(true);
        int timeout = 30;
        Process[] backgroundSleepers = new Process[iterations];
        TimeoutTask sleeperExecutioner = new TimeoutTask(backgroundSleepers);
        timer.schedule(sleeperExecutioner, timeout * 1000);
        byte[] buffer = new byte[10];
        String[] args =
            new String[] {"/bin/sh", "-c",
                          "exec sleep " + (timeout+1) + " >/dev/null"};
        for (int i = 0;
             i < backgroundSleepers.length && !sleeperExecutioner.timedOut();
             ++i) {
            backgroundSleepers[i] = rt.exec(args); 
            try {
                if (backgroundSleepers[i].getInputStream().read() != -1)
                    throw new Exception("Expected EOF, got a byte");
            } catch (IOException e) {
                break;
            }
        }
        timer.cancel();
        destroy(backgroundSleepers);
        if (sleeperExecutioner.timedOut())
            System.out.println("Child process has two (should be one) writable pipe fds for its stdout.");
        return sleeperExecutioner.timedOut();
    }
    public static void main (String[] args) throws Exception {
        try {
            if (hang1() | hang2())
                throw new Exception("Read from closed pipe hangs");
        } catch (IOException e) {
        }
    }
}

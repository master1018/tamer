public class FeelingLucky {
    public static void main(String[] args) throws Throwable {
        final Runtime rt = Runtime.getRuntime();
        final String[] pidPrinter = {"/bin/sh", "-c", "echo $$"};
        final Process minedProcess = rt.exec(pidPrinter);
        int minedPid = 0;
        final InputStream is = minedProcess.getInputStream();
        int c;
        while ((c = is.read()) >= '0' && c <= '9')
            minedPid = 10 * minedPid + (c - '0');
        System.out.printf("minedPid=%d%n", minedPid);
        minedProcess.waitFor();
        final String[] magnum = {
            "perl", "-e",
            "my $punk = getppid();" +
            "open TTY, '> /dev/tty';" +
            "print TTY \"punk=$punk\\n\";" +
            "for (my $i = 0; $i < 32768; $i++) {" +
            "  my $pid = fork();" +
            "  if ($pid == 0) {" +
            "    if ($$ == " + minedPid + ") {" +
            "      print TTY \"MATCH $$ $punk\\n\";" +
            "      $SIG{TERM} = sub {" +
            "        print TTY \"Got TERM $$ $punk\\n\";" +
            "        kill 9, $punk;" +
            "        exit; };" +
            "      $| = 1; print 'Go ahead.  Make my day.';" +
            "      sleep 100;" +
            "    }" +
            "    exit;" +
            "  } else { " +
            "    waitpid($pid,0);" +
            "    break if $pid == " + minedPid + ";" +
            "  }" +
            "}"
        };
        Process p = rt.exec(magnum);
        if (p.getInputStream().read() != -1) {
            System.out.println("got a char");
            minedProcess.destroy();
            Thread.sleep(1000);
        }
    }
}

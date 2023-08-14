final class ReTraceRunnable implements Runnable
{
    private final JTextArea consoleTextArea;
    private final boolean   verbose;
    private final File      mappingFile;
    private final String    stackTrace;
    public ReTraceRunnable(JTextArea consoleTextArea,
                           boolean   verbose,
                           File      mappingFile,
                           String    stackTrace)
    {
        this.consoleTextArea = consoleTextArea;
        this.verbose         = verbose;
        this.mappingFile     = mappingFile;
        this.stackTrace      = stackTrace;
    }
    public void run()
    {
        consoleTextArea.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        consoleTextArea.setText("");
        InputStream oldIn  = System.in;
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        ByteArrayInputStream inputStream =
           new ByteArrayInputStream(stackTrace.getBytes());
        PrintStream printStream =
            new PrintStream(new TextAreaOutputStream(consoleTextArea), true);
        System.setIn(inputStream);
        System.setOut(printStream);
        System.setErr(printStream);
        try
        {
            ReTrace reTrace = new ReTrace(ReTrace.STACK_TRACE_EXPRESSION,
                                          verbose,
                                          mappingFile);
            reTrace.execute();
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
            MessageDialogRunnable.showMessageDialog(consoleTextArea,
                                                    ex.getMessage(),
                                                    msg("errorReTracing"),
                                                    JOptionPane.ERROR_MESSAGE);
        }
        catch (OutOfMemoryError er)
        {
            System.gc();
            System.out.println(msg("outOfMemory"));
            MessageDialogRunnable.showMessageDialog(consoleTextArea,
                                                    msg("outOfMemory"),
                                                    msg("errorReTracing"),
                                                    JOptionPane.ERROR_MESSAGE);
        }
        printStream.flush();
        System.setIn(oldIn);
        System.setOut(oldOut);
        System.setErr(oldErr);
        consoleTextArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        consoleTextArea.setCaretPosition(0);
        ProGuardGUI.systemOutRedirected = false;
    }
    private String msg(String messageKey)
    {
         return GUIResources.getMessage(messageKey);
    }
}

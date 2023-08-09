final class ProGuardRunnable implements Runnable
{
    private final JTextArea     consoleTextArea;
    private final Configuration configuration;
    private final String        configurationFileName;
    public ProGuardRunnable(JTextArea     consoleTextArea,
                            Configuration configuration,
                            String        configurationFileName)
    {
        this.consoleTextArea       = consoleTextArea;
        this.configuration         = configuration;
        this.configurationFileName = configurationFileName;
    }
    public void run()
    {
        consoleTextArea.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        consoleTextArea.setText("");
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        PrintStream printStream =
            new PrintStream(new TextAreaOutputStream(consoleTextArea), true);
        System.setOut(printStream);
        System.setErr(printStream);
        try
        {
            ProGuard proGuard = new ProGuard(configuration);
            proGuard.execute();
            System.out.println("Processing completed successfully");
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
            MessageDialogRunnable.showMessageDialog(consoleTextArea,
                                                    ex.getMessage(),
                                                    msg("errorProcessing"),
                                                    JOptionPane.ERROR_MESSAGE);
        }
        catch (OutOfMemoryError er)
        {
            System.gc();
            System.out.println(msg("outOfMemoryInfo", configurationFileName));
            MessageDialogRunnable.showMessageDialog(consoleTextArea,
                                                    msg("outOfMemory"),
                                                    msg("errorProcessing"),
                                                    JOptionPane.ERROR_MESSAGE);
        }
        finally
        {
            printStream.close();
            System.setOut(oldOut);
            System.setErr(oldErr);
        }
        consoleTextArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        ProGuardGUI.systemOutRedirected = false;
    }
    private String msg(String messageKey)
    {
         return GUIResources.getMessage(messageKey);
    }
    private String msg(String messageKey,
                       Object messageArgument)
    {
         return GUIResources.getMessage(messageKey, new Object[] {messageArgument});
    }
}

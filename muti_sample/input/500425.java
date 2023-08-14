final class MessageDialogRunnable implements Runnable
{
    private final Component parentComponent;
    private final Object    message;
    private final String    title;
    private final int       messageType;
    public static void showMessageDialog(Component parentComponent,
                                         Object    message,
                                         String    title,
                                         int       messageType)
    {
        try
        {
            SwingUtil.invokeAndWait(new MessageDialogRunnable(parentComponent,
                                                              message,
                                                              title,
                                                              messageType));
        }
        catch (Exception e)
        {
        }
    }
    public MessageDialogRunnable(Component parentComponent,
                                 Object    message,
                                 String    title,
                                 int       messageType)
    {
        this.parentComponent = parentComponent;
        this.message         = message;
        this.title           = title;
        this.messageType     = messageType;
    }
    public void run()
    {
        JOptionPane.showMessageDialog(parentComponent,
                                      message,
                                      title,
                                      messageType);
    }
}

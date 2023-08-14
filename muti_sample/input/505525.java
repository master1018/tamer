class GUIResources
{
    private static final ResourceBundle messages  = ResourceBundle.getBundle(GUIResources.class.getName());
    private static final MessageFormat  formatter = new MessageFormat("");
    public static String getMessage(String messageKey)
    {
        return messages.getString(messageKey);
    }
    public static String getMessage(String messageKey, Object[] messageArguments)
    {
        formatter.applyPattern(messages.getString(messageKey));
        return formatter.format(messageArguments);
    }
}

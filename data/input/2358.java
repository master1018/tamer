public class DialogCallbackHandler implements CallbackHandler {
    private Component parentComponent;
    private static final int JPasswordFieldLen = 8 ;
    private static final int JTextFieldLen = 8 ;
    public DialogCallbackHandler() { }
    public DialogCallbackHandler(Component parentComponent) {
        this.parentComponent = parentComponent;
    }
    private static interface Action {
         void perform();
    }
    public void handle(Callback[] callbacks)
        throws UnsupportedCallbackException
    {
        final List<Object> messages = new ArrayList<>(3);
        final List<Action> okActions = new ArrayList<>(2);
        ConfirmationInfo confirmation = new ConfirmationInfo();
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof TextOutputCallback) {
                TextOutputCallback tc = (TextOutputCallback) callbacks[i];
                switch (tc.getMessageType()) {
                case TextOutputCallback.INFORMATION:
                    confirmation.messageType = JOptionPane.INFORMATION_MESSAGE;
                    break;
                case TextOutputCallback.WARNING:
                    confirmation.messageType = JOptionPane.WARNING_MESSAGE;
                    break;
                case TextOutputCallback.ERROR:
                    confirmation.messageType = JOptionPane.ERROR_MESSAGE;
                    break;
                default:
                    throw new UnsupportedCallbackException(
                        callbacks[i], "Unrecognized message type");
                }
                messages.add(tc.getMessage());
            } else if (callbacks[i] instanceof NameCallback) {
                final NameCallback nc = (NameCallback) callbacks[i];
                JLabel prompt = new JLabel(nc.getPrompt());
                final JTextField name = new JTextField(JTextFieldLen);
                String defaultName = nc.getDefaultName();
                if (defaultName != null) {
                    name.setText(defaultName);
                }
                Box namePanel = Box.createHorizontalBox();
                namePanel.add(prompt);
                namePanel.add(name);
                messages.add(namePanel);
                okActions.add(new Action() {
                    public void perform() {
                        nc.setName(name.getText());
                    }
                });
            } else if (callbacks[i] instanceof PasswordCallback) {
                final PasswordCallback pc = (PasswordCallback) callbacks[i];
                JLabel prompt = new JLabel(pc.getPrompt());
                final JPasswordField password =
                                        new JPasswordField(JPasswordFieldLen);
                if (!pc.isEchoOn()) {
                    password.setEchoChar('*');
                }
                Box passwordPanel = Box.createHorizontalBox();
                passwordPanel.add(prompt);
                passwordPanel.add(password);
                messages.add(passwordPanel);
                okActions.add(new Action() {
                    public void perform() {
                        pc.setPassword(password.getPassword());
                    }
                });
            } else if (callbacks[i] instanceof ConfirmationCallback) {
                ConfirmationCallback cc = (ConfirmationCallback)callbacks[i];
                confirmation.setCallback(cc);
                if (cc.getPrompt() != null) {
                    messages.add(cc.getPrompt());
                }
            } else {
                throw new UnsupportedCallbackException(
                    callbacks[i], "Unrecognized Callback");
            }
        }
        int result = JOptionPane.showOptionDialog(
            parentComponent,
            messages.toArray(),
            "Confirmation",                     
            confirmation.optionType,
            confirmation.messageType,
            null,                               
            confirmation.options,               
            confirmation.initialValue);         
        if (result == JOptionPane.OK_OPTION
            || result == JOptionPane.YES_OPTION)
        {
            Iterator<Action> iterator = okActions.iterator();
            while (iterator.hasNext()) {
                iterator.next().perform();
            }
        }
        confirmation.handleResult(result);
    }
    private static class ConfirmationInfo {
        private int[] translations;
        int optionType = JOptionPane.OK_CANCEL_OPTION;
        Object[] options = null;
        Object initialValue = null;
        int messageType = JOptionPane.QUESTION_MESSAGE;
        private ConfirmationCallback callback;
        void setCallback(ConfirmationCallback callback)
            throws UnsupportedCallbackException
        {
            this.callback = callback;
            int confirmationOptionType = callback.getOptionType();
            switch (confirmationOptionType) {
            case ConfirmationCallback.YES_NO_OPTION:
                optionType = JOptionPane.YES_NO_OPTION;
                translations = new int[] {
                    JOptionPane.YES_OPTION, ConfirmationCallback.YES,
                    JOptionPane.NO_OPTION, ConfirmationCallback.NO,
                    JOptionPane.CLOSED_OPTION, ConfirmationCallback.NO
                };
                break;
            case ConfirmationCallback.YES_NO_CANCEL_OPTION:
                optionType = JOptionPane.YES_NO_CANCEL_OPTION;
                translations = new int[] {
                    JOptionPane.YES_OPTION, ConfirmationCallback.YES,
                    JOptionPane.NO_OPTION, ConfirmationCallback.NO,
                    JOptionPane.CANCEL_OPTION, ConfirmationCallback.CANCEL,
                    JOptionPane.CLOSED_OPTION, ConfirmationCallback.CANCEL
                };
                break;
            case ConfirmationCallback.OK_CANCEL_OPTION:
                optionType = JOptionPane.OK_CANCEL_OPTION;
                translations = new int[] {
                    JOptionPane.OK_OPTION, ConfirmationCallback.OK,
                    JOptionPane.CANCEL_OPTION, ConfirmationCallback.CANCEL,
                    JOptionPane.CLOSED_OPTION, ConfirmationCallback.CANCEL
                };
                break;
            case ConfirmationCallback.UNSPECIFIED_OPTION:
                options = callback.getOptions();
                translations = new int[] {
                    JOptionPane.CLOSED_OPTION, callback.getDefaultOption()
                };
                break;
            default:
                throw new UnsupportedCallbackException(
                    callback,
                    "Unrecognized option type: " + confirmationOptionType);
            }
            int confirmationMessageType = callback.getMessageType();
            switch (confirmationMessageType) {
            case ConfirmationCallback.WARNING:
                messageType = JOptionPane.WARNING_MESSAGE;
                break;
            case ConfirmationCallback.ERROR:
                messageType = JOptionPane.ERROR_MESSAGE;
                break;
            case ConfirmationCallback.INFORMATION:
                messageType = JOptionPane.INFORMATION_MESSAGE;
                break;
            default:
                throw new UnsupportedCallbackException(
                    callback,
                    "Unrecognized message type: " + confirmationMessageType);
            }
        }
        void handleResult(int result) {
            if (callback == null) {
                return;
            }
            for (int i = 0; i < translations.length; i += 2) {
                if (translations[i] == result) {
                    result = translations[i + 1];
                    break;
                }
            }
            callback.setSelectedIndex(result);
        }
    }
}

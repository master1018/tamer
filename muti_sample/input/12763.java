public class PrintingStatus {
    private final PrinterJob job;
    private final Component parent;
    private JDialog abortDialog;
    private JButton abortButton;
    private JLabel statusLabel;
    private MessageFormat statusFormat;
    private final AtomicBoolean isAborted = new AtomicBoolean(false);
    private final Action abortAction = new AbstractAction() {
        public void actionPerformed(ActionEvent ae) {
            if (!isAborted.get()) {
                isAborted.set(true);
                abortButton.setEnabled(false);
                abortDialog.setTitle(
                    UIManager.getString("PrintingDialog.titleAbortingText"));
                statusLabel.setText(
                    UIManager.getString("PrintingDialog.contentAbortingText"));
                job.cancel();
            }
        }
    };
    private final WindowAdapter closeListener = new WindowAdapter() {
        public void windowClosing(WindowEvent we) {
            abortAction.actionPerformed(null);
        }
    };
    public static PrintingStatus
            createPrintingStatus(Component parent, PrinterJob job) {
        return new PrintingStatus(parent, job);
    }
    protected PrintingStatus(Component parent, PrinterJob job) {
        this.job = job;
        this.parent = parent;
    }
    private void init() {
        String progressTitle =
            UIManager.getString("PrintingDialog.titleProgressText");
        String dialogInitialContent =
            UIManager.getString("PrintingDialog.contentInitialText");
        statusFormat = new MessageFormat(
            UIManager.getString("PrintingDialog.contentProgressText"));
        String abortText =
            UIManager.getString("PrintingDialog.abortButtonText");
        String abortTooltip =
            UIManager.getString("PrintingDialog.abortButtonToolTipText");
        int abortMnemonic =
            getInt("PrintingDialog.abortButtonMnemonic", -1);
        int abortMnemonicIndex =
            getInt("PrintingDialog.abortButtonDisplayedMnemonicIndex", -1);
        abortButton = new JButton(abortText);
        abortButton.addActionListener(abortAction);
        abortButton.setToolTipText(abortTooltip);
        if (abortMnemonic != -1) {
            abortButton.setMnemonic(abortMnemonic);
        }
        if (abortMnemonicIndex != -1) {
            abortButton.setDisplayedMnemonicIndex(abortMnemonicIndex);
        }
        statusLabel = new JLabel(dialogInitialContent);
        JOptionPane abortPane = new JOptionPane(statusLabel,
            JOptionPane.INFORMATION_MESSAGE,
            JOptionPane.DEFAULT_OPTION,
            null, new Object[]{abortButton},
            abortButton);
        abortPane.getActionMap().put("close", abortAction);
        if (parent != null && parent.getParent() instanceof JViewport) {
            abortDialog =
                    abortPane.createDialog(parent.getParent(), progressTitle);
        } else {
            abortDialog = abortPane.createDialog(parent, progressTitle);
        }
        abortDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        abortDialog.addWindowListener(closeListener);
    }
    public void showModal(final boolean isModal) {
        if (SwingUtilities.isEventDispatchThread()) {
            showModalOnEDT(isModal);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        showModalOnEDT(isModal);
                    }
                });
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            } catch(InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                   throw (RuntimeException) cause;
                } else if (cause instanceof Error) {
                   throw (Error) cause;
                } else {
                   throw new RuntimeException(cause);
                }
            }
        }
    }
    private void showModalOnEDT(boolean isModal) {
        assert SwingUtilities.isEventDispatchThread();
        init();
        abortDialog.setModal(isModal);
        abortDialog.setVisible(true);
    }
    public void dispose() {
        if (SwingUtilities.isEventDispatchThread()) {
            disposeOnEDT();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    disposeOnEDT();
                }
            });
        }
    }
    private void disposeOnEDT() {
        assert SwingUtilities.isEventDispatchThread();
        if (abortDialog != null) {
            abortDialog.removeWindowListener(closeListener);
            abortDialog.dispose();
            abortDialog = null;
        }
    }
    public boolean isAborted() {
        return isAborted.get();
    }
    public Printable createNotificationPrintable(Printable printable) {
        return new NotificationPrintable(printable);
    }
    private class NotificationPrintable implements Printable {
        private final Printable printDelegatee;
        public NotificationPrintable(Printable delegatee) {
            if (delegatee == null) {
                throw new NullPointerException("Printable is null");
            }
            this.printDelegatee = delegatee;
        }
        public int print(final Graphics graphics,
                         final PageFormat pageFormat, final int pageIndex)
                throws PrinterException {
            final int retVal =
                printDelegatee.print(graphics, pageFormat, pageIndex);
            if (retVal != NO_SUCH_PAGE && !isAborted()) {
                if (SwingUtilities.isEventDispatchThread()) {
                    updateStatusOnEDT(pageIndex);
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            updateStatusOnEDT(pageIndex);
                        }
                    });
                }
            }
            return retVal;
        }
        private void updateStatusOnEDT(int pageIndex) {
            assert SwingUtilities.isEventDispatchThread();
            Object[] pageNumber = new Object[]{
                new Integer(pageIndex + 1)};
            statusLabel.setText(statusFormat.format(pageNumber));
        }
    }
    static int getInt(Object key, int defaultValue) {
        Object value = UIManager.get(key);
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch(NumberFormatException nfe) {
            }
        }
        return defaultValue;
    }
}

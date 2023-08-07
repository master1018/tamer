public class ChatDialog extends JFrame implements ActionListener {
    @Locales
    private ResourceBundle messages;
    @In
    private MessagePoint msgPoint;
    @In
    private PrivateMessagePoint privPoint;
    @In
    private Configuration config;
    @In
    private PingListener pingListener;
    @In
    private HistoryKeeper historyKeeper;
    @In
    private MessageParser messageParser;
    @In
    private HideAllMessageEventsAction hideAllMessageEventsAction;
    private static LinkedList<ChatDialog> instances = new LinkedList<ChatDialog>();
    private JTextArea userInputTextArea;
    private JList listOfUsers;
    private JEditorPane chatEditorPane;
    private boolean chatDialogActive = true;
    private int messageCounter;
    @Init
    public void init() {
        instances.add(this);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setTitle(messages.getString("chat_dialog"));
        JPanel main = new JPanel(new BorderLayout());
        this.setLayout(new BorderLayout());
        this.add(main);
        listOfUsers = new JList(new MyUsersDataModel());
        listOfUsers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listOfUsers.setSelectedIndex(0);
        listOfUsers.setFocusable(false);
        JScrollPane listOfUsersScrolPane = new JScrollPane(listOfUsers);
        listOfUsersScrolPane.setPreferredSize(new Dimension(120, 50));
        listOfUsersScrolPane.setAlignmentX(LEFT_ALIGNMENT);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        userInputTextArea = new JTextArea();
        userInputTextArea.setFont(bottomPanel.getFont());
        userInputTextArea.setWrapStyleWord(true);
        userInputTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (e.getModifiers() == 0) {
                        e.setKeyCode(0);
                        actionPerformed(new ActionEvent(this, 0, "send"));
                    } else {
                        userInputTextArea.replaceRange("\n", userInputTextArea.getSelectionStart(), userInputTextArea.getSelectionEnd());
                    }
                }
            }
        });
        JScrollPane userInputScrollPane = new JScrollPane(userInputTextArea);
        userInputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        userInputScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        userInputScrollPane.setPreferredSize(new Dimension(150, 40));
        bottomPanel.add(userInputScrollPane);
        chatEditorPane = createEditorPane();
        fillWithHistory();
        final JScrollPane chatScrollPane = new JScrollPane(chatEditorPane);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        chatScrollPane.setPreferredSize(new Dimension(450, 255));
        chatScrollPane.setMinimumSize(new Dimension(10, 10));
        final PropertyChangeListener historyListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                MessageEvent message = (MessageEvent) evt.getNewValue();
                synchronized (chatEditorPane) {
                    addMessageEvent(message);
                }
            }
        };
        historyKeeper.addPropertyChangeListener(historyListener);
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JSplitPane listOfUsersAndChatSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listOfUsersScrolPane, chatScrollPane);
        listOfUsersAndChatSplitPane.setOneTouchExpandable(true);
        listOfUsersAndChatSplitPane.setDividerLocation(150);
        listOfUsersAndChatSplitPane.setContinuousLayout(true);
        listOfUsersAndChatSplitPane.setBorder(BorderFactory.createEmptyBorder());
        centerPanel.add(listOfUsersAndChatSplitPane);
        main.add(centerPanel, BorderLayout.CENTER);
        main.add(bottomPanel, BorderLayout.SOUTH);
        WindowAdapter myWindowAdapter = new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                super.windowGainedFocus(e);
                setMessageCounter(0);
                chatDialogActive = true;
                userInputTextArea.requestFocus();
                hideAllMessageEventsAction.actionPerformed(null);
            }
            @Override
            public void windowLostFocus(WindowEvent e) {
                super.windowLostFocus(e);
                chatDialogActive = false;
            }
            @Override
            public void windowClosed(WindowEvent e) {
                instances.remove(ChatDialog.this);
                pingListener.removePropertyChangeListener(((MyUsersDataModel) listOfUsers.getModel()).myPingListener);
                historyKeeper.removePropertyChangeListener(historyListener);
                super.windowClosed(e);
            }
            @Override
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e);
            }
        };
        this.addWindowFocusListener(myWindowAdapter);
        this.addWindowListener(myWindowAdapter);
        pack();
        setLocationRelativeTo(null);
    }
    private class MyUsersDataModel extends AbstractListModel {
        ArrayList<String> activeUsers;
        PropertyChangeListener myPingListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                refreshList();
                fireContentsChanged(MyUsersDataModel.this, 0, MyUsersDataModel.this.getSize());
            }
        };
        void refreshList() {
            activeUsers = pingListener.getActiveUsers();
        }
        public MyUsersDataModel() {
            pingListener.addPropertyChangeListener(myPingListener);
        }
        @Override
        public int getSize() {
            if (activeUsers == null) {
                refreshList();
            }
            return activeUsers == null ? 1 : activeUsers.size() + 1;
        }
        @Override
        public Object getElementAt(int index) {
            if (index == 0) {
                return messages.getString("send_message_dialog_all");
            }
            if (activeUsers == null) {
                refreshList();
            }
            return activeUsers == null ? null : activeUsers.get(index - 1);
        }
    }
    public void setSelectedUsers(String... preselectedUsers) {
        listOfUsers.setSelectedIndex(-1);
        ArrayList<String> activeUsers = pingListener.getActiveUsers();
        for (String pre : preselectedUsers) {
            for (int i = 0; i < activeUsers.size(); i++) {
                Object k = activeUsers.get(i);
                if (pre.equals(k)) {
                    listOfUsers.setSelectedValue(k, true);
                }
            }
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if ("ok".equals(e.getActionCommand()) || "send".equals(e.getActionCommand())) {
            String text = userInputTextArea.getText();
            userInputTextArea.setText(null);
            Message preparedMessage = new Message(config.getNick(), text);
            for (Object oUser : listOfUsers.getSelectedValues()) {
                String sUser = (String) oUser;
                InetAddress ipUser = pingListener.getAddressOfUser(sUser);
                if (ipUser != null) {
                    privPoint.sendMessage(preparedMessage, ipUser);
                } else if (messages.getString("send_message_dialog_all").equals(sUser)) {
                    msgPoint.sendMessage(preparedMessage);
                    break;
                }
            }
            if ("ok".equals(e.getActionCommand())) {
                setVisible(false);
            }
        }
    }
    public boolean isChatDialogActive() {
        return chatDialogActive;
    }
    public static LinkedList<ChatDialog> getVisibleChatDialogs() {
        return instances;
    }
    public static ChatDialog getActiveChatDialog() {
        for (ChatDialog dlg : getVisibleChatDialogs()) {
            if (dlg.isChatDialogActive()) {
                return dlg;
            }
        }
        return null;
    }
    private JTextPane createTextPane() {
        JTextPane textPane = new JTextPane();
        StyledDocument doc = textPane.getStyledDocument();
        textPane.setEditable(false);
        return textPane;
    }
    private JEditorPane createEditorPane() {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setFocusable(true);
        editorPane.setBackground(userInputTextArea.getBackground());
        editorPane.setOpaque(true);
        editorPane.addHyperlinkListener(hyperlinkListener);
        ((HTMLDocument) editorPane.getDocument()).getStyleSheet().addRule(String.format("p { font-family: %s; font-size: %dpx; margin: 0px; }", config.getMessageLabelFontName(), config.getMessageLabelFontSize() - 1));
        return editorPane;
    }
    private void setMessageCounter(int counter) {
        messageCounter = counter;
        this.setTitle(messages.getString("chat_dialog") + (messageCounter > 0 ? " (" + messageCounter + ")" : ""));
    }
    private final StringBuffer chatBuffer = new StringBuffer();
    private String messageToHtml(MessageEvent message) {
        if (message == null || chatBuffer == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        String parsed = messageParser.parseMessage(message.getMessage(), false);
        result.append("<p>");
        if ((message.getMessage().getFlags() & Message.MESSAGE_FLAG_SELF) > 0) {
            result.append("<font style=\"background-color:silver\">");
            result.append("&gt;&gt;&gt;");
            result.append("</font>");
            result.append(" ");
        }
        result.append("<b>" + message.getMessage().getSender() + "</b>");
        result.append(" ");
        result.append("<small>");
        result.append(SimpleDateFormat.getDateTimeInstance().format(message.getMessage().getDate()));
        result.append("</small>");
        result.append(": ");
        if ((message.getMessage().getFlags() & Message.MESSAGE_FLAG_PRIVATE) > 0) {
            result.append("<font style=\"background-color:" + Integer.toHexString(config.getPrivateMessageColor().getRGB() & 0xFFFFFF) + "\">");
            result.append(parsed);
            result.append("</font>");
        } else if (message.getEvent() == MessageEvent.EventType.ERROR) {
            result.append("<font style=\"background-color:" + Integer.toHexString(config.getErrorMessageColor().getRGB() & 0xFFFFFF) + "\">");
            result.append(parsed);
            result.append("</font>");
        } else if (message.getEvent() == MessageEvent.EventType.INFO) {
            result.append("<font style=\"background-color:" + Integer.toHexString(config.getInfoMessageColor().getRGB() & 0xFFFFFF) + "\">");
            result.append(parsed);
            result.append("</font>");
        } else {
            result.append(parsed);
        }
        result.append("</p>\r\n");
        return result.toString();
    }
    private void appendMessageToBuffer(StringBuffer append, MessageEvent message) {
        if (message == null || append == null) {
            return;
        }
        String parsed = messageToHtml(message);
        append.append(parsed);
    }
    private void addMessageEvent(final MessageEvent message) {
        (new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                synchronized (chatBuffer) {
                    if (message == null) {
                        return null;
                    }
                    String parsed = messageToHtml(message);
                    chatBuffer.append(parsed);
                    return parsed;
                }
            }
            @Override
            protected void done() {
                try {
                    if (get() == null) {
                        chatEditorPane.setText(chatBuffer.toString());
                        chatEditorPane.scrollRectToVisible(new Rectangle(0, Integer.MAX_VALUE - 1, 1, 1));
                    } else {
                        Document doc = (Document) chatEditorPane.getDocument();
                        EditorKit ek = chatEditorPane.getEditorKit();
                        ((HTMLEditorKit) ek).insertHTML((HTMLDocument) doc, doc.getEndPosition().getOffset() - 1, get(), 1, 0, null);
                        chatEditorPane.scrollRectToVisible(new Rectangle(0, Integer.MAX_VALUE - 1, 1, 1));
                        if (!chatDialogActive) {
                            setMessageCounter(1 + messageCounter);
                        }
                    }
                } catch (BadLocationException ex) {
                    Logger.getLogger(ChatDialog.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ChatDialog.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(ChatDialog.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ChatDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).execute();
    }
    private void fillWithHistory() {
        synchronized (chatBuffer) {
            synchronized (chatEditorPane) {
                synchronized (historyKeeper.getHistory()) {
                    for (MessageEvent msg : historyKeeper.getHistory()) {
                        appendMessageToBuffer(chatBuffer, msg);
                    }
                }
            }
        }
        addMessageEvent(null);
    }
    private HyperlinkListener hyperlinkListener = new HyperlinkListener() {
        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                JEditorPane pane = (JEditorPane) e.getSource();
                if (e instanceof HTMLFrameHyperlinkEvent) {
                    HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
                    HTMLDocument doc = (HTMLDocument) pane.getDocument();
                    doc.processHTMLFrameHyperlinkEvent(evt);
                } else {
                    try {
                        if (Desktop.isDesktopSupported() && (Desktop.getDesktop() != null)) {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }
    };
}

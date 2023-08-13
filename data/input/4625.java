public class BasicOptionPaneUI extends OptionPaneUI {
    public static final int MinimumWidth = 262;
    public static final int MinimumHeight = 90;
    private static String newline;
    protected JOptionPane         optionPane;
    protected Dimension minimumSize;
    protected JComponent          inputComponent;
    protected Component           initialFocusComponent;
    protected boolean             hasCustomComponents;
    protected PropertyChangeListener propertyChangeListener;
    private Handler handler;
    static {
        newline = java.security.AccessController.doPrivileged(
                                new GetPropertyAction("line.separator"));
        if (newline == null) {
            newline = "\n";
        }
    }
    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions(Actions.CLOSE));
        BasicLookAndFeel.installAudioActionMap(map);
    }
    public static ComponentUI createUI(JComponent x) {
        return new BasicOptionPaneUI();
    }
    public void installUI(JComponent c) {
        optionPane = (JOptionPane)c;
        installDefaults();
        optionPane.setLayout(createLayoutManager());
        installComponents();
        installListeners();
        installKeyboardActions();
    }
    public void uninstallUI(JComponent c) {
        uninstallComponents();
        optionPane.setLayout(null);
        uninstallKeyboardActions();
        uninstallListeners();
        uninstallDefaults();
        optionPane = null;
    }
    protected void installDefaults() {
        LookAndFeel.installColorsAndFont(optionPane, "OptionPane.background",
                                         "OptionPane.foreground", "OptionPane.font");
        LookAndFeel.installBorder(optionPane, "OptionPane.border");
        minimumSize = UIManager.getDimension("OptionPane.minimumSize");
        LookAndFeel.installProperty(optionPane, "opaque", Boolean.TRUE);
    }
    protected void uninstallDefaults() {
        LookAndFeel.uninstallBorder(optionPane);
    }
    protected void installComponents() {
        optionPane.add(createMessageArea());
        Container separator = createSeparator();
        if (separator != null) {
            optionPane.add(separator);
        }
        optionPane.add(createButtonArea());
        optionPane.applyComponentOrientation(optionPane.getComponentOrientation());
    }
    protected void uninstallComponents() {
        hasCustomComponents = false;
        inputComponent = null;
        initialFocusComponent = null;
        optionPane.removeAll();
    }
    protected LayoutManager createLayoutManager() {
        return new BoxLayout(optionPane, BoxLayout.Y_AXIS);
    }
    protected void installListeners() {
        if ((propertyChangeListener = createPropertyChangeListener()) != null) {
            optionPane.addPropertyChangeListener(propertyChangeListener);
        }
    }
    protected void uninstallListeners() {
        if (propertyChangeListener != null) {
            optionPane.removePropertyChangeListener(propertyChangeListener);
            propertyChangeListener = null;
        }
        handler = null;
    }
    protected PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }
    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }
    protected void installKeyboardActions() {
        InputMap map = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        SwingUtilities.replaceUIInputMap(optionPane, JComponent.
                                       WHEN_IN_FOCUSED_WINDOW, map);
        LazyActionMap.installLazyActionMap(optionPane, BasicOptionPaneUI.class,
                                           "OptionPane.actionMap");
    }
    protected void uninstallKeyboardActions() {
        SwingUtilities.replaceUIInputMap(optionPane, JComponent.
                                       WHEN_IN_FOCUSED_WINDOW, null);
        SwingUtilities.replaceUIActionMap(optionPane, null);
    }
    InputMap getInputMap(int condition) {
        if (condition == JComponent.WHEN_IN_FOCUSED_WINDOW) {
            Object[] bindings = (Object[])DefaultLookup.get(
                             optionPane, this, "OptionPane.windowBindings");
            if (bindings != null) {
                return LookAndFeel.makeComponentInputMap(optionPane, bindings);
            }
        }
        return null;
    }
    public Dimension getMinimumOptionPaneSize() {
        if (minimumSize == null) {
            return new Dimension(MinimumWidth, MinimumHeight);
        }
        return new Dimension(minimumSize.width,
                             minimumSize.height);
    }
    public Dimension getPreferredSize(JComponent c) {
        if (c == optionPane) {
            Dimension            ourMin = getMinimumOptionPaneSize();
            LayoutManager        lm = c.getLayout();
            if (lm != null) {
                Dimension         lmSize = lm.preferredLayoutSize(c);
                if (ourMin != null)
                    return new Dimension
                        (Math.max(lmSize.width, ourMin.width),
                         Math.max(lmSize.height, ourMin.height));
                return lmSize;
            }
            return ourMin;
        }
        return null;
    }
    protected Container createMessageArea() {
        JPanel top = new JPanel();
        Border topBorder = (Border)DefaultLookup.get(optionPane, this,
                                             "OptionPane.messageAreaBorder");
        if (topBorder != null) {
            top.setBorder(topBorder);
        }
        top.setLayout(new BorderLayout());
        Container          body = new JPanel(new GridBagLayout());
        Container          realBody = new JPanel(new BorderLayout());
        body.setName("OptionPane.body");
        realBody.setName("OptionPane.realBody");
        if (getIcon() != null) {
            JPanel sep = new JPanel();
            sep.setName("OptionPane.separator");
            sep.setPreferredSize(new Dimension(15, 1));
            realBody.add(sep, BorderLayout.BEFORE_LINE_BEGINS);
        }
        realBody.add(body, BorderLayout.CENTER);
        GridBagConstraints cons = new GridBagConstraints();
        cons.gridx = cons.gridy = 0;
        cons.gridwidth = GridBagConstraints.REMAINDER;
        cons.gridheight = 1;
        cons.anchor = DefaultLookup.getInt(optionPane, this,
                      "OptionPane.messageAnchor", GridBagConstraints.CENTER);
        cons.insets = new Insets(0,0,3,0);
        addMessageComponents(body, cons, getMessage(),
                          getMaxCharactersPerLineCount(), false);
        top.add(realBody, BorderLayout.CENTER);
        addIcon(top);
        return top;
    }
    protected void addMessageComponents(Container container,
                                     GridBagConstraints cons,
                                     Object msg, int maxll,
                                     boolean internallyCreated) {
        if (msg == null) {
            return;
        }
        if (msg instanceof Component) {
            if (msg instanceof JScrollPane || msg instanceof JPanel) {
                cons.fill = GridBagConstraints.BOTH;
                cons.weighty = 1;
            } else {
                cons.fill = GridBagConstraints.HORIZONTAL;
            }
            cons.weightx = 1;
            container.add((Component) msg, cons);
            cons.weightx = 0;
            cons.weighty = 0;
            cons.fill = GridBagConstraints.NONE;
            cons.gridy++;
            if (!internallyCreated) {
                hasCustomComponents = true;
            }
        } else if (msg instanceof Object[]) {
            Object [] msgs = (Object[]) msg;
            for (Object o : msgs) {
                addMessageComponents(container, cons, o, maxll, false);
            }
        } else if (msg instanceof Icon) {
            JLabel label = new JLabel( (Icon)msg, SwingConstants.CENTER );
            configureMessageLabel(label);
            addMessageComponents(container, cons, label, maxll, true);
        } else {
            String s = msg.toString();
            int len = s.length();
            if (len <= 0) {
                return;
            }
            int nl;
            int nll = 0;
            if ((nl = s.indexOf(newline)) >= 0) {
                nll = newline.length();
            } else if ((nl = s.indexOf("\r\n")) >= 0) {
                nll = 2;
            } else if ((nl = s.indexOf('\n')) >= 0) {
                nll = 1;
            }
            if (nl >= 0) {
                if (nl == 0) {
                    JPanel breakPanel = new JPanel() {
                        public Dimension getPreferredSize() {
                            Font       f = getFont();
                            if (f != null) {
                                return new Dimension(1, f.getSize() + 2);
                            }
                            return new Dimension(0, 0);
                        }
                    };
                    breakPanel.setName("OptionPane.break");
                    addMessageComponents(container, cons, breakPanel, maxll,
                                         true);
                } else {
                    addMessageComponents(container, cons, s.substring(0, nl),
                                      maxll, false);
                }
                addMessageComponents(container, cons, s.substring(nl + nll), maxll,
                                  false);
            } else if (len > maxll) {
                Container c = Box.createVerticalBox();
                c.setName("OptionPane.verticalBox");
                burstStringInto(c, s, maxll);
                addMessageComponents(container, cons, c, maxll, true );
            } else {
                JLabel label;
                label = new JLabel( s, JLabel.LEADING );
                label.setName("OptionPane.label");
                configureMessageLabel(label);
                addMessageComponents(container, cons, label, maxll, true);
            }
        }
    }
    protected Object getMessage() {
        inputComponent = null;
        if (optionPane != null) {
            if (optionPane.getWantsInput()) {
                Object             message = optionPane.getMessage();
                Object[]           sValues = optionPane.getSelectionValues();
                Object             inputValue = optionPane
                                           .getInitialSelectionValue();
                JComponent         toAdd;
                if (sValues != null) {
                    if (sValues.length < 20) {
                        JComboBox            cBox = new JComboBox();
                        cBox.setName("OptionPane.comboBox");
                        for(int counter = 0, maxCounter = sValues.length;
                            counter < maxCounter; counter++) {
                            cBox.addItem(sValues[counter]);
                        }
                        if (inputValue != null) {
                            cBox.setSelectedItem(inputValue);
                        }
                        inputComponent = cBox;
                        toAdd = cBox;
                    } else {
                        JList                list = new JList(sValues);
                        JScrollPane          sp = new JScrollPane(list);
                        sp.setName("OptionPane.scrollPane");
                        list.setName("OptionPane.list");
                        list.setVisibleRowCount(10);
                        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        if(inputValue != null)
                            list.setSelectedValue(inputValue, true);
                        list.addMouseListener(getHandler());
                        toAdd = sp;
                        inputComponent = list;
                    }
                } else {
                    MultiplexingTextField   tf = new MultiplexingTextField(20);
                    tf.setName("OptionPane.textField");
                    tf.setKeyStrokes(new KeyStroke[] {
                                     KeyStroke.getKeyStroke("ENTER") } );
                    if (inputValue != null) {
                        String inputString = inputValue.toString();
                        tf.setText(inputString);
                        tf.setSelectionStart(0);
                        tf.setSelectionEnd(inputString.length());
                    }
                    tf.addActionListener(getHandler());
                    toAdd = inputComponent = tf;
                }
                Object[]           newMessage;
                if (message == null) {
                    newMessage = new Object[1];
                    newMessage[0] = toAdd;
                } else {
                    newMessage = new Object[2];
                    newMessage[0] = message;
                    newMessage[1] = toAdd;
                }
                return newMessage;
            }
            return optionPane.getMessage();
        }
        return null;
    }
    protected void addIcon(Container top) {
        Icon                  sideIcon = getIcon();
        if (sideIcon != null) {
            JLabel            iconLabel = new JLabel(sideIcon);
            iconLabel.setName("OptionPane.iconLabel");
            iconLabel.setVerticalAlignment(SwingConstants.TOP);
            top.add(iconLabel, BorderLayout.BEFORE_LINE_BEGINS);
        }
    }
    protected Icon getIcon() {
        Icon      mIcon = (optionPane == null ? null : optionPane.getIcon());
        if(mIcon == null && optionPane != null)
            mIcon = getIconForType(optionPane.getMessageType());
        return mIcon;
    }
    protected Icon getIconForType(int messageType) {
        if(messageType < 0 || messageType > 3)
            return null;
        String propertyName = null;
        switch(messageType) {
        case 0:
            propertyName = "OptionPane.errorIcon";
            break;
        case 1:
            propertyName = "OptionPane.informationIcon";
            break;
        case 2:
            propertyName = "OptionPane.warningIcon";
            break;
        case 3:
            propertyName = "OptionPane.questionIcon";
            break;
        }
        if (propertyName != null) {
            return (Icon)DefaultLookup.get(optionPane, this, propertyName);
        }
        return null;
    }
    protected int getMaxCharactersPerLineCount() {
        return optionPane.getMaxCharactersPerLineCount();
    }
    protected void burstStringInto(Container c, String d, int maxll) {
        int len = d.length();
        if (len <= 0)
            return;
        if (len > maxll) {
            int p = d.lastIndexOf(' ', maxll);
            if (p <= 0)
                p = d.indexOf(' ', maxll);
            if (p > 0 && p < len) {
                burstStringInto(c, d.substring(0, p), maxll);
                burstStringInto(c, d.substring(p + 1), maxll);
                return;
            }
        }
        JLabel label = new JLabel(d, JLabel.LEFT);
        label.setName("OptionPane.label");
        configureMessageLabel(label);
        c.add(label);
    }
    protected Container createSeparator() {
        return null;
    }
    protected Container createButtonArea() {
        JPanel bottom = new JPanel();
        Border border = (Border)DefaultLookup.get(optionPane, this,
                                          "OptionPane.buttonAreaBorder");
        bottom.setName("OptionPane.buttonArea");
        if (border != null) {
            bottom.setBorder(border);
        }
        bottom.setLayout(new ButtonAreaLayout(
           DefaultLookup.getBoolean(optionPane, this,
                                    "OptionPane.sameSizeButtons", true),
           DefaultLookup.getInt(optionPane, this, "OptionPane.buttonPadding",
                                6),
           DefaultLookup.getInt(optionPane, this,
                        "OptionPane.buttonOrientation", SwingConstants.CENTER),
           DefaultLookup.getBoolean(optionPane, this, "OptionPane.isYesLast",
                                    false)));
        addButtonComponents(bottom, getButtons(), getInitialValueIndex());
        return bottom;
    }
    protected void addButtonComponents(Container container, Object[] buttons,
                                 int initialIndex) {
        if (buttons != null && buttons.length > 0) {
            boolean            sizeButtonsToSame = getSizeButtonsToSameWidth();
            boolean            createdAll = true;
            int                numButtons = buttons.length;
            JButton[]          createdButtons = null;
            int                maxWidth = 0;
            if (sizeButtonsToSame) {
                createdButtons = new JButton[numButtons];
            }
            for(int counter = 0; counter < numButtons; counter++) {
                Object       button = buttons[counter];
                Component    newComponent;
                if (button instanceof Component) {
                    createdAll = false;
                    newComponent = (Component)button;
                    container.add(newComponent);
                    hasCustomComponents = true;
                } else {
                    JButton      aButton;
                    if (button instanceof ButtonFactory) {
                        aButton = ((ButtonFactory)button).createButton();
                    }
                    else if (button instanceof Icon)
                        aButton = new JButton((Icon)button);
                    else
                        aButton = new JButton(button.toString());
                    aButton.setName("OptionPane.button");
                    aButton.setMultiClickThreshhold(DefaultLookup.getInt(
                          optionPane, this, "OptionPane.buttonClickThreshhold",
                          0));
                    configureButton(aButton);
                    container.add(aButton);
                    ActionListener buttonListener = createButtonActionListener(counter);
                    if (buttonListener != null) {
                        aButton.addActionListener(buttonListener);
                    }
                    newComponent = aButton;
                }
                if (sizeButtonsToSame && createdAll &&
                   (newComponent instanceof JButton)) {
                    createdButtons[counter] = (JButton)newComponent;
                    maxWidth = Math.max(maxWidth,
                                        newComponent.getMinimumSize().width);
                }
                if (counter == initialIndex) {
                    initialFocusComponent = newComponent;
                    if (initialFocusComponent instanceof JButton) {
                        JButton defaultB = (JButton)initialFocusComponent;
                        defaultB.addHierarchyListener(new HierarchyListener() {
                            public void hierarchyChanged(HierarchyEvent e) {
                                if ((e.getChangeFlags() &
                                        HierarchyEvent.PARENT_CHANGED) != 0) {
                                    JButton defaultButton = (JButton) e.getComponent();
                                    JRootPane root =
                                            SwingUtilities.getRootPane(defaultButton);
                                    if (root != null) {
                                        root.setDefaultButton(defaultButton);
                                    }
                                }
                            }
                        });
                    }
                }
            }
            ((ButtonAreaLayout)container.getLayout()).
                              setSyncAllWidths((sizeButtonsToSame && createdAll));
            if (DefaultLookup.getBoolean(optionPane, this,
                   "OptionPane.setButtonMargin", true) && sizeButtonsToSame &&
                   createdAll) {
                JButton               aButton;
                int                   padSize;
                padSize = (numButtons <= 2? 8 : 4);
                for(int counter = 0; counter < numButtons; counter++) {
                    aButton = createdButtons[counter];
                    aButton.setMargin(new Insets(2, padSize, 2, padSize));
                }
            }
        }
    }
    protected ActionListener createButtonActionListener(int buttonIndex) {
        return new ButtonActionListener(buttonIndex);
    }
    protected Object[] getButtons() {
        if (optionPane != null) {
            Object[] suppliedOptions = optionPane.getOptions();
            if (suppliedOptions == null) {
                Object[] defaultOptions;
                int type = optionPane.getOptionType();
                Locale l = optionPane.getLocale();
                int minimumWidth =
                    DefaultLookup.getInt(optionPane, this,
                                        "OptionPane.buttonMinimumWidth",-1);
                if (type == JOptionPane.YES_NO_OPTION) {
                    defaultOptions = new ButtonFactory[2];
                    defaultOptions[0] = new ButtonFactory(
                        UIManager.getString("OptionPane.yesButtonText", l),
                        getMnemonic("OptionPane.yesButtonMnemonic", l),
                        (Icon)DefaultLookup.get(optionPane, this,
                                          "OptionPane.yesIcon"), minimumWidth);
                    defaultOptions[1] = new ButtonFactory(
                        UIManager.getString("OptionPane.noButtonText", l),
                        getMnemonic("OptionPane.noButtonMnemonic", l),
                        (Icon)DefaultLookup.get(optionPane, this,
                                          "OptionPane.noIcon"), minimumWidth);
                } else if (type == JOptionPane.YES_NO_CANCEL_OPTION) {
                    defaultOptions = new ButtonFactory[3];
                    defaultOptions[0] = new ButtonFactory(
                        UIManager.getString("OptionPane.yesButtonText", l),
                        getMnemonic("OptionPane.yesButtonMnemonic", l),
                        (Icon)DefaultLookup.get(optionPane, this,
                                          "OptionPane.yesIcon"), minimumWidth);
                    defaultOptions[1] = new ButtonFactory(
                        UIManager.getString("OptionPane.noButtonText",l),
                        getMnemonic("OptionPane.noButtonMnemonic", l),
                        (Icon)DefaultLookup.get(optionPane, this,
                                          "OptionPane.noIcon"), minimumWidth);
                    defaultOptions[2] = new ButtonFactory(
                        UIManager.getString("OptionPane.cancelButtonText",l),
                        getMnemonic("OptionPane.cancelButtonMnemonic", l),
                        (Icon)DefaultLookup.get(optionPane, this,
                                          "OptionPane.cancelIcon"), minimumWidth);
                } else if (type == JOptionPane.OK_CANCEL_OPTION) {
                    defaultOptions = new ButtonFactory[2];
                    defaultOptions[0] = new ButtonFactory(
                        UIManager.getString("OptionPane.okButtonText",l),
                        getMnemonic("OptionPane.okButtonMnemonic", l),
                        (Icon)DefaultLookup.get(optionPane, this,
                                          "OptionPane.okIcon"), minimumWidth);
                    defaultOptions[1] = new ButtonFactory(
                        UIManager.getString("OptionPane.cancelButtonText",l),
                        getMnemonic("OptionPane.cancelButtonMnemonic", l),
                        (Icon)DefaultLookup.get(optionPane, this,
                                          "OptionPane.cancelIcon"), minimumWidth);
                } else {
                    defaultOptions = new ButtonFactory[1];
                    defaultOptions[0] = new ButtonFactory(
                        UIManager.getString("OptionPane.okButtonText",l),
                        getMnemonic("OptionPane.okButtonMnemonic", l),
                        (Icon)DefaultLookup.get(optionPane, this,
                                          "OptionPane.okIcon"), minimumWidth);
                }
                return defaultOptions;
            }
            return suppliedOptions;
        }
        return null;
    }
    private int getMnemonic(String key, Locale l) {
        String value = (String)UIManager.get(key, l);
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException nfe) { }
        return 0;
    }
    protected boolean getSizeButtonsToSameWidth() {
        return true;
    }
    protected int getInitialValueIndex() {
        if (optionPane != null) {
            Object             iv = optionPane.getInitialValue();
            Object[]           options = optionPane.getOptions();
            if(options == null) {
                return 0;
            }
            else if(iv != null) {
                for(int counter = options.length - 1; counter >= 0; counter--){
                    if(options[counter].equals(iv))
                        return counter;
                }
            }
        }
        return -1;
    }
    protected void resetInputValue() {
        if(inputComponent != null && (inputComponent instanceof JTextField)) {
            optionPane.setInputValue(((JTextField)inputComponent).getText());
        } else if(inputComponent != null &&
                  (inputComponent instanceof JComboBox)) {
            optionPane.setInputValue(((JComboBox)inputComponent)
                                     .getSelectedItem());
        } else if(inputComponent != null) {
            optionPane.setInputValue(((JList)inputComponent)
                                     .getSelectedValue());
        }
    }
    public void selectInitialValue(JOptionPane op) {
        if (inputComponent != null)
            inputComponent.requestFocus();
        else {
            if (initialFocusComponent != null)
                initialFocusComponent.requestFocus();
            if (initialFocusComponent instanceof JButton) {
                JRootPane root = SwingUtilities.getRootPane(initialFocusComponent);
                if (root != null) {
                    root.setDefaultButton((JButton)initialFocusComponent);
                }
            }
        }
    }
    public boolean containsCustomComponents(JOptionPane op) {
        return hasCustomComponents;
    }
    public static class ButtonAreaLayout implements LayoutManager {
        protected boolean           syncAllWidths;
        protected int               padding;
        protected boolean           centersChildren;
        private int orientation;
        private boolean reverseButtons;
        private boolean useOrientation;
        public ButtonAreaLayout(boolean syncAllWidths, int padding) {
            this.syncAllWidths = syncAllWidths;
            this.padding = padding;
            centersChildren = true;
            useOrientation = false;
        }
        ButtonAreaLayout(boolean syncAllSizes, int padding, int orientation,
                         boolean reverseButtons) {
            this(syncAllSizes, padding);
            useOrientation = true;
            this.orientation = orientation;
            this.reverseButtons = reverseButtons;
        }
        public void setSyncAllWidths(boolean newValue) {
            syncAllWidths = newValue;
        }
        public boolean getSyncAllWidths() {
            return syncAllWidths;
        }
        public void setPadding(int newPadding) {
            this.padding = newPadding;
        }
        public int getPadding() {
            return padding;
        }
        public void setCentersChildren(boolean newValue) {
            centersChildren = newValue;
            useOrientation = false;
        }
        public boolean getCentersChildren() {
            return centersChildren;
        }
        private int getOrientation(Container container) {
            if (!useOrientation) {
                return SwingConstants.CENTER;
            }
            if (container.getComponentOrientation().isLeftToRight()) {
                return orientation;
            }
            switch (orientation) {
            case SwingConstants.LEFT:
                return SwingConstants.RIGHT;
            case SwingConstants.RIGHT:
                return SwingConstants.LEFT;
            case SwingConstants.CENTER:
                return SwingConstants.CENTER;
            }
            return SwingConstants.LEFT;
        }
        public void addLayoutComponent(String string, Component comp) {
        }
        public void layoutContainer(Container container) {
            Component[]      children = container.getComponents();
            if(children != null && children.length > 0) {
                int               numChildren = children.length;
                Insets            insets = container.getInsets();
                int maxWidth = 0;
                int maxHeight = 0;
                int totalButtonWidth = 0;
                int x = 0;
                int xOffset = 0;
                boolean ltr = container.getComponentOrientation().
                                        isLeftToRight();
                boolean reverse = (ltr) ? reverseButtons : !reverseButtons;
                for(int counter = 0; counter < numChildren; counter++) {
                    Dimension pref = children[counter].getPreferredSize();
                    maxWidth = Math.max(maxWidth, pref.width);
                    maxHeight = Math.max(maxHeight, pref.height);
                    totalButtonWidth += pref.width;
                }
                if (getSyncAllWidths()) {
                    totalButtonWidth = maxWidth * numChildren;
                }
                totalButtonWidth += (numChildren - 1) * padding;
                switch (getOrientation(container)) {
                case SwingConstants.LEFT:
                    x = insets.left;
                    break;
                case SwingConstants.RIGHT:
                    x = container.getWidth() - insets.right - totalButtonWidth;
                    break;
                case SwingConstants.CENTER:
                    if (getCentersChildren() || numChildren < 2) {
                        x = (container.getWidth() - totalButtonWidth) / 2;
                    }
                    else {
                        x = insets.left;
                        if (getSyncAllWidths()) {
                            xOffset = (container.getWidth() - insets.left -
                                       insets.right - totalButtonWidth) /
                                (numChildren - 1) + maxWidth;
                        }
                        else {
                            xOffset = (container.getWidth() - insets.left -
                                       insets.right - totalButtonWidth) /
                                      (numChildren - 1);
                        }
                    }
                    break;
                }
                for (int counter = 0; counter < numChildren; counter++) {
                    int index = (reverse) ? numChildren - counter - 1 :
                                counter;
                    Dimension pref = children[index].getPreferredSize();
                    if (getSyncAllWidths()) {
                        children[index].setBounds(x, insets.top,
                                                  maxWidth, maxHeight);
                    }
                    else {
                        children[index].setBounds(x, insets.top, pref.width,
                                                  pref.height);
                    }
                    if (xOffset != 0) {
                        x += xOffset;
                    }
                    else {
                        x += children[index].getWidth() + padding;
                    }
                }
            }
        }
        public Dimension minimumLayoutSize(Container c) {
            if(c != null) {
                Component[]       children = c.getComponents();
                if(children != null && children.length > 0) {
                    Dimension     aSize;
                    int           numChildren = children.length;
                    int           height = 0;
                    Insets        cInsets = c.getInsets();
                    int           extraHeight = cInsets.top + cInsets.bottom;
                    int           extraWidth = cInsets.left + cInsets.right;
                    if (syncAllWidths) {
                        int              maxWidth = 0;
                        for(int counter = 0; counter < numChildren; counter++){
                            aSize = children[counter].getPreferredSize();
                            height = Math.max(height, aSize.height);
                            maxWidth = Math.max(maxWidth, aSize.width);
                        }
                        return new Dimension(extraWidth + (maxWidth * numChildren) +
                                             (numChildren - 1) * padding,
                                             extraHeight + height);
                    }
                    else {
                        int        totalWidth = 0;
                        for(int counter = 0; counter < numChildren; counter++){
                            aSize = children[counter].getPreferredSize();
                            height = Math.max(height, aSize.height);
                            totalWidth += aSize.width;
                        }
                        totalWidth += ((numChildren - 1) * padding);
                        return new Dimension(extraWidth + totalWidth, extraHeight + height);
                    }
                }
            }
            return new Dimension(0, 0);
        }
        public Dimension preferredLayoutSize(Container c) {
            return minimumLayoutSize(c);
        }
        public void removeLayoutComponent(Component c) { }
    }
    public class PropertyChangeHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            getHandler().propertyChange(e);
        }
    }
    private void configureMessageLabel(JLabel label) {
        Color color = (Color)DefaultLookup.get(optionPane, this,
                                               "OptionPane.messageForeground");
        if (color != null) {
            label.setForeground(color);
        }
        Font messageFont = (Font)DefaultLookup.get(optionPane, this,
                                                   "OptionPane.messageFont");
        if (messageFont != null) {
            label.setFont(messageFont);
        }
    }
    private void configureButton(JButton button) {
        Font buttonFont = (Font)DefaultLookup.get(optionPane, this,
                                            "OptionPane.buttonFont");
        if (buttonFont != null) {
            button.setFont(buttonFont);
        }
    }
    public class ButtonActionListener implements ActionListener {
        protected int buttonIndex;
        public ButtonActionListener(int buttonIndex) {
            this.buttonIndex = buttonIndex;
        }
        public void actionPerformed(ActionEvent e) {
            if (optionPane != null) {
                int optionType = optionPane.getOptionType();
                Object[] options = optionPane.getOptions();
                if (inputComponent != null) {
                    if (options != null ||
                        optionType == JOptionPane.DEFAULT_OPTION ||
                        ((optionType == JOptionPane.YES_NO_OPTION ||
                         optionType == JOptionPane.YES_NO_CANCEL_OPTION ||
                         optionType == JOptionPane.OK_CANCEL_OPTION) &&
                         buttonIndex == 0)) {
                        resetInputValue();
                    }
                }
                if (options == null) {
                    if (optionType == JOptionPane.OK_CANCEL_OPTION &&
                        buttonIndex == 1) {
                        optionPane.setValue(Integer.valueOf(2));
                    } else {
                        optionPane.setValue(Integer.valueOf(buttonIndex));
                    }
                } else {
                    optionPane.setValue(options[buttonIndex]);
                }
            }
        }
    }
    private class Handler implements ActionListener, MouseListener,
                                     PropertyChangeListener {
        public void actionPerformed(ActionEvent e) {
            optionPane.setInputValue(((JTextField)e.getSource()).getText());
        }
        public void mouseClicked(MouseEvent e) {
        }
        public void mouseReleased(MouseEvent e) {
        }
        public void mouseEntered(MouseEvent e) {
        }
        public void mouseExited(MouseEvent e) {
        }
        public void mousePressed(MouseEvent e) {
            if (e.getClickCount() == 2) {
                JList     list = (JList)e.getSource();
                int       index = list.locationToIndex(e.getPoint());
                optionPane.setInputValue(list.getModel().getElementAt(index));
            }
        }
        public void propertyChange(PropertyChangeEvent e) {
            if(e.getSource() == optionPane) {
                if ("ancestor" == e.getPropertyName()) {
                    JOptionPane op = (JOptionPane)e.getSource();
                    boolean isComingUp;
                    if (e.getOldValue() == null) {
                        isComingUp = true;
                    } else {
                        isComingUp = false;
                    }
                    switch (op.getMessageType()) {
                    case JOptionPane.PLAIN_MESSAGE:
                        if (isComingUp) {
                            BasicLookAndFeel.playSound(optionPane,
                                               "OptionPane.informationSound");
                        }
                        break;
                    case JOptionPane.QUESTION_MESSAGE:
                        if (isComingUp) {
                            BasicLookAndFeel.playSound(optionPane,
                                             "OptionPane.questionSound");
                        }
                        break;
                    case JOptionPane.INFORMATION_MESSAGE:
                        if (isComingUp) {
                            BasicLookAndFeel.playSound(optionPane,
                                             "OptionPane.informationSound");
                        }
                        break;
                    case JOptionPane.WARNING_MESSAGE:
                        if (isComingUp) {
                            BasicLookAndFeel.playSound(optionPane,
                                             "OptionPane.warningSound");
                        }
                        break;
                    case JOptionPane.ERROR_MESSAGE:
                        if (isComingUp) {
                            BasicLookAndFeel.playSound(optionPane,
                                             "OptionPane.errorSound");
                        }
                        break;
                    default:
                        System.err.println("Undefined JOptionPane type: " +
                                           op.getMessageType());
                        break;
                    }
                }
                String         changeName = e.getPropertyName();
                if(changeName == JOptionPane.OPTIONS_PROPERTY ||
                   changeName == JOptionPane.INITIAL_VALUE_PROPERTY ||
                   changeName == JOptionPane.ICON_PROPERTY ||
                   changeName == JOptionPane.MESSAGE_TYPE_PROPERTY ||
                   changeName == JOptionPane.OPTION_TYPE_PROPERTY ||
                   changeName == JOptionPane.MESSAGE_PROPERTY ||
                   changeName == JOptionPane.SELECTION_VALUES_PROPERTY ||
                   changeName == JOptionPane.INITIAL_SELECTION_VALUE_PROPERTY ||
                   changeName == JOptionPane.WANTS_INPUT_PROPERTY) {
                   uninstallComponents();
                   installComponents();
                   optionPane.validate();
                }
                else if (changeName == "componentOrientation") {
                    ComponentOrientation o = (ComponentOrientation)e.getNewValue();
                    JOptionPane op = (JOptionPane)e.getSource();
                    if (o != e.getOldValue()) {
                        op.applyComponentOrientation(o);
                    }
                }
            }
        }
    }
    private static class MultiplexingTextField extends JTextField {
        private KeyStroke[] strokes;
        MultiplexingTextField(int cols) {
            super(cols);
        }
        void setKeyStrokes(KeyStroke[] strokes) {
            this.strokes = strokes;
        }
        protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
                                            int condition, boolean pressed) {
            boolean processed = super.processKeyBinding(ks, e, condition,
                                                        pressed);
            if (processed && condition != JComponent.WHEN_IN_FOCUSED_WINDOW) {
                for (int counter = strokes.length - 1; counter >= 0;
                         counter--) {
                    if (strokes[counter].equals(ks)) {
                        return false;
                    }
                }
            }
            return processed;
        }
    }
    private static class Actions extends UIAction {
        private static final String CLOSE = "close";
        Actions(String key) {
            super(key);
        }
        public void actionPerformed(ActionEvent e) {
            if (getName() == CLOSE) {
                JOptionPane optionPane = (JOptionPane)e.getSource();
                optionPane.setValue(Integer.valueOf(JOptionPane.CLOSED_OPTION));
            }
        }
    }
    private static class ButtonFactory {
        private String text;
        private int mnemonic;
        private Icon icon;
        private int minimumWidth = -1;
        ButtonFactory(String text, int mnemonic, Icon icon, int minimumWidth) {
            this.text = text;
            this.mnemonic = mnemonic;
            this.icon = icon;
            this.minimumWidth = minimumWidth;
        }
        JButton createButton() {
            JButton button;
            if (minimumWidth > 0) {
                button = new ConstrainedButton(text, minimumWidth);
            } else {
                button = new JButton(text);
            }
            if (icon != null) {
                button.setIcon(icon);
            }
            if (mnemonic != 0) {
                button.setMnemonic(mnemonic);
            }
            return button;
        }
        private static class ConstrainedButton extends JButton {
            int minimumWidth;
            ConstrainedButton(String text, int minimumWidth) {
                super(text);
                this.minimumWidth = minimumWidth;
            }
            public Dimension getMinimumSize() {
                Dimension min = super.getMinimumSize();
                min.width = Math.max(min.width, minimumWidth);
                return min;
            }
            public Dimension getPreferredSize() {
                Dimension pref = super.getPreferredSize();
                pref.width = Math.max(pref.width, minimumWidth);
                return pref;
            }
        }
    }
}

public abstract class AbstractButton extends JComponent implements ItemSelectable, SwingConstants {
    public static final String MODEL_CHANGED_PROPERTY = "model";
    public static final String TEXT_CHANGED_PROPERTY = "text";
    public static final String MNEMONIC_CHANGED_PROPERTY = "mnemonic";
    public static final String MARGIN_CHANGED_PROPERTY = "margin";
    public static final String VERTICAL_ALIGNMENT_CHANGED_PROPERTY = "verticalAlignment";
    public static final String HORIZONTAL_ALIGNMENT_CHANGED_PROPERTY = "horizontalAlignment";
    public static final String VERTICAL_TEXT_POSITION_CHANGED_PROPERTY = "verticalTextPosition";
    public static final String HORIZONTAL_TEXT_POSITION_CHANGED_PROPERTY = "horizontalTextPosition";
    public static final String BORDER_PAINTED_CHANGED_PROPERTY = "borderPainted";
    public static final String FOCUS_PAINTED_CHANGED_PROPERTY = "focusPainted";
    public static final String ROLLOVER_ENABLED_CHANGED_PROPERTY = "rolloverEnabled";
    public static final String CONTENT_AREA_FILLED_CHANGED_PROPERTY = "contentAreaFilled";
    public static final String ICON_CHANGED_PROPERTY = "icon";
    public static final String PRESSED_ICON_CHANGED_PROPERTY = "pressedIcon";
    public static final String SELECTED_ICON_CHANGED_PROPERTY = "selectedIcon";
    public static final String ROLLOVER_ICON_CHANGED_PROPERTY = "rolloverIcon";
    public static final String ROLLOVER_SELECTED_ICON_CHANGED_PROPERTY = "rolloverSelectedIcon";
    public static final String DISABLED_ICON_CHANGED_PROPERTY = "disabledIcon";
    public static final String DISABLED_SELECTED_ICON_CHANGED_PROPERTY = "disabledSelectedIcon";
    protected ButtonModel model                = null;
    private String     text                    = ""; 
    private Insets     margin                  = null;
    private Insets     defaultMargin           = null;
    private Icon       defaultIcon             = null;
    private Icon       pressedIcon             = null;
    private Icon       disabledIcon            = null;
    private Icon       selectedIcon            = null;
    private Icon       disabledSelectedIcon    = null;
    private Icon       rolloverIcon            = null;
    private Icon       rolloverSelectedIcon    = null;
    private boolean    paintBorder             = true;
    private boolean    paintFocus              = true;
    private boolean    rolloverEnabled         = false;
    private boolean    contentAreaFilled         = true;
    private int        verticalAlignment       = CENTER;
    private int        horizontalAlignment     = CENTER;
    private int        verticalTextPosition    = CENTER;
    private int        horizontalTextPosition  = TRAILING;
    private int        iconTextGap             = 4;
    private int        mnemonic;
    private int        mnemonicIndex           = -1;
    private long       multiClickThreshhold    = 0;
    private boolean    borderPaintedSet        = false;
    private boolean    rolloverEnabledSet      = false;
    private boolean    iconTextGapSet          = false;
    private boolean    contentAreaFilledSet    = false;
    private boolean setLayout = false;
    boolean defaultCapable = true;
    private Handler handler;
    protected ChangeListener changeListener = null;
    protected ActionListener actionListener = null;
    protected ItemListener itemListener = null;
    protected transient ChangeEvent changeEvent;
    private boolean hideActionText = false;
    public void setHideActionText(boolean hideActionText) {
        if (hideActionText != this.hideActionText) {
            this.hideActionText = hideActionText;
            if (getAction() != null) {
                setTextFromAction(getAction(), false);
            }
            firePropertyChange("hideActionText", !hideActionText,
                               hideActionText);
        }
    }
    public boolean getHideActionText() {
        return hideActionText;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        String oldValue = this.text;
        this.text = text;
        firePropertyChange(TEXT_CHANGED_PROPERTY, oldValue, text);
        updateDisplayedMnemonicIndex(text, getMnemonic());
        if (accessibleContext != null) {
            accessibleContext.firePropertyChange(
                AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                oldValue, text);
        }
        if (text == null || oldValue == null || !text.equals(oldValue)) {
            revalidate();
            repaint();
        }
    }
    public boolean isSelected() {
        return model.isSelected();
    }
    public void setSelected(boolean b) {
        boolean oldValue = isSelected();
        model.setSelected(b);
    }
    public void doClick() {
        doClick(68);
    }
    public void doClick(int pressTime) {
        Dimension size = getSize();
        model.setArmed(true);
        model.setPressed(true);
        paintImmediately(new Rectangle(0,0, size.width, size.height));
        try {
            Thread.currentThread().sleep(pressTime);
        } catch(InterruptedException ie) {
        }
        model.setPressed(false);
        model.setArmed(false);
    }
    public void setMargin(Insets m) {
        if(m instanceof UIResource) {
            defaultMargin = m;
        } else if(margin instanceof UIResource) {
            defaultMargin = margin;
        }
        if(m == null && defaultMargin != null) {
            m = defaultMargin;
        }
        Insets old = margin;
        margin = m;
        firePropertyChange(MARGIN_CHANGED_PROPERTY, old, m);
        if (old == null || !old.equals(m)) {
            revalidate();
            repaint();
        }
    }
    public Insets getMargin() {
        return (margin == null) ? null : (Insets) margin.clone();
    }
    public Icon getIcon() {
        return defaultIcon;
    }
    public void setIcon(Icon defaultIcon) {
        Icon oldValue = this.defaultIcon;
        this.defaultIcon = defaultIcon;
        if (defaultIcon != oldValue && (disabledIcon instanceof UIResource)) {
            disabledIcon = null;
        }
        firePropertyChange(ICON_CHANGED_PROPERTY, oldValue, defaultIcon);
        if (accessibleContext != null) {
            accessibleContext.firePropertyChange(
                AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                oldValue, defaultIcon);
        }
        if (defaultIcon != oldValue) {
            if (defaultIcon == null || oldValue == null ||
                defaultIcon.getIconWidth() != oldValue.getIconWidth() ||
                defaultIcon.getIconHeight() != oldValue.getIconHeight()) {
                revalidate();
            }
            repaint();
        }
    }
    public Icon getPressedIcon() {
        return pressedIcon;
    }
    public void setPressedIcon(Icon pressedIcon) {
        Icon oldValue = this.pressedIcon;
        this.pressedIcon = pressedIcon;
        firePropertyChange(PRESSED_ICON_CHANGED_PROPERTY, oldValue, pressedIcon);
        if (accessibleContext != null) {
            accessibleContext.firePropertyChange(
                AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                oldValue, pressedIcon);
        }
        if (pressedIcon != oldValue) {
            if (getModel().isPressed()) {
                repaint();
            }
        }
    }
    public Icon getSelectedIcon() {
        return selectedIcon;
    }
    public void setSelectedIcon(Icon selectedIcon) {
        Icon oldValue = this.selectedIcon;
        this.selectedIcon = selectedIcon;
        if (selectedIcon != oldValue &&
            disabledSelectedIcon instanceof UIResource) {
            disabledSelectedIcon = null;
        }
        firePropertyChange(SELECTED_ICON_CHANGED_PROPERTY, oldValue, selectedIcon);
        if (accessibleContext != null) {
            accessibleContext.firePropertyChange(
                AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                oldValue, selectedIcon);
        }
        if (selectedIcon != oldValue) {
            if (isSelected()) {
                repaint();
            }
        }
    }
    public Icon getRolloverIcon() {
        return rolloverIcon;
    }
    public void setRolloverIcon(Icon rolloverIcon) {
        Icon oldValue = this.rolloverIcon;
        this.rolloverIcon = rolloverIcon;
        firePropertyChange(ROLLOVER_ICON_CHANGED_PROPERTY, oldValue, rolloverIcon);
        if (accessibleContext != null) {
            accessibleContext.firePropertyChange(
                AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                oldValue, rolloverIcon);
        }
        setRolloverEnabled(true);
        if (rolloverIcon != oldValue) {
            repaint();
        }
    }
    public Icon getRolloverSelectedIcon() {
        return rolloverSelectedIcon;
    }
    public void setRolloverSelectedIcon(Icon rolloverSelectedIcon) {
        Icon oldValue = this.rolloverSelectedIcon;
        this.rolloverSelectedIcon = rolloverSelectedIcon;
        firePropertyChange(ROLLOVER_SELECTED_ICON_CHANGED_PROPERTY, oldValue, rolloverSelectedIcon);
        if (accessibleContext != null) {
            accessibleContext.firePropertyChange(
                AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                oldValue, rolloverSelectedIcon);
        }
        setRolloverEnabled(true);
        if (rolloverSelectedIcon != oldValue) {
            if (isSelected()) {
                repaint();
            }
        }
    }
    @Transient
    public Icon getDisabledIcon() {
        if (disabledIcon == null) {
            disabledIcon = UIManager.getLookAndFeel().getDisabledIcon(this, getIcon());
            if (disabledIcon != null) {
                firePropertyChange(DISABLED_ICON_CHANGED_PROPERTY, null, disabledIcon);
            }
        }
        return disabledIcon;
    }
    public void setDisabledIcon(Icon disabledIcon) {
        Icon oldValue = this.disabledIcon;
        this.disabledIcon = disabledIcon;
        firePropertyChange(DISABLED_ICON_CHANGED_PROPERTY, oldValue, disabledIcon);
        if (accessibleContext != null) {
            accessibleContext.firePropertyChange(
                AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                oldValue, disabledIcon);
        }
        if (disabledIcon != oldValue) {
            if (!isEnabled()) {
                repaint();
            }
        }
    }
    public Icon getDisabledSelectedIcon() {
        if (disabledSelectedIcon == null) {
             if (selectedIcon != null) {
                 disabledSelectedIcon = UIManager.getLookAndFeel().
                         getDisabledSelectedIcon(this, getSelectedIcon());
             } else {
                 return getDisabledIcon();
             }
        }
        return disabledSelectedIcon;
    }
    public void setDisabledSelectedIcon(Icon disabledSelectedIcon) {
        Icon oldValue = this.disabledSelectedIcon;
        this.disabledSelectedIcon = disabledSelectedIcon;
        firePropertyChange(DISABLED_SELECTED_ICON_CHANGED_PROPERTY, oldValue, disabledSelectedIcon);
        if (accessibleContext != null) {
            accessibleContext.firePropertyChange(
                AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                oldValue, disabledSelectedIcon);
        }
        if (disabledSelectedIcon != oldValue) {
            if (disabledSelectedIcon == null || oldValue == null ||
                disabledSelectedIcon.getIconWidth() != oldValue.getIconWidth() ||
                disabledSelectedIcon.getIconHeight() != oldValue.getIconHeight()) {
                revalidate();
            }
            if (!isEnabled() && isSelected()) {
                repaint();
            }
        }
    }
    public int getVerticalAlignment() {
        return verticalAlignment;
    }
    public void setVerticalAlignment(int alignment) {
        if (alignment == verticalAlignment) return;
        int oldValue = verticalAlignment;
        verticalAlignment = checkVerticalKey(alignment, "verticalAlignment");
        firePropertyChange(VERTICAL_ALIGNMENT_CHANGED_PROPERTY, oldValue, verticalAlignment);         repaint();
    }
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }
    public void setHorizontalAlignment(int alignment) {
        if (alignment == horizontalAlignment) return;
        int oldValue = horizontalAlignment;
        horizontalAlignment = checkHorizontalKey(alignment,
                                                 "horizontalAlignment");
        firePropertyChange(HORIZONTAL_ALIGNMENT_CHANGED_PROPERTY,
                           oldValue, horizontalAlignment);
        repaint();
    }
    public int getVerticalTextPosition() {
        return verticalTextPosition;
    }
    public void setVerticalTextPosition(int textPosition) {
        if (textPosition == verticalTextPosition) return;
        int oldValue = verticalTextPosition;
        verticalTextPosition = checkVerticalKey(textPosition, "verticalTextPosition");
        firePropertyChange(VERTICAL_TEXT_POSITION_CHANGED_PROPERTY, oldValue, verticalTextPosition);
        revalidate();
        repaint();
    }
    public int getHorizontalTextPosition() {
        return horizontalTextPosition;
    }
    public void setHorizontalTextPosition(int textPosition) {
        if (textPosition == horizontalTextPosition) return;
        int oldValue = horizontalTextPosition;
        horizontalTextPosition = checkHorizontalKey(textPosition,
                                                    "horizontalTextPosition");
        firePropertyChange(HORIZONTAL_TEXT_POSITION_CHANGED_PROPERTY,
                           oldValue,
                           horizontalTextPosition);
        revalidate();
        repaint();
    }
    public int getIconTextGap() {
        return iconTextGap;
    }
    public void setIconTextGap(int iconTextGap) {
        int oldValue = this.iconTextGap;
        this.iconTextGap = iconTextGap;
        iconTextGapSet = true;
        firePropertyChange("iconTextGap", oldValue, iconTextGap);
        if (iconTextGap != oldValue) {
            revalidate();
            repaint();
        }
    }
    protected int checkHorizontalKey(int key, String exception) {
        if ((key == LEFT) ||
            (key == CENTER) ||
            (key == RIGHT) ||
            (key == LEADING) ||
            (key == TRAILING)) {
            return key;
        } else {
            throw new IllegalArgumentException(exception);
        }
    }
    protected int checkVerticalKey(int key, String exception) {
        if ((key == TOP) || (key == CENTER) || (key == BOTTOM)) {
            return key;
        } else {
            throw new IllegalArgumentException(exception);
        }
    }
    public void removeNotify() {
        super.removeNotify();
        if(isRolloverEnabled()) {
            getModel().setRollover(false);
        }
    }
    public void setActionCommand(String actionCommand) {
        getModel().setActionCommand(actionCommand);
    }
    public String getActionCommand() {
        String ac = getModel().getActionCommand();
        if(ac == null) {
            ac = getText();
        }
        return ac;
    }
    private Action action;
    private PropertyChangeListener actionPropertyChangeListener;
    public void setAction(Action a) {
        Action oldValue = getAction();
        if (action==null || !action.equals(a)) {
            action = a;
            if (oldValue!=null) {
                removeActionListener(oldValue);
                oldValue.removePropertyChangeListener(actionPropertyChangeListener);
                actionPropertyChangeListener = null;
            }
            configurePropertiesFromAction(action);
            if (action!=null) {
                if (!isListener(ActionListener.class, action)) {
                    addActionListener(action);
                }
                actionPropertyChangeListener = createActionPropertyChangeListener(action);
                action.addPropertyChangeListener(actionPropertyChangeListener);
            }
            firePropertyChange("action", oldValue, action);
        }
    }
    private boolean isListener(Class c, ActionListener a) {
        boolean isListener = false;
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==c && listeners[i+1]==a) {
                    isListener=true;
            }
        }
        return isListener;
    }
    public Action getAction() {
        return action;
    }
    protected void configurePropertiesFromAction(Action a) {
        setMnemonicFromAction(a);
        setTextFromAction(a, false);
        AbstractAction.setToolTipTextFromAction(this, a);
        setIconFromAction(a);
        setActionCommandFromAction(a);
        AbstractAction.setEnabledFromAction(this, a);
        if (AbstractAction.hasSelectedKey(a) &&
                shouldUpdateSelectedStateFromAction()) {
            setSelectedFromAction(a);
        }
        setDisplayedMnemonicIndexFromAction(a, false);
    }
    void clientPropertyChanged(Object key, Object oldValue,
                               Object newValue) {
        if (key == "hideActionText") {
            boolean current = (newValue instanceof Boolean) ?
                                (Boolean)newValue : false;
            if (getHideActionText() != current) {
                setHideActionText(current);
            }
        }
    }
    boolean shouldUpdateSelectedStateFromAction() {
        return false;
    }
    protected void actionPropertyChanged(Action action, String propertyName) {
        if (propertyName == Action.NAME) {
            setTextFromAction(action, true);
        } else if (propertyName == "enabled") {
            AbstractAction.setEnabledFromAction(this, action);
        } else if (propertyName == Action.SHORT_DESCRIPTION) {
            AbstractAction.setToolTipTextFromAction(this, action);
        } else if (propertyName == Action.SMALL_ICON) {
            smallIconChanged(action);
        } else if (propertyName == Action.MNEMONIC_KEY) {
            setMnemonicFromAction(action);
        } else if (propertyName == Action.ACTION_COMMAND_KEY) {
            setActionCommandFromAction(action);
        } else if (propertyName == Action.SELECTED_KEY &&
                   AbstractAction.hasSelectedKey(action) &&
                   shouldUpdateSelectedStateFromAction()) {
            setSelectedFromAction(action);
        } else if (propertyName == Action.DISPLAYED_MNEMONIC_INDEX_KEY) {
            setDisplayedMnemonicIndexFromAction(action, true);
        } else if (propertyName == Action.LARGE_ICON_KEY) {
            largeIconChanged(action);
        }
    }
    private void setDisplayedMnemonicIndexFromAction(
            Action a, boolean fromPropertyChange) {
        Integer iValue = (a == null) ? null :
                (Integer)a.getValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY);
        if (fromPropertyChange || iValue != null) {
            int value;
            if (iValue == null) {
                value = -1;
            } else {
                value = iValue;
                String text = getText();
                if (text == null || value >= text.length()) {
                    value = -1;
                }
            }
            setDisplayedMnemonicIndex(value);
        }
    }
    private void setMnemonicFromAction(Action a) {
        Integer n = (a == null) ? null :
                                  (Integer)a.getValue(Action.MNEMONIC_KEY);
        setMnemonic((n == null) ? '\0' : n);
    }
    private void setTextFromAction(Action a, boolean propertyChange) {
        boolean hideText = getHideActionText();
        if (!propertyChange) {
            setText((a != null && !hideText) ?
                        (String)a.getValue(Action.NAME) : null);
        }
        else if (!hideText) {
            setText((String)a.getValue(Action.NAME));
        }
    }
    void setIconFromAction(Action a) {
        Icon icon = null;
        if (a != null) {
            icon = (Icon)a.getValue(Action.LARGE_ICON_KEY);
            if (icon == null) {
                icon = (Icon)a.getValue(Action.SMALL_ICON);
            }
        }
        setIcon(icon);
    }
    void smallIconChanged(Action a) {
        if (a.getValue(Action.LARGE_ICON_KEY) == null) {
            setIconFromAction(a);
        }
    }
    void largeIconChanged(Action a) {
        setIconFromAction(a);
    }
    private void setActionCommandFromAction(Action a) {
        setActionCommand((a != null) ?
                             (String)a.getValue(Action.ACTION_COMMAND_KEY) :
                             null);
    }
    private void setSelectedFromAction(Action a) {
        boolean selected = false;
        if (a != null) {
            selected = AbstractAction.isSelected(a);
        }
        if (selected != isSelected()) {
            setSelected(selected);
            if (!selected && isSelected()) {
                if (getModel() instanceof DefaultButtonModel) {
                    ButtonGroup group = ((DefaultButtonModel)getModel()).getGroup();
                    if (group != null) {
                        group.clearSelection();
                    }
                }
            }
        }
    }
    protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
        return createActionPropertyChangeListener0(a);
    }
    PropertyChangeListener createActionPropertyChangeListener0(Action a) {
        return new ButtonActionPropertyChangeListener(this, a);
    }
    private static class ButtonActionPropertyChangeListener
                 extends ActionPropertyChangeListener<AbstractButton> {
        ButtonActionPropertyChangeListener(AbstractButton b, Action a) {
            super(b, a);
        }
        protected void actionPropertyChanged(AbstractButton button,
                                             Action action,
                                             PropertyChangeEvent e) {
            if (AbstractAction.shouldReconfigure(e)) {
                button.configurePropertiesFromAction(action);
            } else {
                button.actionPropertyChanged(action, e.getPropertyName());
            }
        }
    }
    public boolean isBorderPainted() {
        return paintBorder;
    }
    public void setBorderPainted(boolean b) {
        boolean oldValue = paintBorder;
        paintBorder = b;
        borderPaintedSet = true;
        firePropertyChange(BORDER_PAINTED_CHANGED_PROPERTY, oldValue, paintBorder);
        if (b != oldValue) {
            revalidate();
            repaint();
        }
    }
    protected void paintBorder(Graphics g) {
        if (isBorderPainted()) {
            super.paintBorder(g);
        }
    }
    public boolean isFocusPainted() {
        return paintFocus;
    }
    public void setFocusPainted(boolean b) {
        boolean oldValue = paintFocus;
        paintFocus = b;
        firePropertyChange(FOCUS_PAINTED_CHANGED_PROPERTY, oldValue, paintFocus);
        if (b != oldValue && isFocusOwner()) {
            revalidate();
            repaint();
        }
    }
    public boolean isContentAreaFilled() {
        return contentAreaFilled;
    }
    public void setContentAreaFilled(boolean b) {
        boolean oldValue = contentAreaFilled;
        contentAreaFilled = b;
        contentAreaFilledSet = true;
        firePropertyChange(CONTENT_AREA_FILLED_CHANGED_PROPERTY, oldValue, contentAreaFilled);
        if (b != oldValue) {
            repaint();
        }
    }
    public boolean isRolloverEnabled() {
        return rolloverEnabled;
    }
    public void setRolloverEnabled(boolean b) {
        boolean oldValue = rolloverEnabled;
        rolloverEnabled = b;
        rolloverEnabledSet = true;
        firePropertyChange(ROLLOVER_ENABLED_CHANGED_PROPERTY, oldValue, rolloverEnabled);
        if (b != oldValue) {
            repaint();
        }
    }
    public int getMnemonic() {
        return mnemonic;
    }
    public void setMnemonic(int mnemonic) {
        int oldValue = getMnemonic();
        model.setMnemonic(mnemonic);
        updateMnemonicProperties();
    }
    public void setMnemonic(char mnemonic) {
        int vk = (int) mnemonic;
        if(vk >= 'a' && vk <='z')
            vk -= ('a' - 'A');
        setMnemonic(vk);
    }
    public void setDisplayedMnemonicIndex(int index)
                                          throws IllegalArgumentException {
        int oldValue = mnemonicIndex;
        if (index == -1) {
            mnemonicIndex = -1;
        } else {
            String text = getText();
            int textLength = (text == null) ? 0 : text.length();
            if (index < -1 || index >= textLength) {  
                throw new IllegalArgumentException("index == " + index);
            }
        }
        mnemonicIndex = index;
        firePropertyChange("displayedMnemonicIndex", oldValue, index);
        if (index != oldValue) {
            revalidate();
            repaint();
        }
    }
    public int getDisplayedMnemonicIndex() {
        return mnemonicIndex;
    }
    private void updateDisplayedMnemonicIndex(String text, int mnemonic) {
        setDisplayedMnemonicIndex(
            SwingUtilities.findDisplayedMnemonicIndex(text, mnemonic));
    }
    private void updateMnemonicProperties() {
        int newMnemonic = model.getMnemonic();
        if (mnemonic != newMnemonic) {
            int oldValue = mnemonic;
            mnemonic = newMnemonic;
            firePropertyChange(MNEMONIC_CHANGED_PROPERTY,
                               oldValue, mnemonic);
            updateDisplayedMnemonicIndex(getText(), mnemonic);
            revalidate();
            repaint();
        }
    }
    public void setMultiClickThreshhold(long threshhold) {
        if (threshhold < 0) {
            throw new IllegalArgumentException("threshhold must be >= 0");
        }
        this.multiClickThreshhold = threshhold;
    }
    public long getMultiClickThreshhold() {
        return multiClickThreshhold;
    }
    public ButtonModel getModel() {
        return model;
    }
    public void setModel(ButtonModel newModel) {
        ButtonModel oldModel = getModel();
        if (oldModel != null) {
            oldModel.removeChangeListener(changeListener);
            oldModel.removeActionListener(actionListener);
            oldModel.removeItemListener(itemListener);
            changeListener = null;
            actionListener = null;
            itemListener = null;
        }
        model = newModel;
        if (newModel != null) {
            changeListener = createChangeListener();
            actionListener = createActionListener();
            itemListener = createItemListener();
            newModel.addChangeListener(changeListener);
            newModel.addActionListener(actionListener);
            newModel.addItemListener(itemListener);
            updateMnemonicProperties();
            super.setEnabled(newModel.isEnabled());
        } else {
            mnemonic = '\0';
        }
        updateDisplayedMnemonicIndex(getText(), mnemonic);
        firePropertyChange(MODEL_CHANGED_PROPERTY, oldModel, newModel);
        if (newModel != oldModel) {
            revalidate();
            repaint();
        }
    }
    public ButtonUI getUI() {
        return (ButtonUI) ui;
    }
    public void setUI(ButtonUI ui) {
        super.setUI(ui);
        if (disabledIcon instanceof UIResource) {
            setDisabledIcon(null);
        }
        if (disabledSelectedIcon instanceof UIResource) {
            setDisabledSelectedIcon(null);
        }
    }
    public void updateUI() {
    }
    protected void addImpl(Component comp, Object constraints, int index) {
        if (!setLayout) {
            setLayout(new OverlayLayout(this));
        }
        super.addImpl(comp, constraints, index);
    }
    public void setLayout(LayoutManager mgr) {
        setLayout = true;
        super.setLayout(mgr);
    }
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }
    public ChangeListener[] getChangeListeners() {
        return listenerList.getListeners(ChangeListener.class);
    }
    protected void fireStateChanged() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ChangeListener.class) {
                if (changeEvent == null)
                    changeEvent = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }
    public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }
    public void removeActionListener(ActionListener l) {
        if ((l != null) && (getAction() == l)) {
            setAction(null);
        } else {
            listenerList.remove(ActionListener.class, l);
        }
    }
    public ActionListener[] getActionListeners() {
        return listenerList.getListeners(ActionListener.class);
    }
    protected ChangeListener createChangeListener() {
        return getHandler();
    }
    protected class ButtonChangeListener implements ChangeListener, Serializable {
        ButtonChangeListener() {
        }
        public void stateChanged(ChangeEvent e) {
            getHandler().stateChanged(e);
        }
    }
    protected void fireActionPerformed(ActionEvent event) {
        Object[] listeners = listenerList.getListenerList();
        ActionEvent e = null;
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ActionListener.class) {
                if (e == null) {
                      String actionCommand = event.getActionCommand();
                      if(actionCommand == null) {
                         actionCommand = getActionCommand();
                      }
                      e = new ActionEvent(AbstractButton.this,
                                          ActionEvent.ACTION_PERFORMED,
                                          actionCommand,
                                          event.getWhen(),
                                          event.getModifiers());
                }
                ((ActionListener)listeners[i+1]).actionPerformed(e);
            }
        }
    }
    protected void fireItemStateChanged(ItemEvent event) {
        Object[] listeners = listenerList.getListenerList();
        ItemEvent e = null;
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ItemListener.class) {
                if (e == null) {
                    e = new ItemEvent(AbstractButton.this,
                                      ItemEvent.ITEM_STATE_CHANGED,
                                      AbstractButton.this,
                                      event.getStateChange());
                }
                ((ItemListener)listeners[i+1]).itemStateChanged(e);
            }
        }
        if (accessibleContext != null) {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    null, AccessibleState.SELECTED);
                accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
                    Integer.valueOf(0), Integer.valueOf(1));
            } else {
                accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    AccessibleState.SELECTED, null);
                accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
                    Integer.valueOf(1), Integer.valueOf(0));
            }
        }
    }
    protected ActionListener createActionListener() {
        return getHandler();
    }
    protected ItemListener createItemListener() {
        return getHandler();
    }
    public void setEnabled(boolean b) {
        if (!b && model.isRollover()) {
            model.setRollover(false);
        }
        super.setEnabled(b);
        model.setEnabled(b);
    }
    @Deprecated
    public String getLabel() {
        return getText();
    }
    @Deprecated
    public void setLabel(String label) {
        setText(label);
    }
    public void addItemListener(ItemListener l) {
        listenerList.add(ItemListener.class, l);
    }
    public void removeItemListener(ItemListener l) {
        listenerList.remove(ItemListener.class, l);
    }
    public ItemListener[] getItemListeners() {
        return listenerList.getListeners(ItemListener.class);
    }
    public Object[] getSelectedObjects() {
        if (isSelected() == false) {
            return null;
        }
        Object[] selectedObjects = new Object[1];
        selectedObjects[0] = getText();
        return selectedObjects;
    }
    protected void init(String text, Icon icon) {
        if(text != null) {
            setText(text);
        }
        if(icon != null) {
            setIcon(icon);
        }
        updateUI();
        setAlignmentX(LEFT_ALIGNMENT);
        setAlignmentY(CENTER_ALIGNMENT);
    }
    public boolean imageUpdate(Image img, int infoflags,
                               int x, int y, int w, int h) {
        Icon iconDisplayed = getIcon();
        if (iconDisplayed == null) {
            return false;
        }
        if (!model.isEnabled()) {
            if (model.isSelected()) {
                iconDisplayed = getDisabledSelectedIcon();
            } else {
                iconDisplayed = getDisabledIcon();
            }
        } else if (model.isPressed() && model.isArmed()) {
            iconDisplayed = getPressedIcon();
        } else if (isRolloverEnabled() && model.isRollover()) {
            if (model.isSelected()) {
                iconDisplayed = getRolloverSelectedIcon();
            } else {
                iconDisplayed = getRolloverIcon();
            }
        } else if (model.isSelected()) {
            iconDisplayed = getSelectedIcon();
        }
        if (!SwingUtilities.doesIconReferenceImage(iconDisplayed, img)) {
            return false;
        }
        return super.imageUpdate(img, infoflags, x, y, w, h);
    }
    void setUIProperty(String propertyName, Object value) {
        if (propertyName == "borderPainted") {
            if (!borderPaintedSet) {
                setBorderPainted(((Boolean)value).booleanValue());
                borderPaintedSet = false;
            }
        } else if (propertyName == "rolloverEnabled") {
            if (!rolloverEnabledSet) {
                setRolloverEnabled(((Boolean)value).booleanValue());
                rolloverEnabledSet = false;
            }
        } else if (propertyName == "iconTextGap") {
            if (!iconTextGapSet) {
                setIconTextGap(((Number)value).intValue());
                iconTextGapSet = false;
            }
        } else if (propertyName == "contentAreaFilled") {
            if (!contentAreaFilledSet) {
                setContentAreaFilled(((Boolean)value).booleanValue());
                contentAreaFilledSet = false;
            }
        } else {
            super.setUIProperty(propertyName, value);
        }
    }
    protected String paramString() {
        String defaultIconString = ((defaultIcon != null)
                                    && (defaultIcon != this) ?
                                    defaultIcon.toString() : "");
        String pressedIconString = ((pressedIcon != null)
                                    && (pressedIcon != this) ?
                                    pressedIcon.toString() : "");
        String disabledIconString = ((disabledIcon != null)
                                     && (disabledIcon != this) ?
                                     disabledIcon.toString() : "");
        String selectedIconString = ((selectedIcon != null)
                                     && (selectedIcon != this) ?
                                     selectedIcon.toString() : "");
        String disabledSelectedIconString = ((disabledSelectedIcon != null) &&
                                             (disabledSelectedIcon != this) ?
                                             disabledSelectedIcon.toString()
                                             : "");
        String rolloverIconString = ((rolloverIcon != null)
                                     && (rolloverIcon != this) ?
                                     rolloverIcon.toString() : "");
        String rolloverSelectedIconString = ((rolloverSelectedIcon != null) &&
                                             (rolloverSelectedIcon != this) ?
                                             rolloverSelectedIcon.toString()
                                             : "");
        String paintBorderString = (paintBorder ? "true" : "false");
        String paintFocusString = (paintFocus ? "true" : "false");
        String rolloverEnabledString = (rolloverEnabled ? "true" : "false");
        return super.paramString() +
        ",defaultIcon=" + defaultIconString +
        ",disabledIcon=" + disabledIconString +
        ",disabledSelectedIcon=" + disabledSelectedIconString +
        ",margin=" + margin +
        ",paintBorder=" + paintBorderString +
        ",paintFocus=" + paintFocusString +
        ",pressedIcon=" + pressedIconString +
        ",rolloverEnabled=" + rolloverEnabledString +
        ",rolloverIcon=" + rolloverIconString +
        ",rolloverSelectedIcon=" + rolloverSelectedIconString +
        ",selectedIcon=" + selectedIconString +
        ",text=" + text;
    }
    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }
    class Handler implements ActionListener, ChangeListener, ItemListener,
                             Serializable {
        public void stateChanged(ChangeEvent e) {
            Object source = e.getSource();
            updateMnemonicProperties();
            if (isEnabled() != model.isEnabled()) {
                setEnabled(model.isEnabled());
            }
            fireStateChanged();
            repaint();
        }
        public void actionPerformed(ActionEvent event) {
            fireActionPerformed(event);
        }
        public void itemStateChanged(ItemEvent event) {
            fireItemStateChanged(event);
            if (shouldUpdateSelectedStateFromAction()) {
                Action action = getAction();
                if (action != null && AbstractAction.hasSelectedKey(action)) {
                    boolean selected = isSelected();
                    boolean isActionSelected = AbstractAction.isSelected(
                              action);
                    if (isActionSelected != selected) {
                        action.putValue(Action.SELECTED_KEY, selected);
                    }
                }
            }
        }
    }
    protected abstract class AccessibleAbstractButton
        extends AccessibleJComponent implements AccessibleAction,
        AccessibleValue, AccessibleText, AccessibleExtendedComponent {
        public String getAccessibleName() {
            String name = accessibleName;
            if (name == null) {
                name = (String)getClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY);
            }
            if (name == null) {
                name = AbstractButton.this.getText();
            }
            if (name == null) {
                name = super.getAccessibleName();
            }
            return name;
        }
        public AccessibleIcon [] getAccessibleIcon() {
            Icon defaultIcon = getIcon();
            if (defaultIcon instanceof Accessible) {
                AccessibleContext ac =
                    ((Accessible)defaultIcon).getAccessibleContext();
                if (ac != null && ac instanceof AccessibleIcon) {
                    return new AccessibleIcon[] { (AccessibleIcon)ac };
                }
            }
            return null;
        }
        public AccessibleStateSet getAccessibleStateSet() {
        AccessibleStateSet states = super.getAccessibleStateSet();
            if (getModel().isArmed()) {
                states.add(AccessibleState.ARMED);
            }
            if (isFocusOwner()) {
                states.add(AccessibleState.FOCUSED);
            }
            if (getModel().isPressed()) {
                states.add(AccessibleState.PRESSED);
            }
            if (isSelected()) {
                states.add(AccessibleState.CHECKED);
            }
            return states;
        }
        public AccessibleRelationSet getAccessibleRelationSet() {
            AccessibleRelationSet relationSet
                = super.getAccessibleRelationSet();
            if (!relationSet.contains(AccessibleRelation.MEMBER_OF)) {
                ButtonModel model = getModel();
                if (model != null && model instanceof DefaultButtonModel) {
                    ButtonGroup group = ((DefaultButtonModel)model).getGroup();
                    if (group != null) {
                        int len = group.getButtonCount();
                        Object [] target = new Object[len];
                        Enumeration elem = group.getElements();
                        for (int i = 0; i < len; i++) {
                            if (elem.hasMoreElements()) {
                                target[i] = elem.nextElement();
                            }
                        }
                        AccessibleRelation relation =
                            new AccessibleRelation(AccessibleRelation.MEMBER_OF);
                        relation.setTarget(target);
                        relationSet.add(relation);
                    }
                }
            }
            return relationSet;
        }
        public AccessibleAction getAccessibleAction() {
            return this;
        }
        public AccessibleValue getAccessibleValue() {
            return this;
        }
        public int getAccessibleActionCount() {
            return 1;
        }
        public String getAccessibleActionDescription(int i) {
            if (i == 0) {
                return UIManager.getString("AbstractButton.clickText");
            } else {
                return null;
            }
        }
        public boolean doAccessibleAction(int i) {
            if (i == 0) {
                doClick();
                return true;
            } else {
                return false;
            }
        }
        public Number getCurrentAccessibleValue() {
            if (isSelected()) {
                return Integer.valueOf(1);
            } else {
                return Integer.valueOf(0);
            }
        }
        public boolean setCurrentAccessibleValue(Number n) {
            if (n == null) {
                return false;
            }
            int i = n.intValue();
            if (i == 0) {
                setSelected(false);
            } else {
                setSelected(true);
            }
            return true;
        }
        public Number getMinimumAccessibleValue() {
            return Integer.valueOf(0);
        }
        public Number getMaximumAccessibleValue() {
            return Integer.valueOf(1);
        }
        public AccessibleText getAccessibleText() {
            View view = (View)AbstractButton.this.getClientProperty("html");
            if (view != null) {
                return this;
            } else {
                return null;
            }
        }
        public int getIndexAtPoint(Point p) {
            View view = (View) AbstractButton.this.getClientProperty("html");
            if (view != null) {
                Rectangle r = getTextRectangle();
                if (r == null) {
                    return -1;
                }
                Rectangle2D.Float shape =
                    new Rectangle2D.Float(r.x, r.y, r.width, r.height);
                Position.Bias bias[] = new Position.Bias[1];
                return view.viewToModel(p.x, p.y, shape, bias);
            } else {
                return -1;
            }
        }
        public Rectangle getCharacterBounds(int i) {
            View view = (View) AbstractButton.this.getClientProperty("html");
            if (view != null) {
                Rectangle r = getTextRectangle();
                if (r == null) {
                    return null;
                }
                Rectangle2D.Float shape =
                    new Rectangle2D.Float(r.x, r.y, r.width, r.height);
                try {
                    Shape charShape =
                        view.modelToView(i, shape, Position.Bias.Forward);
                    return charShape.getBounds();
                } catch (BadLocationException e) {
                    return null;
                }
            } else {
                return null;
            }
        }
        public int getCharCount() {
            View view = (View) AbstractButton.this.getClientProperty("html");
            if (view != null) {
                Document d = view.getDocument();
                if (d instanceof StyledDocument) {
                    StyledDocument doc = (StyledDocument)d;
                    return doc.getLength();
                }
            }
            return accessibleContext.getAccessibleName().length();
        }
        public int getCaretPosition() {
            return -1;
        }
        public String getAtIndex(int part, int index) {
            if (index < 0 || index >= getCharCount()) {
                return null;
            }
            switch (part) {
            case AccessibleText.CHARACTER:
                try {
                    return getText(index, 1);
                } catch (BadLocationException e) {
                    return null;
                }
            case AccessibleText.WORD:
                try {
                    String s = getText(0, getCharCount());
                    BreakIterator words = BreakIterator.getWordInstance(getLocale());
                    words.setText(s);
                    int end = words.following(index);
                    return s.substring(words.previous(), end);
                } catch (BadLocationException e) {
                    return null;
                }
            case AccessibleText.SENTENCE:
                try {
                    String s = getText(0, getCharCount());
                    BreakIterator sentence =
                        BreakIterator.getSentenceInstance(getLocale());
                    sentence.setText(s);
                    int end = sentence.following(index);
                    return s.substring(sentence.previous(), end);
                } catch (BadLocationException e) {
                    return null;
                }
            default:
                return null;
            }
        }
        public String getAfterIndex(int part, int index) {
            if (index < 0 || index >= getCharCount()) {
                return null;
            }
            switch (part) {
            case AccessibleText.CHARACTER:
                if (index+1 >= getCharCount()) {
                   return null;
                }
                try {
                    return getText(index+1, 1);
                } catch (BadLocationException e) {
                    return null;
                }
            case AccessibleText.WORD:
                try {
                    String s = getText(0, getCharCount());
                    BreakIterator words = BreakIterator.getWordInstance(getLocale());
                    words.setText(s);
                    int start = words.following(index);
                    if (start == BreakIterator.DONE || start >= s.length()) {
                        return null;
                    }
                    int end = words.following(start);
                    if (end == BreakIterator.DONE || end >= s.length()) {
                        return null;
                    }
                    return s.substring(start, end);
                } catch (BadLocationException e) {
                    return null;
                }
            case AccessibleText.SENTENCE:
                try {
                    String s = getText(0, getCharCount());
                    BreakIterator sentence =
                        BreakIterator.getSentenceInstance(getLocale());
                    sentence.setText(s);
                    int start = sentence.following(index);
                    if (start == BreakIterator.DONE || start > s.length()) {
                        return null;
                    }
                    int end = sentence.following(start);
                    if (end == BreakIterator.DONE || end > s.length()) {
                        return null;
                    }
                    return s.substring(start, end);
                } catch (BadLocationException e) {
                    return null;
                }
            default:
                return null;
            }
        }
        public String getBeforeIndex(int part, int index) {
            if (index < 0 || index > getCharCount()-1) {
                return null;
            }
            switch (part) {
            case AccessibleText.CHARACTER:
                if (index == 0) {
                    return null;
                }
                try {
                    return getText(index-1, 1);
                } catch (BadLocationException e) {
                    return null;
                }
            case AccessibleText.WORD:
                try {
                    String s = getText(0, getCharCount());
                    BreakIterator words = BreakIterator.getWordInstance(getLocale());
                    words.setText(s);
                    int end = words.following(index);
                    end = words.previous();
                    int start = words.previous();
                    if (start == BreakIterator.DONE) {
                        return null;
                    }
                    return s.substring(start, end);
                } catch (BadLocationException e) {
                    return null;
                }
            case AccessibleText.SENTENCE:
                try {
                    String s = getText(0, getCharCount());
                    BreakIterator sentence =
                        BreakIterator.getSentenceInstance(getLocale());
                    sentence.setText(s);
                    int end = sentence.following(index);
                    end = sentence.previous();
                    int start = sentence.previous();
                    if (start == BreakIterator.DONE) {
                        return null;
                    }
                    return s.substring(start, end);
                } catch (BadLocationException e) {
                    return null;
                }
            default:
                return null;
            }
        }
        public AttributeSet getCharacterAttribute(int i) {
            View view = (View) AbstractButton.this.getClientProperty("html");
            if (view != null) {
                Document d = view.getDocument();
                if (d instanceof StyledDocument) {
                    StyledDocument doc = (StyledDocument)d;
                    Element elem = doc.getCharacterElement(i);
                    if (elem != null) {
                        return elem.getAttributes();
                    }
                }
            }
            return null;
        }
        public int getSelectionStart() {
            return -1;
        }
        public int getSelectionEnd() {
            return -1;
        }
        public String getSelectedText() {
            return null;
        }
        private String getText(int offset, int length)
            throws BadLocationException {
            View view = (View) AbstractButton.this.getClientProperty("html");
            if (view != null) {
                Document d = view.getDocument();
                if (d instanceof StyledDocument) {
                    StyledDocument doc = (StyledDocument)d;
                    return doc.getText(offset, length);
                }
            }
            return null;
        }
        private Rectangle getTextRectangle() {
            String text = AbstractButton.this.getText();
            Icon icon = (AbstractButton.this.isEnabled()) ? AbstractButton.this.getIcon() : AbstractButton.this.getDisabledIcon();
            if ((icon == null) && (text == null)) {
                return null;
            }
            Rectangle paintIconR = new Rectangle();
            Rectangle paintTextR = new Rectangle();
            Rectangle paintViewR = new Rectangle();
            Insets paintViewInsets = new Insets(0, 0, 0, 0);
            paintViewInsets = AbstractButton.this.getInsets(paintViewInsets);
            paintViewR.x = paintViewInsets.left;
            paintViewR.y = paintViewInsets.top;
            paintViewR.width = AbstractButton.this.getWidth() - (paintViewInsets.left + paintViewInsets.right);
            paintViewR.height = AbstractButton.this.getHeight() - (paintViewInsets.top + paintViewInsets.bottom);
            String clippedText = SwingUtilities.layoutCompoundLabel(
                AbstractButton.this,
                getFontMetrics(getFont()),
                text,
                icon,
                AbstractButton.this.getVerticalAlignment(),
                AbstractButton.this.getHorizontalAlignment(),
                AbstractButton.this.getVerticalTextPosition(),
                AbstractButton.this.getHorizontalTextPosition(),
                paintViewR,
                paintIconR,
                paintTextR,
                0);
            return paintTextR;
        }
        AccessibleExtendedComponent getAccessibleExtendedComponent() {
            return this;
        }
        public String getToolTipText() {
            return AbstractButton.this.getToolTipText();
        }
        public String getTitledBorderText() {
            return super.getTitledBorderText();
        }
        public AccessibleKeyBinding getAccessibleKeyBinding() {
            int mnemonic = AbstractButton.this.getMnemonic();
            if (mnemonic == 0) {
                return null;
            }
            return new ButtonKeyBinding(mnemonic);
        }
        class ButtonKeyBinding implements AccessibleKeyBinding {
            int mnemonic;
            ButtonKeyBinding(int mnemonic) {
                this.mnemonic = mnemonic;
            }
            public int getAccessibleKeyBindingCount() {
                return 1;
            }
            public java.lang.Object getAccessibleKeyBinding(int i) {
                if (i != 0) {
                    throw new IllegalArgumentException();
                }
                return KeyStroke.getKeyStroke(mnemonic, 0);
            }
        }
    }
}

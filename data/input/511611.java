public class TabbedPane
     extends JPanel
{
    private final CardLayout  cardLayout  = new CardLayout();
    private final JPanel      cardPanel   = new JPanel(cardLayout);
    private final ButtonGroup buttonGroup = new ButtonGroup();
    public TabbedPane()
    {
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        GridBagConstraints cardConstraints = new GridBagConstraints();
        cardConstraints.gridx      = 1;
        cardConstraints.gridy      = 0;
        cardConstraints.gridheight = GridBagConstraints.REMAINDER;
        cardConstraints.fill       = GridBagConstraints.BOTH;
        cardConstraints.weightx    = 1.0;
        cardConstraints.weighty    = 1.0;
        cardConstraints.anchor     = GridBagConstraints.NORTHWEST;
        add(cardPanel, cardConstraints);
    }
    public Component add(final String title, Component component)
    {
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx  = 0;
        buttonConstraints.fill   = GridBagConstraints.HORIZONTAL;
        buttonConstraints.anchor = GridBagConstraints.NORTHWEST;
        buttonConstraints.ipadx  = 10;
        buttonConstraints.ipady  = 4;
        JToggleButton button = new JToggleButton(title);
        button.setModel(new JToggleButton.ToggleButtonModel()
        {
            public void setPressed(boolean b)
            {
                if ((isPressed() == b) || !isEnabled())
                {
                    return;
                }
                if (!b && isArmed())
                {
                    setSelected(!this.isSelected());
                }
                if (b)
                {
                    stateMask |= PRESSED;
                }
                else
                {
                    stateMask &= ~PRESSED;
                }
                fireStateChanged();
                if (isPressed())
                {
                    fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand()));
                }
            }
        });
        button.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                cardLayout.show(cardPanel, title);
            }
        });
        buttonGroup.add(button);
        if (cardPanel.getComponentCount() == 0)
        {
            button.setSelected(true);
        }
        add(button, buttonConstraints);
        cardPanel.add(title, component);
        return component;
    }
    public Component addImage(final Image image)
    {
        GridBagConstraints imageConstraints = new GridBagConstraints();
        imageConstraints.gridx   = 0;
        imageConstraints.weighty = 1.0;
        imageConstraints.fill    = GridBagConstraints.BOTH;
        imageConstraints.anchor  = GridBagConstraints.SOUTHWEST;
        JButton component = new JButton(new ImageIcon(image));
        component.setFocusPainted(false);
        component.setFocusable(false);
        component.setRequestFocusEnabled(false);
        component.setRolloverEnabled(false);
        component.setMargin(new Insets(0, 0, 0, 0));
        component.setHorizontalAlignment(JButton.LEFT);
        component.setVerticalAlignment(JButton.BOTTOM);
        component.setPreferredSize(new Dimension(0, 0));
        add(component, imageConstraints);
        return component;
    }
    public void first()
    {
        cardLayout.first(cardPanel);
        updateButtonSelection();
    }
    public void last()
    {
        cardLayout.last(cardPanel);
        updateButtonSelection();
    }
    public void previous()
    {
        cardLayout.previous(cardPanel);
        updateButtonSelection();
    }
    public void next()
    {
        cardLayout.next(cardPanel);
        updateButtonSelection();
    }
    private void updateButtonSelection()
    {
        int count = cardPanel.getComponentCount();
        for (int index = 0 ; index < count ; index++) {
            Component card = cardPanel.getComponent(index);
            if (card.isShowing())
            {
                JToggleButton button = (JToggleButton)getComponent(index+1);
                button.setSelected(true);
            }
        }
    }
}

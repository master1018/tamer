final class OptimizationsDialog extends JDialog
{
    public static final int CANCEL_OPTION = 1;
    public static final int APPROVE_OPTION = 0;
    private final JCheckBox[] optimizationCheckBoxes = new JCheckBox[Optimizer.OPTIMIZATION_NAMES.length];
    private int returnValue;
    public OptimizationsDialog(JFrame owner)
    {
        super(owner, msg("selectOptimizations"), true);
        setResizable(true);
        GridBagConstraints constraintsLast = new GridBagConstraints();
        constraintsLast.gridwidth = GridBagConstraints.REMAINDER;
        constraintsLast.anchor    = GridBagConstraints.WEST;
        constraintsLast.insets    = new Insets(1, 2, 1, 2);
        GridBagConstraints constraintsLastStretch = new GridBagConstraints();
        constraintsLastStretch.gridwidth = GridBagConstraints.REMAINDER;
        constraintsLastStretch.fill      = GridBagConstraints.HORIZONTAL;
        constraintsLastStretch.weightx   = 1.0;
        constraintsLastStretch.anchor    = GridBagConstraints.WEST;
        constraintsLastStretch.insets    = constraintsLast.insets;
        GridBagConstraints panelConstraints = new GridBagConstraints();
        panelConstraints.gridwidth = GridBagConstraints.REMAINDER;
        panelConstraints.fill      = GridBagConstraints.HORIZONTAL;
        panelConstraints.weightx   = 1.0;
        panelConstraints.weighty   = 0.0;
        panelConstraints.anchor    = GridBagConstraints.NORTHWEST;
        panelConstraints.insets    = constraintsLast.insets;
        GridBagConstraints selectButtonConstraints = new GridBagConstraints();
        selectButtonConstraints.weighty = 1.0;
        selectButtonConstraints.anchor  = GridBagConstraints.SOUTHWEST;
        selectButtonConstraints.insets  = new Insets(4, 4, 8, 4);
        GridBagConstraints okButtonConstraints = new GridBagConstraints();
        okButtonConstraints.weightx = 1.0;
        okButtonConstraints.weighty = 1.0;
        okButtonConstraints.anchor  = GridBagConstraints.SOUTHEAST;
        okButtonConstraints.insets  = selectButtonConstraints.insets;
        GridBagConstraints cancelButtonConstraints = new GridBagConstraints();
        cancelButtonConstraints.gridwidth = GridBagConstraints.REMAINDER;
        cancelButtonConstraints.weighty   = 1.0;
        cancelButtonConstraints.anchor    = GridBagConstraints.SOUTHEAST;
        cancelButtonConstraints.insets    = selectButtonConstraints.insets;
        GridBagLayout layout = new GridBagLayout();
        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        JPanel optimizationsPanel     = new JPanel(layout);
        JPanel optimizationSubpanel   = null;
        String lastOptimizationPrefix = null;
        for (int index = 0; index < Optimizer.OPTIMIZATION_NAMES.length; index++)
        {
            String optimizationName = Optimizer.OPTIMIZATION_NAMES[index];
            String optimizationPrefix = optimizationName.substring(0, optimizationName.indexOf('/'));
            if (optimizationSubpanel == null || !optimizationPrefix.equals(lastOptimizationPrefix))
            {
                optimizationSubpanel = new JPanel(layout);
                optimizationSubpanel.setBorder(BorderFactory.createTitledBorder(etchedBorder, msg(optimizationPrefix)));
                optimizationsPanel.add(optimizationSubpanel, panelConstraints);
                lastOptimizationPrefix = optimizationPrefix;
            }
            JCheckBox optimizationCheckBox = new JCheckBox(optimizationName);
            optimizationCheckBoxes[index] = optimizationCheckBox;
            optimizationSubpanel.add(tip(optimizationCheckBox, optimizationName.replace('/', '_')+"Tip"), constraintsLastStretch);
        }
        JButton selectAllButton = new JButton(msg("selectAll"));
        selectAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                for (int index = 0; index < optimizationCheckBoxes.length; index++)
                {
                    optimizationCheckBoxes[index].setSelected(true);
                }
            }
        });
        JButton selectNoneButton = new JButton(msg("selectNone"));
        selectNoneButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                for (int index = 0; index < optimizationCheckBoxes.length; index++)
                {
                    optimizationCheckBoxes[index].setSelected(false);
                }
            }
        });
        JButton okButton = new JButton(msg("ok"));
        okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                returnValue = APPROVE_OPTION;
                hide();
            }
        });
        JButton cancelButton = new JButton(msg("cancel"));
        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                hide();
            }
        });
        optimizationsPanel.add(selectAllButton,  selectButtonConstraints);
        optimizationsPanel.add(selectNoneButton, selectButtonConstraints);
        optimizationsPanel.add(okButton,         okButtonConstraints);
        optimizationsPanel.add(cancelButton,     cancelButtonConstraints);
        getContentPane().add(new JScrollPane(optimizationsPanel));
    }
    public void setFilter(String optimizations)
    {
        StringMatcher filter = optimizations != null && optimizations.length() > 0 ?
            new ListParser(new NameParser()).parse(optimizations) :
            new FixedStringMatcher("");
        for (int index = 0; index < Optimizer.OPTIMIZATION_NAMES.length; index++)
        {
            optimizationCheckBoxes[index].setSelected(filter.matches(Optimizer.OPTIMIZATION_NAMES[index]));
        }
    }
    public String getFilter()
    {
        return new FilterBuilder(optimizationCheckBoxes, '/').buildFilter();
    }
    public int showDialog()
    {
        returnValue = CANCEL_OPTION;
        pack();
        setLocationRelativeTo(getOwner());
        show();
        return returnValue;
    }
    private static JComponent tip(JComponent component, String messageKey)
    {
        component.setToolTipText(msg(messageKey));
        return component;
    }
    private static String msg(String messageKey)
    {
         return GUIResources.getMessage(messageKey);
    }
}
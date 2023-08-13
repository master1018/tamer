public class FilterDialog extends JDialog
{
    public static final int CANCEL_OPTION = 1;
    public static final int APPROVE_OPTION = 0;
    private static final String DEFAULT_FILTER     = "**";
    private static final String DEFAULT_JAR_FILTER = "**.jar";
    private static final String DEFAULT_WAR_FILTER = "**.war";
    private static final String DEFAULT_EAR_FILTER = "**.ear";
    private static final String DEFAULT_ZIP_FILTER = "**.zip";
    private final JTextField filterTextField    = new JTextField(40);
    private final JTextField jarFilterTextField = new JTextField(40);
    private final JTextField warFilterTextField = new JTextField(40);
    private final JTextField earFilterTextField = new JTextField(40);
    private final JTextField zipFilterTextField = new JTextField(40);
    private int        returnValue;
    public FilterDialog(JFrame owner,
                        String explanation)
    {
        super(owner, true);
        setResizable(true);
        GridBagConstraints textConstraints = new GridBagConstraints();
        textConstraints.gridwidth = GridBagConstraints.REMAINDER;
        textConstraints.fill      = GridBagConstraints.HORIZONTAL;
        textConstraints.weightx   = 1.0;
        textConstraints.weighty   = 1.0;
        textConstraints.anchor    = GridBagConstraints.NORTHWEST;
        textConstraints.insets    = new Insets(10, 10, 10, 10);
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(1, 2, 1, 2);
        GridBagConstraints textFieldConstraints = new GridBagConstraints();
        textFieldConstraints.gridwidth = GridBagConstraints.REMAINDER;
        textFieldConstraints.fill      = GridBagConstraints.HORIZONTAL;
        textFieldConstraints.weightx   = 1.0;
        textFieldConstraints.anchor    = GridBagConstraints.WEST;
        textFieldConstraints.insets    = labelConstraints.insets;
        GridBagConstraints panelConstraints = new GridBagConstraints();
        panelConstraints.gridwidth = GridBagConstraints.REMAINDER;
        panelConstraints.fill      = GridBagConstraints.HORIZONTAL;
        panelConstraints.weightx   = 1.0;
        panelConstraints.weighty   = 0.0;
        panelConstraints.anchor    = GridBagConstraints.NORTHWEST;
        panelConstraints.insets    = labelConstraints.insets;
        GridBagConstraints okButtonConstraints = new GridBagConstraints();
        okButtonConstraints.weightx = 1.0;
        okButtonConstraints.weighty = 1.0;
        okButtonConstraints.anchor  = GridBagConstraints.SOUTHEAST;
        okButtonConstraints.insets  = new Insets(4, 4, 8, 4);
        GridBagConstraints cancelButtonConstraints = new GridBagConstraints();
        cancelButtonConstraints.gridwidth = GridBagConstraints.REMAINDER;
        cancelButtonConstraints.weighty   = 1.0;
        cancelButtonConstraints.anchor    = GridBagConstraints.SOUTHEAST;
        cancelButtonConstraints.insets    = okButtonConstraints.insets;
        GridBagLayout layout = new GridBagLayout();
        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        JTextArea explanationTextArea = new JTextArea(explanation, 3, 0);
        explanationTextArea.setOpaque(false);
        explanationTextArea.setEditable(false);
        explanationTextArea.setLineWrap(true);
        explanationTextArea.setWrapStyleWord(true);
        JLabel filterLabel    = new JLabel(msg("nameFilter"));
        JLabel jarFilterLabel = new JLabel(msg("jarNameFilter"));
        JLabel warFilterLabel = new JLabel(msg("warNameFilter"));
        JLabel earFilterLabel = new JLabel(msg("earNameFilter"));
        JLabel zipFilterLabel = new JLabel(msg("zipNameFilter"));
        JPanel filterPanel = new JPanel(layout);
        filterPanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                               msg("filters")));
        filterPanel.add(explanationTextArea, textConstraints);
        filterPanel.add(tip(filterLabel,        "nameFilterTip"),     labelConstraints);
        filterPanel.add(tip(filterTextField,    "fileNameFilterTip"), textFieldConstraints);
        filterPanel.add(tip(jarFilterLabel,     "jarNameFilterTip"),  labelConstraints);
        filterPanel.add(tip(jarFilterTextField, "fileNameFilterTip"), textFieldConstraints);
        filterPanel.add(tip(warFilterLabel,     "warNameFilterTip"),  labelConstraints);
        filterPanel.add(tip(warFilterTextField, "fileNameFilterTip"), textFieldConstraints);
        filterPanel.add(tip(earFilterLabel,     "earNameFilterTip"),  labelConstraints);
        filterPanel.add(tip(earFilterTextField, "fileNameFilterTip"), textFieldConstraints);
        filterPanel.add(tip(zipFilterLabel,     "zipNameFilterTip"),  labelConstraints);
        filterPanel.add(tip(zipFilterTextField, "fileNameFilterTip"), textFieldConstraints);
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
        JPanel mainPanel = new JPanel(layout);
        mainPanel.add(filterPanel,  panelConstraints);
        mainPanel.add(okButton,     okButtonConstraints);
        mainPanel.add(cancelButton, cancelButtonConstraints);
        getContentPane().add(mainPanel);
    }
    public void setFilter(List filter)
    {
        filterTextField.setText(filter != null ? ListUtil.commaSeparatedString(filter) : DEFAULT_FILTER);
    }
    public List getFilter()
    {
        String filter = filterTextField.getText();
        return filter.equals(DEFAULT_FILTER) ? null : ListUtil.commaSeparatedList(filter);
    }
    public void setJarFilter(List filter)
    {
        jarFilterTextField.setText(filter != null ? ListUtil.commaSeparatedString(filter) : DEFAULT_JAR_FILTER);
    }
    public List getJarFilter()
    {
        String filter = jarFilterTextField.getText();
        return filter.equals(DEFAULT_JAR_FILTER) ? null : ListUtil.commaSeparatedList(filter);
    }
    public void setWarFilter(List filter)
    {
        warFilterTextField.setText(filter != null ? ListUtil.commaSeparatedString(filter) : DEFAULT_WAR_FILTER);
    }
    public List getWarFilter()
    {
        String filter = warFilterTextField.getText();
        return filter.equals(DEFAULT_WAR_FILTER) ? null : ListUtil.commaSeparatedList(filter);
    }
    public void setEarFilter(List filter)
    {
        earFilterTextField.setText(filter != null ? ListUtil.commaSeparatedString(filter) : DEFAULT_EAR_FILTER);
    }
    public List getEarFilter()
    {
        String filter = earFilterTextField.getText();
        return filter.equals(DEFAULT_EAR_FILTER) ? null : ListUtil.commaSeparatedList(filter);
    }
    public void setZipFilter(List filter)
    {
        zipFilterTextField.setText(filter != null ? ListUtil.commaSeparatedString(filter) : DEFAULT_ZIP_FILTER);
    }
    public List getZipFilter()
    {
        String filter = zipFilterTextField.getText();
        return filter.equals(DEFAULT_ZIP_FILTER) ? null : ListUtil.commaSeparatedList(filter);
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

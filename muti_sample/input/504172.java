class ClassPathPanel extends ListPanel
{
    private final JFrame       owner;
    private final boolean      inputAndOutput;
    private final JFileChooser chooser;
    private final FilterDialog filterDialog;
    public ClassPathPanel(JFrame owner, boolean inputAndOutput)
    {
        super();
        super.firstSelectionButton = inputAndOutput ? 3 : 2;
        this.owner          = owner;
        this.inputAndOutput = inputAndOutput;
        list.setCellRenderer(new MyListCellRenderer());
        chooser = new JFileChooser("");
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.addChoosableFileFilter(
            new ExtensionFileFilter(msg("jarWarEarZipExtensions"),
                                    new String[] { ".jar", ".war", ".ear", ".zip" }));
        chooser.setApproveButtonText(msg("ok"));
        filterDialog = new FilterDialog(owner, msg("enterFilter"));
        addAddButton(inputAndOutput, false);
        if (inputAndOutput)
        {
            addAddButton(inputAndOutput, true);
        }
        addEditButton();
        addFilterButton();
        addRemoveButton();
        addUpButton();
        addDownButton();
        enableSelectionButtons();
    }
    protected void addAddButton(boolean       inputAndOutput,
                                final boolean isOutput)
    {
        JButton addButton = new JButton(msg(inputAndOutput ?
                                            isOutput       ? "addOutput" :
                                                             "addInput" :
                                                             "add"));
        addButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                chooser.setDialogTitle(msg("addJars"));
                chooser.setSelectedFile(null);
                chooser.setSelectedFiles(null);
                int returnValue = chooser.showOpenDialog(owner);
                if (returnValue == JFileChooser.APPROVE_OPTION)
                {
                    File[] selectedFiles = chooser.getSelectedFiles();
                    ClassPathEntry[] entries = classPathEntries(selectedFiles, isOutput);
                    addElements(entries);
                }
            }
        });
        addButton(tip(addButton, inputAndOutput ?
                                 isOutput       ? "addOutputTip" :
                                                  "addInputTip" :
                                                  "addTip"));
    }
    protected void addEditButton()
    {
        JButton editButton = new JButton(msg("edit"));
        editButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                boolean isOutput = false;
                int[] selectedIndices = list.getSelectedIndices();
                File[] selectedFiles = new File[selectedIndices.length];
                for (int index = 0; index < selectedFiles.length; index++)
                {
                    ClassPathEntry entry =
                        (ClassPathEntry)listModel.getElementAt(selectedIndices[index]);
                    isOutput = entry.isOutput();
                    selectedFiles[index] = entry.getFile();
                }
                chooser.setDialogTitle(msg("chooseJars"));
                chooser.setSelectedFile(selectedFiles[0]);
                chooser.setSelectedFiles(selectedFiles);
                int returnValue = chooser.showOpenDialog(owner);
                if (returnValue == JFileChooser.APPROVE_OPTION)
                {
                    selectedFiles = chooser.getSelectedFiles();
                    ClassPathEntry[] entries = classPathEntries(selectedFiles, isOutput);
                    if (selectedIndices.length == selectedFiles.length)
                    {
                        setElementsAt(entries, selectedIndices);
                    }
                    else
                    {
                        removeElementsAt(selectedIndices);
                        addElements(entries);
                    }
                }
            }
        });
        addButton(tip(editButton, "editTip"));
    }
    protected void addFilterButton()
    {
        JButton filterButton = new JButton(msg("filter"));
        filterButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (!list.isSelectionEmpty())
                {
                    int[] selectedIndices = list.getSelectedIndices();
                    getFiltersFrom(selectedIndices[0]);
                    int returnValue = filterDialog.showDialog();
                    if (returnValue == FilterDialog.APPROVE_OPTION)
                    {
                        setFiltersAt(selectedIndices);
                    }
                }
            }
        });
        addButton(tip(filterButton, "filterTip"));
    }
    public void setClassPath(ClassPath classPath)
    {
        listModel.clear();
        if (classPath != null)
        {
            for (int index = 0; index < classPath.size(); index++)
            {
                listModel.addElement(classPath.get(index));
            }
        }
        enableSelectionButtons();
    }
    public ClassPath getClassPath()
    {
        int size = listModel.size();
        if (size == 0)
        {
            return null;
        }
        ClassPath classPath = new ClassPath();
        for (int index = 0; index < size; index++)
        {
            classPath.add((ClassPathEntry)listModel.get(index));
        }
        return classPath;
    }
    private ClassPathEntry[] classPathEntries(File[] files, boolean isOutput)
    {
        ClassPathEntry[] entries = new ClassPathEntry[files.length];
        for (int index = 0; index < entries.length; index++)
        {
            entries[index] = new ClassPathEntry(files[index], isOutput);
        }
        return entries;
    }
    private void getFiltersFrom(int index)
    {
        ClassPathEntry firstEntry = (ClassPathEntry)listModel.get(index);
        filterDialog.setFilter(firstEntry.getFilter());
        filterDialog.setJarFilter(firstEntry.getJarFilter());
        filterDialog.setWarFilter(firstEntry.getWarFilter());
        filterDialog.setEarFilter(firstEntry.getEarFilter());
        filterDialog.setZipFilter(firstEntry.getZipFilter());
    }
    private void setFiltersAt(int[] indices)
    {
        for (int index = indices.length - 1; index >= 0; index--)
        {
            ClassPathEntry entry = (ClassPathEntry)listModel.get(indices[index]);
            entry.setFilter(filterDialog.getFilter());
            entry.setJarFilter(filterDialog.getJarFilter());
            entry.setWarFilter(filterDialog.getWarFilter());
            entry.setEarFilter(filterDialog.getEarFilter());
            entry.setZipFilter(filterDialog.getZipFilter());
        }
        list.setSelectedIndices(indices);
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
    private class MyListCellRenderer implements ListCellRenderer
    {
        private static final String ARROW_IMAGE_FILE = "arrow.gif";
        private final JPanel cellPanel    = new JPanel(new GridBagLayout());
        private final JLabel iconLabel    = new JLabel("", JLabel.RIGHT);
        private final JLabel jarNameLabel = new JLabel("", JLabel.RIGHT);
        private final JLabel filterLabel  = new JLabel("", JLabel.RIGHT);
        private final Icon arrowIcon;
        public MyListCellRenderer()
        {
            GridBagConstraints jarNameLabelConstraints = new GridBagConstraints();
            jarNameLabelConstraints.anchor             = GridBagConstraints.WEST;
            jarNameLabelConstraints.insets             = new Insets(1, 2, 1, 2);
            GridBagConstraints filterLabelConstraints  = new GridBagConstraints();
            filterLabelConstraints.gridwidth           = GridBagConstraints.REMAINDER;
            filterLabelConstraints.fill                = GridBagConstraints.HORIZONTAL;
            filterLabelConstraints.weightx             = 1.0;
            filterLabelConstraints.anchor              = GridBagConstraints.EAST;
            filterLabelConstraints.insets              = jarNameLabelConstraints.insets;
            arrowIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource(ARROW_IMAGE_FILE)));
            cellPanel.add(iconLabel,    jarNameLabelConstraints);
            cellPanel.add(jarNameLabel, jarNameLabelConstraints);
            cellPanel.add(filterLabel,  filterLabelConstraints);
        }
        public Component getListCellRendererComponent(JList   list,
                                                      Object  value,
                                                      int     index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            ClassPathEntry entry = (ClassPathEntry)value;
            if (inputAndOutput && entry.isOutput())
            {
                iconLabel.setIcon(arrowIcon);
            }
            else
            {
                iconLabel.setIcon(null);
            }
            jarNameLabel.setText(entry.getName());
            StringBuffer filter = null;
            filter = appendFilter(filter, entry.getZipFilter());
            filter = appendFilter(filter, entry.getEarFilter());
            filter = appendFilter(filter, entry.getWarFilter());
            filter = appendFilter(filter, entry.getJarFilter());
            filter = appendFilter(filter, entry.getFilter());
            if (filter != null)
            {
                filter.append(')');
            }
            filterLabel.setText(filter != null ? filter.toString() : "");
            if (isSelected)
            {
                cellPanel.setBackground(list.getSelectionBackground());
                jarNameLabel.setForeground(list.getSelectionForeground());
                filterLabel.setForeground(list.getSelectionForeground());
            }
            else
            {
                cellPanel.setBackground(list.getBackground());
                jarNameLabel.setForeground(list.getForeground());
                filterLabel.setForeground(list.getForeground());
            }
            if (!(inputAndOutput && entry.isOutput()) &&
                !entry.getFile().canRead())
            {
                jarNameLabel.setForeground(Color.red);
            }
            cellPanel.setOpaque(true);
            return cellPanel;
        }
        private StringBuffer appendFilter(StringBuffer filter, List additionalFilter)
        {
            if (filter != null)
            {
                filter.append(';');
            }
            if (additionalFilter != null)
            {
                if (filter == null)
                {
                    filter = new StringBuffer().append('(');
                }
                filter.append(ListUtil.commaSeparatedString(additionalFilter));
            }
            return filter;
        }
    }
}

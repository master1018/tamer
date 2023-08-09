abstract class ListPanel extends JPanel
{
    protected final DefaultListModel listModel = new DefaultListModel();
    protected final JList            list      = new JList(listModel);
    protected int firstSelectionButton = 2;
    protected ListPanel()
    {
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        GridBagConstraints listConstraints = new GridBagConstraints();
        listConstraints.gridheight = GridBagConstraints.REMAINDER;
        listConstraints.fill       = GridBagConstraints.BOTH;
        listConstraints.weightx    = 1.0;
        listConstraints.weighty    = 1.0;
        listConstraints.anchor     = GridBagConstraints.NORTHWEST;
        listConstraints.insets     = new Insets(0, 2, 0, 2);
        list.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                enableSelectionButtons();
            }
        });
        add(new JScrollPane(list), listConstraints);
    }
    protected void addRemoveButton()
    {
        JButton removeButton = new JButton(msg("remove"));
        removeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                removeElementsAt(list.getSelectedIndices());
            }
        });
        addButton(tip(removeButton, "removeTip"));
    }
    protected void addUpButton()
    {
        JButton upButton = new JButton(msg("moveUp"));
        upButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int[] selectedIndices = list.getSelectedIndices();
                if (selectedIndices.length > 0 &&
                    selectedIndices[0] > 0)
                {
                    moveElementsAt(selectedIndices, -1);
                }
            }
        });
        addButton(tip(upButton, "moveUpTip"));
    }
    protected void addDownButton()
    {
        JButton downButton = new JButton(msg("moveDown"));
        downButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int[] selectedIndices = list.getSelectedIndices();
                if (selectedIndices.length > 0 &&
                    selectedIndices[selectedIndices.length-1] < listModel.getSize()-1)
                {
                    moveElementsAt(selectedIndices, 1);
                }
            }
        });
        addButton(tip(downButton, "moveDownTip"));
    }
    public void addCopyToPanelButton(String          buttonTextKey,
                                     String          tipKey,
                                     final ListPanel panel)
    {
        JButton moveButton = new JButton(msg(buttonTextKey));
        moveButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int[]    selectedIndices  = list.getSelectedIndices();
                Object[] selectedElements = list.getSelectedValues();
                removeElementsAt(selectedIndices);
                panel.addElements(selectedElements);
            }
        });
        addButton(tip(moveButton, tipKey));
    }
    protected void addButton(JComponent button)
    {
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridwidth = GridBagConstraints.REMAINDER;
        buttonConstraints.fill      = GridBagConstraints.HORIZONTAL;
        buttonConstraints.anchor    = GridBagConstraints.NORTHWEST;
        buttonConstraints.insets    = new Insets(0, 2, 0, 2);
        add(button, buttonConstraints);
    }
    public List getButtons()
    {
        List list = new ArrayList(getComponentCount()-1);
        for (int index = 1; index < getComponentCount(); index++)
        {
            list.add(getComponent(index));
        }
        return list;
    }
    protected void addElement(Object element)
    {
        listModel.addElement(element);
        list.setSelectedIndex(listModel.size() - 1);
    }
    protected void addElements(Object[] elements)
    {
        for (int index = 0; index < elements.length; index++)
        {
            listModel.addElement(elements[index]);
        }
        int[] selectedIndices = new int[elements.length];
        for (int index = 0; index < selectedIndices.length; index++)
        {
            selectedIndices[index] =
                listModel.size() - selectedIndices.length + index;
        }
        list.setSelectedIndices(selectedIndices);
    }
    protected void moveElementsAt(int[] indices, int offset)
    {
        Object[] selectedElements = list.getSelectedValues();
        removeElementsAt(indices);
        for (int index = 0; index < indices.length; index++)
        {
            indices[index] += offset;
        }
        insertElementsAt(selectedElements, indices);
    }
    protected void insertElementsAt(Object[] elements, int[] indices)
    {
        for (int index = 0; index < elements.length; index++)
        {
            listModel.insertElementAt(elements[index], indices[index]);
        }
        list.setSelectedIndices(indices);
    }
    protected void setElementAt(Object element, int index)
    {
        listModel.setElementAt(element, index);
        list.setSelectedIndex(index);
    }
    protected void setElementsAt(Object[] elements, int[] indices)
    {
        for (int index = 0; index < elements.length; index++)
        {
            listModel.setElementAt(elements[index], indices[index]);
        }
        list.setSelectedIndices(indices);
    }
    protected void removeElementsAt(int[] indices)
    {
        for (int index = indices.length - 1; index >= 0; index--)
        {
            listModel.removeElementAt(indices[index]);
        }
        list.clearSelection();
        enableSelectionButtons();
    }
    protected void removeAllElements()
    {
        listModel.removeAllElements();
        enableSelectionButtons();
    }
    protected void enableSelectionButtons()
    {
        boolean selected = !list.isSelectionEmpty();
        for (int index = firstSelectionButton; index < getComponentCount(); index++)
        {
            getComponent(index).setEnabled(selected);
        }
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

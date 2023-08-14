import proguard.classfile.util.ClassUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
class ClassSpecificationsPanel extends ListPanel
{
    protected final ClassSpecificationDialog classSpecificationDialog;
    public ClassSpecificationsPanel(JFrame owner, boolean fullKeepOptions)
    {
        super();
        list.setCellRenderer(new MyListCellRenderer());
        classSpecificationDialog = new ClassSpecificationDialog(owner, fullKeepOptions);
        addAddButton();
        addEditButton();
        addRemoveButton();
        addUpButton();
        addDownButton();
        enableSelectionButtons();
    }
    protected void addAddButton()
    {
        JButton addButton = new JButton(msg("add"));
        addButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setClassSpecification(createClassSpecification());
                int returnValue = classSpecificationDialog.showDialog();
                if (returnValue == ClassSpecificationDialog.APPROVE_OPTION)
                {
                    addElement(getClassSpecification());
                }
            }
        });
        addButton(tip(addButton, "addTip"));
    }
    protected void addEditButton()
    {
        JButton editButton = new JButton(msg("edit"));
        editButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                ClassSpecification selectedClassSpecification =
                    (ClassSpecification)list.getSelectedValue();
                setClassSpecification(selectedClassSpecification);
                int returnValue = classSpecificationDialog.showDialog();
                if (returnValue == ClassSpecificationDialog.APPROVE_OPTION)
                {
                    setElementAt(getClassSpecification(),
                                 list.getSelectedIndex());
                }
            }
        });
        addButton(tip(editButton, "editTip"));
    }
    protected ClassSpecification createClassSpecification()
    {
        return new ClassSpecification();
    }
    protected void setClassSpecification(ClassSpecification classSpecification)
    {
        classSpecificationDialog.setClassSpecification(classSpecification);
    }
    protected ClassSpecification getClassSpecification()
    {
        return classSpecificationDialog.getClassSpecification();
    }
    public void setClassSpecifications(List classSpecifications)
    {
        listModel.clear();
        if (classSpecifications != null)
        {
            for (int index = 0; index < classSpecifications.size(); index++)
            {
                listModel.addElement(classSpecifications.get(index));
            }
        }
        enableSelectionButtons();
    }
    public List getClassSpecifications()
    {
        int size = listModel.size();
        if (size == 0)
        {
            return null;
        }
        List classSpecifications = new ArrayList(size);
        for (int index = 0; index < size; index++)
        {
            classSpecifications.add(listModel.get(index));
        }
        return classSpecifications;
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
    private static class MyListCellRenderer implements ListCellRenderer
    {
        private final JLabel label = new JLabel();
        public Component getListCellRendererComponent(JList   list,
                                                      Object  value,
                                                      int     index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            ClassSpecification classSpecification = (ClassSpecification)value;
            String comments = classSpecification.comments;
            label.setText(comments                            != null ? comments.trim()                                                                                        :
                          classSpecification.className        != null ? (msg("class") + ' ' + ClassUtil.externalClassName(classSpecification.className))               :
                          classSpecification.extendsClassName != null ? (msg("extensionsOf") + ' ' + ClassUtil.externalClassName(classSpecification.extendsClassName)) :
                                                                        (msg("specificationNumber") + index));
            if (isSelected)
            {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            else
            {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }
            label.setOpaque(true);
            return label;
        }
    }
}

import proguard.classfile.ClassConstants;
import proguard.classfile.util.ClassUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
final class MemberSpecificationsPanel extends ListPanel
{
    private final MemberSpecificationDialog fieldSpecificationDialog;
    private final MemberSpecificationDialog methodSpecificationDialog;
    public MemberSpecificationsPanel(JDialog owner, boolean fullKeepOptions)
    {
        super();
        super.firstSelectionButton = fullKeepOptions ? 3 : 2;
        list.setCellRenderer(new MyListCellRenderer());
        fieldSpecificationDialog  = new MemberSpecificationDialog(owner, true);
        methodSpecificationDialog = new MemberSpecificationDialog(owner, false);
        if (fullKeepOptions)
        {
            addAddFieldButton();
        }
        addAddMethodButton();
        addEditButton();
        addRemoveButton();
        addUpButton();
        addDownButton();
        enableSelectionButtons();
    }
    protected void addAddFieldButton()
    {
        JButton addFieldButton = new JButton(msg("addField"));
        addFieldButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fieldSpecificationDialog.setMemberSpecification(new MemberSpecification());
                int returnValue = fieldSpecificationDialog.showDialog();
                if (returnValue == MemberSpecificationDialog.APPROVE_OPTION)
                {
                    addElement(new MyMemberSpecificationWrapper(fieldSpecificationDialog.getMemberSpecification(),
                                                                  true));
                }
            }
        });
        addButton(tip(addFieldButton, "addFieldTip"));
    }
    protected void addAddMethodButton()
    {
        JButton addMethodButton = new JButton(msg("addMethod"));
        addMethodButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                methodSpecificationDialog.setMemberSpecification(new MemberSpecification());
                int returnValue = methodSpecificationDialog.showDialog();
                if (returnValue == MemberSpecificationDialog.APPROVE_OPTION)
                {
                    addElement(new MyMemberSpecificationWrapper(methodSpecificationDialog.getMemberSpecification(),
                                                                  false));
                }
            }
        });
        addButton(tip(addMethodButton, "addMethodTip"));
    }
    protected void addEditButton()
    {
        JButton editButton = new JButton(msg("edit"));
        editButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                MyMemberSpecificationWrapper wrapper =
                    (MyMemberSpecificationWrapper)list.getSelectedValue();
                MemberSpecificationDialog memberSpecificationDialog =
                    wrapper.isField ?
                        fieldSpecificationDialog :
                        methodSpecificationDialog;
                memberSpecificationDialog.setMemberSpecification(wrapper.memberSpecification);
                int returnValue = memberSpecificationDialog.showDialog();
                if (returnValue == MemberSpecificationDialog.APPROVE_OPTION)
                {
                    wrapper.memberSpecification = memberSpecificationDialog.getMemberSpecification();
                    setElementAt(wrapper,
                                 list.getSelectedIndex());
                }
            }
        });
        addButton(tip(editButton, "editTip"));
    }
    public void setMemberSpecifications(List fieldSpecifications,
                                        List methodSpecifications)
    {
        listModel.clear();
        if (fieldSpecifications != null)
        {
            for (int index = 0; index < fieldSpecifications.size(); index++)
            {
                listModel.addElement(
                    new MyMemberSpecificationWrapper((MemberSpecification)fieldSpecifications.get(index),
                                                     true));
            }
        }
        if (methodSpecifications != null)
        {
            for (int index = 0; index < methodSpecifications.size(); index++)
            {
                listModel.addElement(
                    new MyMemberSpecificationWrapper((MemberSpecification)methodSpecifications.get(index),
                                                     false));
            }
        }
        enableSelectionButtons();
    }
    public List getMemberSpecifications(boolean isField)
    {
        int size = listModel.size();
        if (size == 0)
        {
            return null;
        }
        List memberSpecifications = new ArrayList(size);
        for (int index = 0; index < size; index++)
        {
            MyMemberSpecificationWrapper wrapper =
                (MyMemberSpecificationWrapper)listModel.get(index);
            if (wrapper.isField == isField)
            {
                memberSpecifications.add(wrapper.memberSpecification);
            }
        }
        return memberSpecifications;
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
            MyMemberSpecificationWrapper wrapper = (MyMemberSpecificationWrapper)value;
            MemberSpecification option = wrapper.memberSpecification;
            String name       = option.name;
            String descriptor = option.descriptor;
            label.setText(wrapper.isField ?
                (descriptor == null ? name == null ?
                    "<fields>" :
                    "***" + ' ' + name :
                    ClassUtil.externalFullFieldDescription(0,
                                                           name == null ? "*" : name,
                                                           descriptor)) :
                (descriptor == null ? name == null ?
                    "<methods>" :
                    "***" + ' ' + name + "(...)" :
                    ClassUtil.externalFullMethodDescription(ClassConstants.INTERNAL_METHOD_NAME_INIT,
                                                            0,
                                                            name == null ? "*" : name,
                                                            descriptor)));
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
    private static JComponent tip(JComponent component, String messageKey)
    {
        component.setToolTipText(msg(messageKey));
        return component;
    }
    private static String msg(String messageKey)
    {
         return GUIResources.getMessage(messageKey);
    }
    private static class MyMemberSpecificationWrapper
    {
        public MemberSpecification memberSpecification;
        public final boolean             isField;
        public MyMemberSpecificationWrapper(MemberSpecification memberSpecification,
                                            boolean             isField)
        {
            this.memberSpecification = memberSpecification;
            this.isField                  = isField;
        }
    }
}

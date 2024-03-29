import proguard.classfile.ClassConstants;
import proguard.classfile.util.ClassUtil;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
final class ClassSpecificationDialog extends JDialog
{
    public static final int CANCEL_OPTION = 1;
    public static final int APPROVE_OPTION = 0;
    private final JTextArea commentsTextArea = new JTextArea(4, 20);
    private final JRadioButton keepClassesAndMembersRadioButton  = new JRadioButton(msg("keep"));
    private final JRadioButton keepClassMembersRadioButton       = new JRadioButton(msg("keepClassMembers"));
    private final JRadioButton keepClassesWithMembersRadioButton = new JRadioButton(msg("keepClassesWithMembers"));
    private final JCheckBox allowShrinkingCheckBox    = new JCheckBox(msg("allowShrinking"));
    private final JCheckBox allowOptimizationCheckBox = new JCheckBox(msg("allowOptimization"));
    private final JCheckBox allowObfuscationCheckBox  = new JCheckBox(msg("allowObfuscation"));
    private final JRadioButton[] publicRadioButtons;
    private final JRadioButton[] finalRadioButtons;
    private final JRadioButton[] abstractRadioButtons;
    private final JRadioButton[] enumRadioButtons;
    private final JRadioButton[] annotationRadioButtons;
    private final JRadioButton[] interfaceRadioButtons;
    private final JTextField annotationTypeTextField        = new JTextField(20);
    private final JTextField classNameTextField             = new JTextField(20);
    private final JTextField extendsAnnotationTypeTextField = new JTextField(20);
    private final JTextField extendsClassNameTextField      = new JTextField(20);
    private final MemberSpecificationsPanel memberSpecificationsPanel;
    private int returnValue;
    public ClassSpecificationDialog(JFrame owner, boolean fullKeepOptions)
    {
        super(owner, msg("specifyClasses"), true);
        setResizable(true);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(1, 2, 1, 2);
        GridBagConstraints constraintsStretch = new GridBagConstraints();
        constraintsStretch.fill    = GridBagConstraints.HORIZONTAL;
        constraintsStretch.weightx = 1.0;
        constraintsStretch.anchor  = GridBagConstraints.WEST;
        constraintsStretch.insets  = constraints.insets;
        GridBagConstraints constraintsLast = new GridBagConstraints();
        constraintsLast.gridwidth = GridBagConstraints.REMAINDER;
        constraintsLast.anchor    = GridBagConstraints.WEST;
        constraintsLast.insets    = constraints.insets;
        GridBagConstraints constraintsLastStretch = new GridBagConstraints();
        constraintsLastStretch.gridwidth = GridBagConstraints.REMAINDER;
        constraintsLastStretch.fill      = GridBagConstraints.HORIZONTAL;
        constraintsLastStretch.weightx   = 1.0;
        constraintsLastStretch.anchor    = GridBagConstraints.WEST;
        constraintsLastStretch.insets    = constraints.insets;
        GridBagConstraints panelConstraints = new GridBagConstraints();
        panelConstraints.gridwidth = GridBagConstraints.REMAINDER;
        panelConstraints.fill      = GridBagConstraints.HORIZONTAL;
        panelConstraints.weightx   = 1.0;
        panelConstraints.weighty   = 0.0;
        panelConstraints.anchor    = GridBagConstraints.NORTHWEST;
        panelConstraints.insets    = constraints.insets;
        GridBagConstraints stretchPanelConstraints = new GridBagConstraints();
        stretchPanelConstraints.gridwidth = GridBagConstraints.REMAINDER;
        stretchPanelConstraints.fill      = GridBagConstraints.BOTH;
        stretchPanelConstraints.weightx   = 1.0;
        stretchPanelConstraints.weighty   = 1.0;
        stretchPanelConstraints.anchor    = GridBagConstraints.NORTHWEST;
        stretchPanelConstraints.insets    = constraints.insets;
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.CENTER;
        labelConstraints.insets = new Insets(2, 10, 2, 10);
        GridBagConstraints lastLabelConstraints = new GridBagConstraints();
        lastLabelConstraints.gridwidth = GridBagConstraints.REMAINDER;
        lastLabelConstraints.anchor    = GridBagConstraints.CENTER;
        lastLabelConstraints.insets    = labelConstraints.insets;
        GridBagConstraints advancedButtonConstraints = new GridBagConstraints();
        advancedButtonConstraints.weightx = 1.0;
        advancedButtonConstraints.weighty = 1.0;
        advancedButtonConstraints.anchor  = GridBagConstraints.SOUTHWEST;
        advancedButtonConstraints.insets  = new Insets(4, 4, 8, 4);
        GridBagConstraints okButtonConstraints = new GridBagConstraints();
        okButtonConstraints.weightx = 1.0;
        okButtonConstraints.weighty = 1.0;
        okButtonConstraints.anchor  = GridBagConstraints.SOUTHEAST;
        okButtonConstraints.insets  = advancedButtonConstraints.insets;
        GridBagConstraints cancelButtonConstraints = new GridBagConstraints();
        cancelButtonConstraints.gridwidth = GridBagConstraints.REMAINDER;
        cancelButtonConstraints.weighty   = 1.0;
        cancelButtonConstraints.anchor    = GridBagConstraints.SOUTHEAST;
        cancelButtonConstraints.insets    = advancedButtonConstraints.insets;
        GridBagLayout layout = new GridBagLayout();
        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        JPanel commentsPanel = new JPanel(layout);
        commentsPanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                                 msg("comments")));
        JScrollPane commentsScrollPane = new JScrollPane(commentsTextArea);
        commentsScrollPane.setBorder(classNameTextField.getBorder());
        commentsPanel.add(tip(commentsScrollPane, "commentsTip"), constraintsLastStretch);
        ButtonGroup keepButtonGroup = new ButtonGroup();
        keepButtonGroup.add(keepClassesAndMembersRadioButton);
        keepButtonGroup.add(keepClassMembersRadioButton);
        keepButtonGroup.add(keepClassesWithMembersRadioButton);
        JPanel keepOptionPanel = new JPanel(layout);
        keepOptionPanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                                   msg("keepTitle")));
        keepOptionPanel.add(tip(keepClassesAndMembersRadioButton,  "keepTip"),                   constraintsLastStretch);
        keepOptionPanel.add(tip(keepClassMembersRadioButton,       "keepClassMembersTip"),       constraintsLastStretch);
        keepOptionPanel.add(tip(keepClassesWithMembersRadioButton, "keepClassesWithMembersTip"), constraintsLastStretch);
        final JPanel allowOptionPanel = new JPanel(layout);
        allowOptionPanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                                    msg("allowTitle")));
        allowOptionPanel.add(tip(allowShrinkingCheckBox,    "allowShrinkingTip"),    constraintsLastStretch);
        allowOptionPanel.add(tip(allowOptimizationCheckBox, "allowOptimizationTip"), constraintsLastStretch);
        allowOptionPanel.add(tip(allowObfuscationCheckBox,  "allowObfuscationTip"),  constraintsLastStretch);
        JPanel accessPanel = new JPanel(layout);
        accessPanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                               msg("access")));
        accessPanel.add(Box.createGlue(),                                labelConstraints);
        accessPanel.add(tip(new JLabel(msg("required")), "requiredTip"), labelConstraints);
        accessPanel.add(tip(new JLabel(msg("not")),      "notTip"),      labelConstraints);
        accessPanel.add(tip(new JLabel(msg("dontCare")), "dontCareTip"), labelConstraints);
        accessPanel.add(Box.createGlue(),                                constraintsLastStretch);
        publicRadioButtons     = addRadioButtonTriplet("Public",     accessPanel);
        finalRadioButtons      = addRadioButtonTriplet("Final",      accessPanel);
        abstractRadioButtons   = addRadioButtonTriplet("Abstract",   accessPanel);
        enumRadioButtons       = addRadioButtonTriplet("Enum",       accessPanel);
        annotationRadioButtons = addRadioButtonTriplet("Annotation", accessPanel);
        interfaceRadioButtons  = addRadioButtonTriplet("Interface",  accessPanel);
        final JPanel annotationTypePanel = new JPanel(layout);
        annotationTypePanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                                       msg("annotation")));
        annotationTypePanel.add(tip(annotationTypeTextField, "classNameTip"), constraintsLastStretch);
        JPanel classNamePanel = new JPanel(layout);
        classNamePanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                                  msg("class")));
        classNamePanel.add(tip(classNameTextField, "classNameTip"), constraintsLastStretch);
        final JPanel extendsAnnotationTypePanel = new JPanel(layout);
        extendsAnnotationTypePanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                                              msg("extendsImplementsAnnotation")));
        extendsAnnotationTypePanel.add(tip(extendsAnnotationTypeTextField, "classNameTip"), constraintsLastStretch);
        JPanel extendsClassNamePanel = new JPanel(layout);
        extendsClassNamePanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                                         msg("extendsImplementsClass")));
        extendsClassNamePanel.add(tip(extendsClassNameTextField, "classNameTip"), constraintsLastStretch);
        memberSpecificationsPanel = new MemberSpecificationsPanel(this, fullKeepOptions);
        memberSpecificationsPanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                                             msg("classMembers")));
        final JButton advancedButton = new JButton(msg("basic"));
        advancedButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                boolean visible = !allowOptionPanel.isVisible();
                allowOptionPanel          .setVisible(visible);
                annotationTypePanel       .setVisible(visible);
                extendsAnnotationTypePanel.setVisible(visible);
                advancedButton.setText(msg(visible ? "basic" : "advanced"));
                pack();
            }
        });
        advancedButton.doClick();
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
        mainPanel.add(tip(commentsPanel,              "commentsTip"),                    panelConstraints);
        if (fullKeepOptions)
        {
            mainPanel.add(tip(keepOptionPanel,        "keepTitleTip"),                   panelConstraints);
            mainPanel.add(tip(allowOptionPanel,       "allowTitleTip"),                  panelConstraints);
        }
        mainPanel.add(tip(accessPanel,                "accessTip"),                      panelConstraints);
        mainPanel.add(tip(annotationTypePanel,        "annotationTip"),                  panelConstraints);
        mainPanel.add(tip(classNamePanel,             "classTip"),                       panelConstraints);
        mainPanel.add(tip(extendsAnnotationTypePanel, "extendsImplementsAnnotationTip"), panelConstraints);
        mainPanel.add(tip(extendsClassNamePanel,      "extendsImplementsClassTip"),      panelConstraints);
        mainPanel.add(tip(memberSpecificationsPanel,  "classMembersTip"),                stretchPanelConstraints);
        mainPanel.add(tip(advancedButton, "advancedTip"), advancedButtonConstraints);
        mainPanel.add(okButton,                           okButtonConstraints);
        mainPanel.add(cancelButton,                       cancelButtonConstraints);
        getContentPane().add(new JScrollPane(mainPanel));
    }
    private JRadioButton[] addRadioButtonTriplet(String labelText,
                                                 JPanel panel)
    {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(2, 10, 2, 10);
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.insets = labelConstraints.insets;
        GridBagConstraints lastGlueConstraints = new GridBagConstraints();
        lastGlueConstraints.gridwidth = GridBagConstraints.REMAINDER;
        lastGlueConstraints.weightx   = 1.0;
        JRadioButton radioButton0 = new JRadioButton();
        JRadioButton radioButton1 = new JRadioButton();
        JRadioButton radioButton2 = new JRadioButton();
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(radioButton0);
        buttonGroup.add(radioButton1);
        buttonGroup.add(radioButton2);
        panel.add(new JLabel(labelText), labelConstraints);
        panel.add(radioButton0,          buttonConstraints);
        panel.add(radioButton1,          buttonConstraints);
        panel.add(radioButton2,          buttonConstraints);
        panel.add(Box.createGlue(),      lastGlueConstraints);
        return new JRadioButton[]
        {
             radioButton0,
             radioButton1,
             radioButton2
        };
    }
    public void setKeepSpecification(KeepClassSpecification keepClassSpecification)
    {
        boolean markClasses       = keepClassSpecification.markClasses;
        boolean markConditionally = keepClassSpecification.markConditionally;
        boolean allowShrinking    = keepClassSpecification.allowShrinking;
        boolean allowOptimization = keepClassSpecification.allowOptimization;
        boolean allowObfuscation  = keepClassSpecification.allowObfuscation;
        JRadioButton keepOptionRadioButton =
            markConditionally ? keepClassesWithMembersRadioButton :
            markClasses       ? keepClassesAndMembersRadioButton  :
                                keepClassMembersRadioButton;
        keepOptionRadioButton.setSelected(true);
        allowShrinkingCheckBox   .setSelected(allowShrinking);
        allowOptimizationCheckBox.setSelected(allowOptimization);
        allowObfuscationCheckBox .setSelected(allowObfuscation);
        setClassSpecification(keepClassSpecification);
    }
    public void setClassSpecification(ClassSpecification classSpecification)
    {
        String comments              = classSpecification.comments;
        String annotationType        = classSpecification.annotationType;
        String className             = classSpecification.className;
        String extendsAnnotationType = classSpecification.extendsAnnotationType;
        String extendsClassName      = classSpecification.extendsClassName;
        List   keepFieldOptions      = classSpecification.fieldSpecifications;
        List   keepMethodOptions     = classSpecification.methodSpecifications;
        commentsTextArea.setText(comments == null ? "" : comments);
        setClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_PUBLIC,      publicRadioButtons);
        setClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_FINAL,       finalRadioButtons);
        setClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_ABSTRACT,    abstractRadioButtons);
        setClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_ENUM,        enumRadioButtons);
        setClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_ANNOTATTION, annotationRadioButtons);
        setClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_INTERFACE,   interfaceRadioButtons);
        annotationTypeTextField       .setText(annotationType        == null ? ""  : ClassUtil.externalType(annotationType));
        classNameTextField            .setText(className             == null ? "*" : ClassUtil.externalClassName(className));
        extendsAnnotationTypeTextField.setText(extendsAnnotationType == null ? ""  : ClassUtil.externalType(extendsAnnotationType));
        extendsClassNameTextField     .setText(extendsClassName      == null ? ""  : ClassUtil.externalClassName(extendsClassName));
        memberSpecificationsPanel.setMemberSpecifications(keepFieldOptions, keepMethodOptions);
    }
    public KeepClassSpecification getKeepSpecification()
    {
        boolean markClasses       = !keepClassMembersRadioButton     .isSelected();
        boolean markConditionally = keepClassesWithMembersRadioButton.isSelected();
        boolean allowShrinking    = allowShrinkingCheckBox           .isSelected();
        boolean allowOptimization = allowOptimizationCheckBox        .isSelected();
        boolean allowObfuscation  = allowObfuscationCheckBox         .isSelected();
        return new KeepClassSpecification(markClasses,
                                     markConditionally,
                                     allowShrinking,
                                     allowOptimization,
                                     allowObfuscation,
                                     getClassSpecification());
    }
    public ClassSpecification getClassSpecification()
    {
        String comments              = commentsTextArea.getText();
        String annotationType        = annotationTypeTextField.getText();
        String className             = classNameTextField.getText();
        String extendsAnnotationType = extendsAnnotationTypeTextField.getText();
        String extendsClassName      = extendsClassNameTextField.getText();
        ClassSpecification classSpecification =
            new ClassSpecification(comments.equals("")              ? null : comments,
                                   0,
                                   0,
                                   annotationType.equals("")        ? null : ClassUtil.internalType(annotationType),
                                   className.equals("") ||
                                   className.equals("*")            ? null : ClassUtil.internalClassName(className),
                                   extendsAnnotationType.equals("") ? null : ClassUtil.internalType(extendsAnnotationType),
                                   extendsClassName.equals("")      ? null : ClassUtil.internalClassName(extendsClassName));
        getClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_PUBLIC,      publicRadioButtons);
        getClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_FINAL,       finalRadioButtons);
        getClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_ABSTRACT,    abstractRadioButtons);
        getClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_ENUM,        enumRadioButtons);
        getClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_ANNOTATTION, annotationRadioButtons);
        getClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_INTERFACE,   interfaceRadioButtons);
        classSpecification.fieldSpecifications  = memberSpecificationsPanel.getMemberSpecifications(true);
        classSpecification.methodSpecifications = memberSpecificationsPanel.getMemberSpecifications(false);
        return classSpecification;
    }
    public int showDialog()
    {
        returnValue = CANCEL_OPTION;
        pack();
        setLocationRelativeTo(getOwner());
        show();
        return returnValue;
    }
    private void setClassSpecificationRadioButtons(ClassSpecification classSpecification,
                                                   int                flag,
                                                   JRadioButton[]     radioButtons)
    {
        int index = (classSpecification.requiredSetAccessFlags   & flag) != 0 ? 0 :
                    (classSpecification.requiredUnsetAccessFlags & flag) != 0 ? 1 :
                                                                                 2;
        radioButtons[index].setSelected(true);
    }
    private void getClassSpecificationRadioButtons(ClassSpecification classSpecification,
                                                   int                flag,
                                                   JRadioButton[]     radioButtons)
    {
        if      (radioButtons[0].isSelected())
        {
            classSpecification.requiredSetAccessFlags   |= flag;
        }
        else if (radioButtons[1].isSelected())
        {
            classSpecification.requiredUnsetAccessFlags |= flag;
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

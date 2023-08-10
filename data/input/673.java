public class ChoiceListEditor extends PropertyEditorSupport {
    public boolean supportsCustomEditor() {
        return true;
    }
    public Component getCustomEditor() {
        ChoiceList list = (ChoiceList) getValue();
        final JComboBox comboBox = new JComboBox(list.getList());
        comboBox.setSelectedItem(list.getSelectedItem());
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selected = (String) comboBox.getSelectedItem();
                ChoiceList list = (ChoiceList) getValue();
                list.setSelectedItem(selected);
            }
        });
        return comboBox;
    }
}

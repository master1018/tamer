class OptionComboBoxModel extends DefaultComboBoxModel implements Serializable {
    private Option selectedOption = null;
    public void setInitialSelection(Option option) {
        selectedOption = option;
    }
    public Option getInitialSelection() {
        return selectedOption;
    }
}

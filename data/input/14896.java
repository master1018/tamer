public final class java_awt_GridBagConstraints extends AbstractTest<GridBagConstraints> {
    public static void main(String[] args) {
        new java_awt_GridBagConstraints().test(true);
    }
    protected GridBagConstraints getObject() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.gridheight = 4;
        gbc.weightx = 0.1;
        gbc.weighty = 0.2;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets.top = 1;
        gbc.insets.left = 2;
        gbc.insets.right = 3;
        gbc.insets.bottom = 4;
        gbc.ipadx = -1;
        gbc.ipady = -2;
        return gbc;
    }
    protected GridBagConstraints getAnotherObject() {
        return new GridBagConstraints();
    }
}

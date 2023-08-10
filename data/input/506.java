public class GB_TButtonAllSA extends GB_ToolButton implements ActionListener {
    private GB_TextOwnerSet textOwner;
    public GB_TButtonAllSA(GB_TextOwnerSet a_textOwner) {
        super(AA.ICON.X16_TOOLS);
        this.setToolTipText(AA.BUTTON_ALL_SA_DESC);
        textOwner = a_textOwner;
        this.addActionListener(this);
    }
    public void actionPerformed(ActionEvent actionEvent) {
        GB_AllSATN l_treeNode = new GB_AllSATN();
        GB_AllSATNVM l_vm = new GB_AllSATNVM(l_treeNode, textOwner);
        String l_title = AA.TITLE_ALL_SA;
        GB_VMTools.showIntoFrame(l_vm, l_title, false);
    }
}

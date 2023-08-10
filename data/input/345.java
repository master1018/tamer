public class SaveListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        MapMakerIO.getInstance().saveProcess();
    }
}

public class bug6738668 {
    public static void main(String[] args) throws Exception {
        for (UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels()) {
            UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
            new JFileChooser(new File("c:/temp"));
            System.out.println("Test passed for LookAndFeel " + lookAndFeelInfo.getClassName());
        }
    }
}

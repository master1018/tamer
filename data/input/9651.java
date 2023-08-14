public class bug7036025 {
    public static final String DIR = "c:/temp";
    public static void main(String[] args) throws Exception {
        String systemLookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
        if (!systemLookAndFeelClassName.toLowerCase().contains("windows")) {
            System.out.println("The test is only for Windows OS.");
            return;
        }
        File file = new File(DIR);
        if (!file.exists()) {
            if (!file.mkdir()) {
                throw new RuntimeException("Cannot create " + DIR);
            }
            file.deleteOnExit();
        }
        UIManager.setLookAndFeel(systemLookAndFeelClassName);
        new JFileChooser(file);
        System.out.println("Test passed for LookAndFeel " + UIManager.getLookAndFeel().getName());
    }
}

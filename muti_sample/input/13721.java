class Launcher {
    public static void main(String... args) {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        JFileChooser fileChooser;
        Preferences prefs = Preferences.userNodeForPackage(Launcher.class);
        if (args.length > 0)
            fileChooser = new JFileChooser(args[0]);
        else {
            String fileName = prefs.get("recent.file", null);
            fileChooser = new JFileChooser();
            if (fileName != null) {
                fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new File(fileName));
            }
        }
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            String fileName = fileChooser.getSelectedFile().getPath();
            prefs.put("recent.file", fileName);
            javac.run(System.in, null, null, "-d", "/tmp", fileName);
        }
    }
}

public class InstantiateMojo extends AbstractMojo {
    String id;
    private File outputPath;
    public int objectFactoryClassnameColumnIndex;
    public int filenameColumnIndex;
    public String fileext;
    public File xlsFile;
    private MavenProject project;
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            String filePath = outputPath.getAbsolutePath() + System.getProperty("file.separator") + id;
            Workbook workbook = Workbook.getWorkbook(xlsFile);
            for (Sheet s : workbook.getSheets()) {
                List<String> keys = new ArrayList<String>();
                for (Cell c : s.getRow(0)) {
                    keys.add(c.getContents());
                }
                for (int i = 1; i < s.getRows(); i++) {
                    Properties factoryProp = new Properties();
                    String fileName = null;
                    String objectFactoryClassname = null;
                    for (Cell c : s.getRow(i)) {
                        if (c.getColumn() == filenameColumnIndex) fileName = c.getContents();
                        if (c.getColumn() == objectFactoryClassnameColumnIndex) objectFactoryClassname = c.getContents();
                        factoryProp.put(keys.get(c.getColumn()), c.getContents());
                    }
                    Class<?> factoryClass = Class.forName(objectFactoryClassname);
                    ObjectFactory factory = (ObjectFactory) factoryClass.newInstance();
                    Object o = factory.instantiate(factoryProp, getLog());
                    writeObject(o, filePath + System.getProperty("file.separator") + s.getName(), fileName);
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed", e);
        }
    }
    private void writeObject(Object o, String filePath, String filename) throws IOException {
        File fileDestDir = new File(filePath);
        fileDestDir.mkdirs();
        File destFile = new File(fileDestDir, filename + (fileext != null ? ("." + fileext) : ""));
        destFile.delete();
        FileOutputStream fos = new FileOutputStream(destFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(o);
    }
}

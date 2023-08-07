class Directory implements FileSystemUnit {
    private File directory;
    private String relativePath;
    private List<FileSystemUnit> list = null;
    public Directory(String absolutePath, String relativePath) {
        this(new File(absolutePath), relativePath);
    }
    public Directory(File directory, String relativePath) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("File is not a directory.");
        }
        this.directory = directory;
        this.relativePath = (relativePath == null ? "" : relativePath) + directory.getName() + "/";
    }
    public String getRelativePath() {
        return relativePath;
    }
    public List<FileSystemUnit> getDirList() {
        if (list == null) {
            list = new ArrayList<FileSystemUnit>();
            String[] children = directory.list();
            for (String filename : children) {
                FileContainer file = new FileContainer(directory.getAbsolutePath() + "/" + filename, relativePath);
                if (file.isDirectory()) {
                    list.add(new Directory(file, relativePath));
                } else {
                    list.add(file);
                }
            }
        }
        return list;
    }
    public void write(ZipOutputStream out) throws IOException {
        getDirList();
        for (FileSystemUnit file : list) {
            file.write(out);
        }
    }
}

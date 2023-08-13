public class BinaryApi implements IApiExternalizer, IApiLoader {
    public void externalizeApi(String fileName, IApi api) throws IOException {
        File directory = new File(fileName);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, getFileName(api));
        file.createNewFile();
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
                file));
        oos.writeObject(api);
        oos.flush();
        oos.close();
    }
    private String getFileName(IApi api) {
        return api.getName().replaceAll(" ", "_").concat(".sig");
    }
    public IApi loadApi(String name, Visibility visibility,
            Set<String> fileNames, Set<String> packageNames) throws
            IOException {
        System.err
                .println("Binary signature loader ignores visibility and " +
                        "package names.");
        if (fileNames.size() != 1) {
            throw new IllegalArgumentException(
                    "Only one file can be processed by the binary signature " +
                    "loader.");
        }
        String fileName = fileNames.iterator().next();
        File file = new File(fileName);
        ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file));
        IApi sig = null;
        try {
            sig = (IApi) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        if (name != null) {
            sig.setName(name);
        }
        ois.close();
        return sig;
    }
}

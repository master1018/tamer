public class DeleteFileUtils {
    public static Exception checkFile(final File file) {
        Exception ex = null;
        String error = null;
        if (!file.exists()) {
            error = "The " + file + " does not exists.";
            ex = new FileDoesNotExistException(error);
            return ex;
        }
        if (!file.isDirectory()) {
            error = "The " + file + " is not a directory.";
            ex = new FileIsNotADirectoryException(error);
            return ex;
        }
        final File[] ff = file.listFiles();
        if (ff == null) {
            error = "The " + file + " could not list the content.";
            ex = new DirectoryHasNoContentException(error);
        }
        return ex;
    }
    public static void deleteFiles(final File file) throws IOException {
        final File[] ff = file.listFiles();
        if (ff != null) {
            for (int i = 0; i < ff.length; i++) {
                final File f = ff[i];
                DeleteFileUtils.delete(f);
            }
        }
    }
    public static void deleteFilesWithFileFilter(File source, FileFilter includeFileFilter) throws FileIsNotADirectoryException, IOException, FileIsSecurityRestrictedException {
        DeleteFileUtils.deleteFilesWithFileFilter(source, includeFileFilter, null);
    }
    public static void deleteFilesWithFileFilter(File source, FileFilter includeFileFilter, FileFilter excludeFileFilter) throws FileIsNotADirectoryException, IOException, FileIsSecurityRestrictedException {
        if (!source.isDirectory()) {
            throw new FileIsNotADirectoryException("Source file '" + source.getAbsolutePath() + "' is not a directory.");
        }
        File[] includeFilesArray;
        if (null != includeFileFilter) {
            includeFilesArray = source.listFiles(includeFileFilter);
        } else {
            includeFilesArray = source.listFiles();
        }
        if (null != includeFilesArray) {
            File[] excludeFilesArray = null;
            List<File> excludeFilesList = null;
            if (null != excludeFileFilter) {
                excludeFilesArray = source.listFiles(excludeFileFilter);
                excludeFilesList = Arrays.asList(excludeFilesArray);
            }
            if (null != excludeFilesList && (!excludeFilesList.isEmpty())) {
                for (int i = 0; i < includeFilesArray.length; i++) {
                    File currentFile = includeFilesArray[i];
                    if (!excludeFilesList.contains(currentFile)) {
                        if (currentFile.isDirectory()) {
                            deleteFilesWithFileFilter(currentFile, includeFileFilter, excludeFileFilter);
                        } else {
                            deleteFile(currentFile);
                        }
                    }
                }
            } else {
                for (int i = 0; i < includeFilesArray.length; i++) {
                    File currentFile = includeFilesArray[i];
                    if (currentFile.isDirectory()) {
                        deleteFilesWithFileFilter(currentFile, includeFileFilter, excludeFileFilter);
                    } else {
                        deleteFile(currentFile);
                    }
                }
            }
        } else {
            throw new FileIsSecurityRestrictedException("File '" + source.getAbsolutePath() + "' is security restricted.");
        }
    }
    public static void deleteFilesWithFilenameFilter(File source, FilenameFilter includeFilenameFilter) throws FileIsNotADirectoryException, IOException, FileIsSecurityRestrictedException {
        DeleteFileUtils.deleteFilesWithFilenameFilter(source, includeFilenameFilter, null);
    }
    public static void deleteFilesWithFilenameFilter(File source, FilenameFilter includeFilenameFilter, FilenameFilter excludeFilenameFilter) throws FileIsNotADirectoryException, IOException, FileIsSecurityRestrictedException {
        if (!source.isDirectory()) {
            throw new FileIsNotADirectoryException("Source file '" + source.getAbsolutePath() + "' is not a directory.");
        }
        File[] includeFilesArray;
        if (null != includeFilenameFilter) {
            includeFilesArray = source.listFiles(includeFilenameFilter);
        } else {
            includeFilesArray = source.listFiles();
        }
        if (null != includeFilesArray) {
            File[] excludeFilesArray = null;
            List<File> excludeFilesList = null;
            if (null != excludeFilenameFilter) {
                excludeFilesArray = source.listFiles(excludeFilenameFilter);
                excludeFilesList = Arrays.asList(excludeFilesArray);
            }
            if (null != excludeFilesList && (!excludeFilesList.isEmpty())) {
                for (int i = 0; i < includeFilesArray.length; i++) {
                    File currentFile = includeFilesArray[i];
                    if (!excludeFilesList.contains(currentFile)) {
                        if (currentFile.isDirectory()) {
                            deleteFilesWithFilenameFilter(currentFile, includeFilenameFilter, excludeFilenameFilter);
                        } else {
                            deleteFile(currentFile);
                        }
                    }
                }
            } else {
                for (int i = 0; i < includeFilesArray.length; i++) {
                    File currentFile = includeFilesArray[i];
                    if (currentFile.isDirectory()) {
                        deleteFilesWithFilenameFilter(currentFile, includeFilenameFilter, excludeFilenameFilter);
                    } else {
                        deleteFile(currentFile);
                    }
                }
            }
        } else {
            throw new FileIsSecurityRestrictedException("File '" + source.getAbsolutePath() + "' is security restricted.");
        }
    }
    public static void delete(final File file) throws IOException {
        if (file.isDirectory()) {
            DeleteFileUtils.deleteAllFiles(file);
        } else {
            String error = null;
            if (!file.delete()) {
                error = "Cannot delete the File " + file.getAbsolutePath() + ".";
                throw new IOException(error);
            }
        }
    }
    public static void deleteAllFiles(final File file) throws IOException {
        String error = null;
        if (!file.exists()) {
            return;
        }
        final Exception ex = checkFile(file);
        if (ex != null) {
            try {
                throw ex;
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        DeleteFileUtils.deleteFiles(file);
        if (!file.delete()) {
            error = "Cannot delete the File " + file.getAbsolutePath() + ".";
            throw new IOException(error);
        }
    }
    public static void deleteAllFilesWithSuffix(final File file, final String theSuffix) throws IOException {
        final String filePath = file.getAbsolutePath();
        final String suffix[] = { theSuffix };
        final Vector<File> files = FileSearchUtils.findFiles(filePath, suffix);
        final int fileCount = files.size();
        for (int i = 0; i < fileCount; i++) {
            DeleteFileUtils.deleteFile(files.elementAt(i));
        }
    }
    public static void deleteFile(final File fileToDelete) throws IOException {
        delete(fileToDelete);
    }
}

public final class FileCheck extends FileAction implements Decidable {
    static final long serialVersionUID = 400;
    public FileCheck(String name, byte[] extra_information, ResourceSet resources, String file) {
        super(name, extra_information, resources);
        this.file = file;
    }
    public FileCheck(String name, String file) {
        this(name, (byte[]) null, (ResourceSet) null, file);
    }
    public FileCheck(String name) {
        this(name, (byte[]) null, (ResourceSet) null, (String) null);
    }
    public FileCheck() {
        this("");
    }
    private String file;
    public String getFileToCheck() {
        return file;
    }
    public void setFileToCheck(String file) {
        this.file = file;
    }
    private Boolean file_exists;
    public void setFileExists(Boolean file_exists) {
        this.file_exists = file_exists;
    }
    public Boolean getFileExists() {
        return file_exists;
    }
    private Boolean can_read_file;
    public void setCanReadFile(Boolean can_read_file) {
        this.can_read_file = can_read_file;
    }
    public Boolean getCanReadFile() {
        return can_read_file;
    }
    private Boolean can_write_file;
    public void setCanWriteFile(Boolean can_write_file) {
        this.can_write_file = can_write_file;
    }
    public Boolean getCanWriteFile() {
        return can_write_file;
    }
    private Boolean can_execute_file;
    public void setCanExecuteFile(Boolean can_execute_file) {
        this.can_execute_file = can_execute_file;
    }
    public Boolean getCanExecuteFile() {
        return can_execute_file;
    }
    private Boolean file_owned_by_user;
    public void setFileOwnedByUser(Boolean file_owned_by_user) {
        this.file_owned_by_user = file_owned_by_user;
    }
    public Boolean getFileOwnedByUser() {
        return file_owned_by_user;
    }
    private Boolean is_directory;
    public void setIsDirectory(Boolean is_directory) {
        this.is_directory = is_directory;
    }
    public Boolean getIsDirectory() {
        return is_directory;
    }
    private Expression file_size;
    public void setFileSize(Expression file_size) {
        if (file_size == null || file_size.getValue() instanceof java.lang.Long) {
            this.file_size = file_size;
        } else {
            throw new ClassCastException("The value of the expression for the file size in FileCheck must be a Long, got: " + file_size.getValue().getClass());
        }
    }
    public Expression getFileSize() {
        return file_size;
    }
    private Expression file_modification_date;
    public void setFileModificationDate(Expression file_modification_date) {
        if (file_modification_date == null || file_modification_date.getValue() instanceof java.util.Date) {
            this.file_modification_date = file_modification_date;
        } else {
            throw new ClassCastException("The value of the expression for the file date in FileCheck must be a java.util.Date, got: " + file_modification_date.getValue().getClass());
        }
    }
    public Expression getFileModificationDate() {
        return file_modification_date;
    }
}

public class BackupTestAgent extends BackupAgentHelper
{
    public void onCreate() {
        addHelper("data_files", new FileBackupHelper(this, BackupTestActivity.FILE_NAME));
        addHelper("more_data_files", new FileBackupHelper(this, "another_file.txt", "3.txt",
                    "empty.txt"));
        addHelper("shared_prefs", new SharedPreferencesBackupHelper(this, "settings", "raw"));
    }
}

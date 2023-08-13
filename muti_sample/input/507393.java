public class LatinIMEBackupAgent extends BackupAgentHelper {
    public void onCreate() {
        addHelper("shared_pref", new SharedPreferencesBackupHelper(this,
                getPackageName() + "_preferences"));
    }
}

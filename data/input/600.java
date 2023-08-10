public class RelationalDBPage extends DataRepositoryPage {
    @Length(max = 100, msg = "", checkSize = true)
    @Ui(desc = "Name", id = "rdb_name")
    public UiInputText name;
    @Length(max = 30, msg = "", checkSize = true)
    @Ui(desc = "External Name", id = "rdb_externalName")
    public UiInputText externalName;
    @Length(max = 255, msg = "", checkSize = true)
    @Ui(desc = "Description", id = "rdb_descrition")
    public UiInputText description;
    @Length(max = 255, msg = "", checkSize = true)
    @Ui(desc = "Url", id = "rdb_url")
    public UiInputText url;
    @Length(max = 255, msg = "", checkSize = true)
    @Ui(desc = "Driver", id = "rdb_driver")
    public UiInputText driver;
    @Length(max = 30, msg = "", checkSize = true)
    @Ui(desc = "User", id = "rdb_user")
    public UiInputText user;
    @Ui(desc = "Forçar senha vazia", id = "relationalDataBase_forceEmptyPassword")
    public UiCheckBox forceEmptyPassword;
    @Length(max = 30, msg = "", checkSize = true)
    @Ui(desc = "Password", id = "rdb_password")
    public UiInputText password;
    @Length(max = 2, msg = "")
    @Ui(desc = "Visibility", id = "rdb_visibility")
    public UiSelect visibility;
    @Ui(desc = "MaxRows", id = "rdb_publicExportingMaxRows")
    public UiInputText maxRows;
    @Ui(desc = "OwnerPopup", id = "rdb_imgSearch")
    @RequestConfig(window = IRequest.Window.OPEN)
    public UiImage owner;
    @Ui(desc = "Owner", id = "rdb_owner_name_aux")
    public UiInputText owner_aux;
    @Ui(desc = "TestRepository", id = "buttonTestConnection")
    public UiButton testrepository;
    @Ui(desc = "DataBaseVersion", id = "rdb_databaseVersion")
    public UiInputText databaseVersion;
    @Ui(desc = "DataBaseVersionTable", id = "rdb_databaseVersionTable")
    public UiInputText databaseVersionTable;
    @Ui(desc = "DataBaseVersionField", id = "rdb_databaseVersionField")
    public UiInputText databaseVersionField;
    @Ui(id = "authorizedHarvesterSelTable")
    public UiTable<Collector> collectors;
    @Line
    public static class Collector extends GenericLine {
        @Ui(desc = "Collector [#{index}]", id = "detailAuthorizedHarvester_harvester_id")
        public UiSelect collector;
    }
    @Override
    public void invokeModify(int id) {
        invoke(Context.getInstance().getConfig().getPath() + "/RelationalDataBase.faces?event=edit&id=" + id);
    }
}

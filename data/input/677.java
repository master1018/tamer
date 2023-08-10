public class AddManufacturer {
    @ApplicationState
    private User user;
    private boolean userExists;
    Object onActivate() {
        if (!userExists) return Index.class; else {
            if (!user.isAdmin()) return Index.class;
        }
        return null;
    }
    @Property
    @Persist("flash")
    private Manufacturer manufacturer;
    private String name;
    private String description;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    @Inject
    @SpringBean("com.mirovicjovan.carmachine.service.admin.impl.WriteCommand")
    private WriteCommand<Manufacturer> writer;
    @OnEvent(value = "submit", component = "manufacturerCreationForm")
    void onFormSubmit() {
        manufacturer = new ManufacturerBean();
        manufacturer.setName(getName());
        manufacturer.setDescription(getDescription());
        writer.setInput(manufacturer);
        writer.execute();
        System.out.println("\n\n\n\n\n" + manufacturer.getName() + " " + manufacturer.getDescription() + "\n\n\n\n\n");
    }
}

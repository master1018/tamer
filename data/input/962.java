@HasExtraButton(label = "Create Contract", action = CreateContractAction.class, show = ModuleAction.DETAIL)
public class Offering extends AbstractEntity {
    @Label("Unique Services")
    @FieldTableAnnotation(UniqueService.class)
    @OneToMany(UniqueService.class)
    public ArrayList<Key> uniqueServices;
    @Label("Recurring Services")
    @FieldTableAnnotation(RecurringService.class)
    @OneToMany(RecurringService.class)
    public ArrayList<Key> recurringServices;
    @Label("Contact")
    @FieldRelateAnnotation(Contact.class)
    public Key contactID;
    @FieldDateAnnotation
    @Label("Deadline")
    public Date deadline;
    @Label("Contract")
    @FieldRelateAnnotation(Contract.class)
    public Key contractID;
    @Label("Account")
    @FieldRelateAnnotation(Account.class)
    public Key accountID;
    public Double getCosts() {
        return 23.0;
    }
}

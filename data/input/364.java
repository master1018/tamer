public class test {
    public void setInternalDoubleField(int field, double newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation("Not allowed to read/write to a instance marked for deletion");
    }
}

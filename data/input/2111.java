public class test {
    public void setInternalShortField(int field, short newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation("Not allowed to read/write to a instance marked for deletion");
    }
}

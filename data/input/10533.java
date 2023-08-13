public class DefaultResponseControlFactory extends ControlFactory {
    public DefaultResponseControlFactory() {
    }
    public Control getControlInstance(Control ctl)
        throws NamingException {
        String id = ctl.getID();
        try {
            if (id.equals(SortResponseControl.OID)) {
                return new SortResponseControl(id, ctl.isCritical(),
                    ctl.getEncodedValue());
            } else if (id.equals(PagedResultsResponseControl.OID)) {
                return new PagedResultsResponseControl(id, ctl.isCritical(),
                    ctl.getEncodedValue());
            } else if (id.equals(EntryChangeResponseControl.OID)) {
                return new EntryChangeResponseControl(id, ctl.isCritical(),
                    ctl.getEncodedValue());
            }
        } catch (IOException e) {
            NamingException ne = new NamingException();
            ne.setRootCause(e);
            throw ne;
        }
        return null;
    }
}

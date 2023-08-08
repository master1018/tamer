public class FindAll extends DistPollerMappingQuery {
    public FindAll(DataSource ds) {
        super(ds, "FROM distPoller");
        compile();
    }
}

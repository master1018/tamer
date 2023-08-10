public class FilteredActivityMapper implements ActivityMapper {
    public interface Filter {
        Place filter(Place place);
    }
    private final Filter filter;
    private final ActivityMapper wrapped;
    public FilteredActivityMapper(Filter filter, ActivityMapper wrapped) {
        this.filter = filter;
        this.wrapped = wrapped;
    }
    public Activity getActivity(Place place) {
        return wrapped.getActivity(filter.filter(place));
    }
}

public class DetachableSecurityCardModel extends LoadableDetachableModel {
    private EntityManagerWrapper manager;
    private Integer id;
    public DetachableSecurityCardModel(Integer id, EntityManagerWrapper manager) {
        this.id = id;
        this.manager = manager;
    }
    @Override
    protected Object load() {
        return this.manager.get(SecurityCard.class, this.id);
    }
}

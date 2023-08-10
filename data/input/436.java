public class RealWorldObjectEntity implements IEntity<Long> {
    @Transient
    private boolean initialized = false;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OBJECT_ID", nullable = false)
    private Long id;
    @Column(name = "OBJECT_NAME", length = 100, nullable = false)
    private String name;
    public RealWorldObjectEntity() {
    }
    @Override
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    @Override
    public void initialize() {
        if (!this.initialized) {
            this.initialized = true;
        }
    }
    @Override
    public void resetInitialization() {
        this.initialized = false;
    }
    @Override
    public String toString() {
        return "entity:
    }
}

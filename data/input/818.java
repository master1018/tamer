public class AbstractSpecificDAO<TYPE> extends AbstractDAO implements SpecificDAO<TYPE> {
    private ExceptionManager exceptionManager;
    private final transient Class<? extends TYPE> persistentTypeClass;
    protected AbstractSpecificDAO(Class<? extends TYPE> persistentTypeClass) {
        this.persistentTypeClass = persistentTypeClass;
    }
    public void delete(TYPE object) throws PersistenceException {
        if (!(object instanceof Persistable<?>)) {
            throw new ObjectIsNotPersistableException();
        }
        super.delete((Persistable<?>) object);
    }
    public TYPE newObject() throws TechnicalException {
        try {
            return persistentTypeClass.newInstance();
        } catch (Exception e) {
            throw getExceptionManager().convertException(e);
        }
    }
    public void save(TYPE object) throws PersistenceException {
        if (!(object instanceof Persistable<?>)) {
            throw new ObjectIsNotPersistableException();
        }
        super.save((Persistable<?>) object);
    }
    public void update(TYPE object) throws PersistenceException {
        if (!(object instanceof Persistable<?>)) {
            throw new ObjectIsNotPersistableException();
        }
        super.update((Persistable<?>) object);
    }
    public ExceptionManager getExceptionManager() {
        return exceptionManager;
    }
    public void setExceptionManager(ExceptionManager exceptionManager) {
        this.exceptionManager = exceptionManager;
    }
}

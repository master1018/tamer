public abstract class ReliableQuery<T> {
    private static final Logger logger = Logger.getLogger(ReliableQuery.class);
    private int traits = 3;
    private final Storage storage;
    public ReliableQuery(Storage storage) {
        this.storage = storage;
    }
    public List<T> execute() throws StorageError {
        EntityManager em = storage.getEM();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            List<T> result = query(em);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            if (e.getCause() != null && e.getCause().getMessage() != null && e.getCause().getMessage().contains("Communications link failure")) {
                logger.warn("Database connection lost: ", e);
                return queryAfterReconnect();
            }
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new StorageError(e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }
    private List<T> queryAfterReconnect() {
        logger.info("Trying to reconnect: " + traits);
        reconnect();
        EntityManager em = storage.getEM();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            List<T> result = query(em);
            tx.commit();
            logger.info("Database connection is up and running now");
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            --traits;
            if (traits < 0) {
                throw e;
            }
            em.close();
            return queryAfterReconnect();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }
    private void reconnect() {
        if (storage.emf.isOpen()) {
            storage.emf.close();
        }
        storage.emf = storage.createEMF();
    }
    public abstract List<T> query(EntityManager em);
}

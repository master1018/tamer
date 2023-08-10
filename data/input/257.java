public class AppEngineEntityManagerFactory {
    private static EntityManagerFactory emf;
    private final String persistenceUnitName;
    private final static Logger log = Logger.getLogger(AppEngineEntityManagerFactory.class.getName());
    public AppEngineEntityManagerFactory(String persistenceUnitName) {                
        this.persistenceUnitName = persistenceUnitName;
    }
    public EntityManagerFactory entityManagerFactory() {       
        log.warning(" accessing entityManagerFactory ");
        try {
            long begin = System.currentTimeMillis();
            if (emf == null) {
                emf = Persistence.createEntityManagerFactory(this.persistenceUnitName);
                log.warning("EntityManagerFactory ( singleton ) created for " + (System.currentTimeMillis() - begin) + "ms");
            }
            return emf;
        } catch (RuntimeException e) {
            log.log(Level.SEVERE,"Couldn't create EntityManagerFactory: " + e.getMessage(), e);
            throw e;
        } catch (Error e) {
            log.log(Level.SEVERE,"Couldn't create EntityManagerFactory: " + e.getMessage(), e);
            throw e;
        }
    }
}

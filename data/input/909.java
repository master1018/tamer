public class GrammarPool implements XMLGrammarPool {
    private static final Logger logger = Logger.getLogger(GrammarPool.class);
    private XMLGrammarPool pool;
    public GrammarPool() {
        if (logger.isInfoEnabled()) logger.info("Initializing GrammarPool.");
        pool = new XMLGrammarPoolImpl();
    }
    public GrammarPool(XMLGrammarPool pool) {
        if (logger.isInfoEnabled()) logger.info("Initializing GrammarPool using supplied pool.");
        this.pool = pool;
    }
    public Grammar[] retrieveInitialGrammarSet(String type) {
        if (logger.isDebugEnabled()) logger.debug("Retrieve initial grammarset (" + type + ").");
        Grammar[] grammars = pool.retrieveInitialGrammarSet(type);
        if (logger.isDebugEnabled()) logger.debug("Found " + grammars.length + " grammars.");
        return grammars;
    }
    public void cacheGrammars(String type, Grammar[] grammar) {
        if (logger.isDebugEnabled()) logger.debug("Cache " + grammar.length + " grammars (" + type + ").");
        pool.cacheGrammars(type, grammar);
    }
    public void unlockPool() {
        if (logger.isDebugEnabled()) logger.debug("Unlock grammarpool.");
        pool.unlockPool();
    }
    public Grammar retrieveGrammar(XMLGrammarDescription xgd) {
        if (xgd == null) {
            if (logger.isDebugEnabled()) logger.debug("XMLGrammarDescription is null");
            return null;
        }
        if (xgd.getNamespace() != null) {
            if (logger.isDebugEnabled()) logger.debug("Retrieve grammar for namespace '" + xgd.getNamespace() + "'.");
        }
        if (xgd.getPublicId() != null) {
            if (logger.isDebugEnabled()) logger.debug("Retrieve grammar for publicId '" + xgd.getPublicId() + "'.");
        }
        return pool.retrieveGrammar(xgd);
    }
    public void lockPool() {
        if (logger.isDebugEnabled()) logger.debug("Lock grammarpool.");
        pool.lockPool();
    }
    public void clear() {
        if (logger.isDebugEnabled()) logger.debug("Clear grammarpool.");
        pool.clear();
    }
    public void clearDTDs() {
        Grammar dtds[] = retrieveInitialGrammarSet(Namespaces.DTD_NS);
        if (dtds.length > 0) {
            if (logger.isDebugEnabled()) logger.debug("Removing " + dtds.length + " DTDs.");
            Grammar schemas[] = retrieveInitialGrammarSet(Namespaces.SCHEMA_NS);
            clear();
            cacheGrammars(Namespaces.SCHEMA_NS, schemas);
        } else {
        }
    }
}

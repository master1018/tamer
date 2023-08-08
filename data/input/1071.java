public class GeodatabaseOjbOracle extends GeodatabaseOjb implements Geodatabase {
    static Logger logger = Logger.getLogger(GeodatabaseOjbOracle.class.getName());
    public GeodatabaseOjbOracle() {
        super();
        updateConnection();
        initGeomMetadata();
    }
    public GeodatabaseOjbOracle(GeodatabaseOjb ojb) {
        _conn = ojb.getConnection();
        _odmg = ojb.getODMGImplementation();
        _db = ojb.getODMGDatabase();
        _tx = ojb.getODMGTransaction();
        _metadataList = ojb.getMetadata();
        updateConnection();
        initGeomMetadata();
    }
    private void updateConnection() {
        try {
            GeomGeOxygene2Oracle.CONNECTION = _conn;
            GeomGeOxygene2Oracle.GF = OraSpatialManager.getGeometryFactory();
            GeomGeOxygene2Oracle.SRM = OraSpatialManager.getSpatialReferenceManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void initGeomMetadata() {
        OracleSpatialQuery.initGeomMetadata(_metadataList, _conn);
    }
    @SuppressWarnings("unchecked")
    public <T extends FT_Feature> FT_FeatureCollection<T> loadAllFeatures(Class<T> featureClass, GM_Object geom) {
        FT_FeatureCollection<T> result = new FT_FeatureCollection<T>();
        if ((FT_Feature.class).isAssignableFrom(featureClass)) {
            List<?> idList = OracleSpatialQuery.loadAllFeatures(this, featureClass, geom);
            if (idList.size() > 0) {
                String query = createInQuery(idList, featureClass.getName());
                OQLQuery oqlQuery = _odmg.newOQLQuery();
                try {
                    oqlQuery.create(query);
                    DList list = (DList) oqlQuery.execute();
                    Iterator<T> iter = list.iterator();
                    while (iter.hasNext()) {
                        T feature = iter.next();
                        result.add(feature);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            logger.warn("La classe passee en parametre n'est pas une sous-classe de FT_Feature");
        }
        return result;
    }
    public <T> T loadAllFeatures(Class<?> featureClass, Class<T> featureListClass, GM_Object geom) {
        T result = null;
        try {
            result = featureListClass.newInstance();
        } catch (Exception e) {
            logger.error("Impossible de créer une nouvelle instance de la classe " + featureListClass.getName());
            e.printStackTrace();
            return null;
        }
        if ((FT_Feature.class).isAssignableFrom(featureClass)) {
            List<?> idList = OracleSpatialQuery.loadAllFeatures(this, featureClass, geom);
            if (idList.size() > 0) {
                String query = createInQuery(idList, featureClass.getName());
                OQLQuery oqlQuery = _odmg.newOQLQuery();
                try {
                    oqlQuery.create(query);
                    DList list = (DList) oqlQuery.execute();
                    Iterator<?> iter = list.iterator();
                    while (iter.hasNext()) {
                        FT_Feature feature = (FT_Feature) iter.next();
                        result.getClass().getMethod("add", new Class[] { FT_Feature.class }).invoke(result, new Object[] { feature });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            logger.warn("La classe passee en parametre n'est pas une sous-classe de FT_Feature");
        }
        return result;
    }
    @SuppressWarnings("unchecked")
    public <T extends FT_Feature> FT_FeatureCollection<T> loadAllFeatures(Class<T> featureClass, GM_Object geom, double dist) {
        FT_FeatureCollection<T> result = new FT_FeatureCollection<T>();
        if ((FT_Feature.class).isAssignableFrom(featureClass)) {
            List<?> idList = OracleSpatialQuery.loadAllFeatures(this, featureClass, geom, dist);
            if (idList.size() > 0) {
                String query = createInQuery(idList, featureClass.getName());
                OQLQuery oqlQuery = _odmg.newOQLQuery();
                try {
                    oqlQuery.create(query);
                    DList list = (DList) oqlQuery.execute();
                    Iterator<T> iter = list.iterator();
                    while (iter.hasNext()) {
                        T feature = iter.next();
                        result.add(feature);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            logger.warn("La classe passee en parametre n'est pas une sous-classe de FT_Feature");
        }
        return result;
    }
    public <T> T loadAllFeatures(Class<?> featureClass, Class<T> featureListClass, GM_Object geom, double dist) {
        T result = null;
        try {
            result = featureListClass.newInstance();
        } catch (Exception e) {
            logger.error("Impossible de créer une nouvelle instance de la classe " + featureListClass.getName());
            e.printStackTrace();
            return null;
        }
        if ((FT_Feature.class).isAssignableFrom(featureClass)) {
            List<?> idList = OracleSpatialQuery.loadAllFeatures(this, featureClass, geom, dist);
            if (idList.size() > 0) {
                String query = createInQuery(idList, featureClass.getName());
                OQLQuery oqlQuery = _odmg.newOQLQuery();
                try {
                    oqlQuery.create(query);
                    DList list = (DList) oqlQuery.execute();
                    Iterator<?> iter = list.iterator();
                    while (iter.hasNext()) {
                        FT_Feature feature = (FT_Feature) iter.next();
                        result.getClass().getMethod("add", new Class[] { FT_Feature.class }).invoke(result, new Object[] { feature });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            logger.warn("La classe passee en parametre n'est pas une sous-classe de FT_Feature");
        }
        return result;
    }
    private String createInQuery(List<?> idList, String className) {
        StringBuffer strBuff = new StringBuffer("select x from " + className + " where id in (");
        Iterator<?> i = idList.iterator();
        while (i.hasNext()) {
            int k = ((BigDecimal) i.next()).intValue();
            strBuff.append(k);
            strBuff.append(",");
        }
        String result = strBuff.toString();
        result = result.substring(0, result.length() - 1);
        result = result + ")";
        return result;
    }
    public void mbr(Class<?> clazz) {
        OracleSpatialQuery.mbr(this, clazz);
    }
    public void spatialIndex(Class<?> clazz) {
        OracleSpatialQuery.spatialIndex(this, clazz);
    }
    public int countObjects(Class<?> theClass) {
        String tableName = getMetadata(theClass).getTableName();
        String query = "select count(*) from " + tableName;
        BigDecimal nn = null;
        try {
            Connection conn = getConnection();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) nn = (BigDecimal) rs.getObject(1);
            stm.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (nn != null) return nn.intValue();
        return 0;
    }
    public int maxId(Class<?> theClass) {
        String idColumnName = getMetadata(theClass).getIdColumnName();
        String tableName = getMetadata(theClass).getTableName();
        String query = "select max(" + idColumnName + ") from " + tableName;
        BigDecimal nn = null;
        try {
            Connection conn = getConnection();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) nn = (BigDecimal) rs.getObject(1);
            stm.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (nn != null) return nn.intValue();
        return 1;
    }
    public int minId(Class<?> theClass) {
        String idColumnName = getMetadata(theClass).getIdColumnName();
        String tableName = getMetadata(theClass).getTableName();
        String query = "select min(" + idColumnName + ") from " + tableName;
        BigDecimal nn = null;
        try {
            Connection conn = getConnection();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) nn = (BigDecimal) rs.getObject(1);
            stm.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (nn != null) return nn.intValue();
        return 1;
    }
    public int getDBMS() {
        return Geodatabase.ORACLE;
    }
    public void refreshRepository(File newRepository) throws Exception {
        MetadataManager mm = MetadataManager.getInstance();
        DescriptorRepository rd = mm.readDescriptorRepository(newRepository.getPath());
        mm.setDescriptor(rd, true);
        begin();
        PersistenceBrokerHandle pbh = (PersistenceBrokerHandle) ((HasBroker) _tx).getBroker();
        PoolablePersistenceBroker ppb = (PoolablePersistenceBroker) pbh.getDelegate();
        GeOxygenePersistenceBrokerImpl pbi = (GeOxygenePersistenceBrokerImpl) ppb.getDelegate();
        pbi.refresh();
        commit();
        initMetadata();
        initGeomMetadata();
    }
}

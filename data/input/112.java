public class TableSequence implements PrimaryKeyStrategy {
    protected String sequenceTableName = "DUAL";
    protected String sequenceColumnSuffix = "_UID_SEQ.NEXTVAL";
    public void setSequenceTableName(String newTableName) {
        if (newTableName == null || newTableName.length() == 0) {
            return;
        } else {
            this.sequenceTableName = newTableName;
        }
    }
    public void setSequenceColumnSuffix(String newSuffix) {
        if (newSuffix == null || newSuffix.length() == 0) {
            return;
        } else {
            this.sequenceColumnSuffix = newSuffix;
        }
    }
    public String getSequenceTableName() {
        return sequenceTableName;
    }
    public String getSequenceColumnSuffix() {
        return sequenceColumnSuffix;
    }
    public Object nextPrimaryKey(final RDBBroker broker, final RDBPersistence pObj) throws SQLException, QueryException {
        String sequenceColumnName = pObj.getTableName(pObj) + sequenceColumnSuffix;
        SQLSelect sql = new SQLSelect(getSequenceTableName());
        sql.addColumnList(sequenceColumnName);
        int nextPk = broker.getConnection().nextPrimaryKey(sql);
        return new Integer(nextPk);
    }
}

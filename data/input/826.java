public abstract class PersonBean implements EntityBean {
    private EntityContext ctx;
    public Integer ejbCreate(int newPersonID, String newMainName) throws CreateException {
        setPersonID(new Integer(newPersonID));
        setMainName(newMainName);
        return null;
    }
    public void ejbPostCreate(int newPersonID, String newMainName) {
    }
    public Integer ejbCreate(int newPersonID, String newMainName, String newGivenName) throws CreateException {
        setPersonID(new Integer(newPersonID));
        setMainName(newMainName);
        setGivenName(newGivenName);
        return null;
    }
    public void ejbPostCreate(int newPersonID, String newMainName, String newGivenName) {
    }
    public Integer ejbCreate(int newPersonID, String newMainName, String newGivenName, String newOtherNames) throws CreateException {
        setPersonID(new Integer(newPersonID));
        setMainName(newMainName);
        setGivenName(newGivenName);
        setOtherNames(newOtherNames);
        return null;
    }
    public void ejbPostCreate(int newPersonID, String newMainName, String newGivenName, String newOtherNames) {
    }
    public Integer ejbCreate(int newPersonID, String newMainName, String newGivenName, String newOtherNames, String newSuffix) throws CreateException {
        setPersonID(new Integer(newPersonID));
        setMainName(newMainName);
        setGivenName(newGivenName);
        setOtherNames(newOtherNames);
        setSuffix(newSuffix);
        return null;
    }
    public void ejbPostCreate(int newPersonID, String newMainName, String newGivenName, String newOtherNames, String newSuffix) {
    }
    public abstract Integer getPersonID();
    public abstract void setPersonID(Integer newPersonID);
    public abstract String getPrefix();
    public abstract void setPrefix(String newPrefix);
    public abstract String getGivenName();
    public abstract void setGivenName(String newGivenName);
    public abstract String getMainName();
    public abstract void setMainName(String newMainName);
    public abstract String getOtherNames();
    public abstract void setOtherNames(String newOtherNames);
    public abstract String getSuffix();
    public abstract void setSuffix(String newSuffix);
    public abstract int getGenderCode();
    public abstract void setGenderCode(int newGenderCode);
    public abstract Set getMediaProductionRelationships();
    public abstract void setMediaProductionRelationships(Set newMediaProductionRelationships);
    public PersonBean() {
    }
    public void ejbActivate() throws javax.ejb.EJBException {
    }
    public void ejbLoad() throws javax.ejb.EJBException {
    }
    public void ejbPassivate() throws javax.ejb.EJBException {
    }
    public void ejbRemove() throws javax.ejb.RemoveException, javax.ejb.EJBException {
    }
    public void ejbStore() throws javax.ejb.EJBException {
    }
    public void setEntityContext(javax.ejb.EntityContext entityContext) throws javax.ejb.EJBException {
        ctx = entityContext;
    }
    public void unsetEntityContext() throws javax.ejb.EJBException, java.rmi.RemoteException {
        ctx = null;
    }
}

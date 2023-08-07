public abstract class GuarantorEntityBean implements EntityBean {
    public EntityContext mContext;
    public void setValueObject(GuarantorEntityData pGuarantorEntity) throws InvalidValueException {
        if (pGuarantorEntity == null) {
            throw new InvalidValueException("object.undefined", "GuarantorEntity");
        }
        if (pGuarantorEntity.getPerson_ID().intValue() <= 0) {
            throw new InvalidValueException("id.invalid", new String[] { "GuarantorEntity", "Id" });
        }
        setPerson_ID(pGuarantorEntity.getPerson_ID());
    }
    public GuarantorEntityData getValueObject() {
        GuarantorEntityData lData = new GuarantorEntityData();
        lData.setPerson_ID(getPerson_ID());
        return lData;
    }
    public String toString() {
        return "GuarantorEntityBean [ " + getValueObject() + " ]";
    }
    private Integer generateUniqueId() throws ServiceUnavailableException {
        Integer lUniqueId = new Integer(-1);
        try {
            Context lContext = new InitialContext();
            String lSequenceName = (java.lang.String) lContext.lookup("java:comp/env/SequenceName");
            SequenceGeneratorHome lHome = (SequenceGeneratorHome) PortableRemoteObject.narrow(lContext.lookup("java:comp/env/ejb/com/medcentrex/bridge/SequenceGenerator"), SequenceGeneratorHome.class);
            SequenceGenerator lBean = (SequenceGenerator) lHome.create();
            lUniqueId = lBean.getNextNumber(lSequenceName);
            lBean.remove();
        } catch (NamingException ne) {
            throw new ServiceUnavailableException("Naming lookup failure: " + ne.getMessage());
        } catch (CreateException ce) {
            throw new ServiceUnavailableException("Failure while creating a generator session bean: " + ce.getMessage());
        } catch (RemoveException re) {
        } catch (RemoteException rte) {
            throw new ServiceUnavailableException("Remote exception occured while accessing generator session bean: " + rte.getMessage());
        }
        return lUniqueId;
    }
    public abstract Integer getPerson_ID();
    public abstract void setPerson_ID(java.lang.Integer pPerson_ID);
    public GuarantorEntityPK ejbCreate(GuarantorEntityData pGuarantorEntity) throws InvalidValueException, EJBException, CreateException {
        GuarantorEntityData lData = (GuarantorEntityData) pGuarantorEntity.clone();
        setValueObject(lData);
        return null;
    }
    public void ejbPostCreate(GuarantorEntityData pGuarantorEntity) {
    }
    public void setEntityContext(EntityContext lContext) {
        mContext = lContext;
    }
    public void unsetEntityContext() {
        mContext = null;
    }
    public void ejbActivate() {
    }
    public void ejbPassivate() {
    }
    public void ejbLoad() {
    }
    public void ejbStore() {
    }
    public void ejbRemove() throws RemoveException {
    }
}

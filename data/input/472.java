@XmlSeeAlso({ HIView.class, HIInscription.class })
public class HIObjectContent extends HIBase {
    @ManyToOne(cascade = CascadeType.PERSIST, targetEntity = HIObject.class)
    HIObject object;
    @XmlTransient
    public HIObject getObject() {
        return object;
    }
    public void setObject(HIObject object) {
        this.object = object;
    }
}

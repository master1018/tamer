public class ArtikelReal extends WikiArticle implements Serializable {
    private static final long serialVersionUID = -3272635532156248306L;
    protected static final Log LOG = LogFactory.getLog(ArtikelReal.class);
    protected static final boolean DEBUG = LOG.isDebugEnabled();
    protected static final boolean TRACE = LOG.isTraceEnabled();
    static final String FIND_ARTIKEL = "findArtikel";
    static final String FIND_ARTIKEL_ROLLE_REAL = "findArtikelRolleReal";
    static final String FIND_ARTIKEL_BY_NAME_ROLLE = "findArtikelByNameRolle";
    static final String FIND_ARTIKEL_BY_KATEGORIE_ROLLE_REAL = "findArtikelByKategorieRolleReal";
    static final String FIND_ARTIKEL_LIKE_BESCHREIBUNG = "findArtikelLikeBeschreibung";
    static final String FIND_ARTIKELREAL_BY_USER = "findArtikelRealByUser";
    static final String FIND_ARTIKELREAL_BY_ROLE_NULL = "findArtikelRealByRoleNull";
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Artikel_ID")
    private int Artikel_ID;
    @Column(name = "Artikel_Name", nullable = false)
    private String name;
    @Column(name = "Artikel_Text_Name")
    private String Artikel_Text;
    @Column(name = "Datum")
    private String datum;
    @Lob
    @Column(name = "Artikel_Bild")
    private String Artikel_Bild;
    @Version
    @Column(name = "Artikel_Version")
    private int Artikel_Version = ERSTE_VERSION;
    @ManyToOne(optional = false)
    @JoinColumn(name = "User_FK")
    private User user_fk;
    @OneToOne
    @JoinColumn(name = "Rolle_FK")
    private ArtikelRolle rolle_fk;
    @ManyToOne
    @JoinColumn(name = "Kategorie_FK")
    private Kategorie kategorie_fk;
    @OneToOne
    @JoinColumn(name = "Artikel_Status_FK")
    private ArtikelStatus status;
    @Column(name = "Artikel_Text_Inhalt")
    private String data;
    public ArtikelReal() {
        super();
    }
    public ArtikelReal(String name, String Artikel_Text, int Version, User user_fk, ArtikelRolle rolle_fk, Kategorie kategorie_fk, ArtikelStatus status) {
        this.name = name;
        this.Artikel_Text = Artikel_Text;
        this.Artikel_Version = Version;
        this.user_fk = user_fk;
        this.rolle_fk = rolle_fk;
        this.kategorie_fk = kategorie_fk;
        this.status = status;
    }
    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }
    public ArtikelStatus getStatus() {
        return status;
    }
    public void setStatus(ArtikelStatus status) {
        this.status = status;
    }
    public int getArtikel_ID() {
        return Artikel_ID;
    }
    public void setArtikel_ID(int artikel_ID) {
        Artikel_ID = artikel_ID;
    }
    public String getArtikel_Bild() {
        return Artikel_Bild;
    }
    public void setArtikel_Bild(String artikel_Bild) {
        Artikel_Bild = artikel_Bild;
    }
    public int getArtikel_Version() {
        return Artikel_Version;
    }
    public void setArtikel_Version(int artikel_Version) {
        Artikel_Version = artikel_Version;
    }
    public User getUser_fk() {
        return user_fk;
    }
    public void setUser_fk(User user_fk) {
        this.user_fk = user_fk;
    }
    public ArtikelRolle getRolle_fk() {
        return rolle_fk;
    }
    public void setRolle_fk(ArtikelRolle rolle_fk) {
        this.rolle_fk = rolle_fk;
    }
    public Kategorie getKategorie_fk() {
        return kategorie_fk;
    }
    public void setKategorie_fk(Kategorie kategorie_fk) {
        this.kategorie_fk = kategorie_fk;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((Artikel_Bild == null) ? 0 : Artikel_Bild.hashCode());
        result = prime * result + Artikel_ID;
        result = prime * result + ((Artikel_Text == null) ? 0 : Artikel_Text.hashCode());
        result = prime * result + Artikel_Version;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + ((datum == null) ? 0 : datum.hashCode());
        result = prime * result + ((kategorie_fk == null) ? 0 : kategorie_fk.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((rolle_fk == null) ? 0 : rolle_fk.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((user_fk == null) ? 0 : user_fk.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ArtikelReal other = (ArtikelReal) obj;
        if (Artikel_Bild == null) {
            if (other.Artikel_Bild != null) return false;
        } else if (!Artikel_Bild.equals(other.Artikel_Bild)) return false;
        if (Artikel_ID != other.Artikel_ID) return false;
        if (Artikel_Text == null) {
            if (other.Artikel_Text != null) return false;
        } else if (!Artikel_Text.equals(other.Artikel_Text)) return false;
        if (Artikel_Version != other.Artikel_Version) return false;
        if (data == null) {
            if (other.data != null) return false;
        } else if (!data.equals(other.data)) return false;
        if (datum == null) {
            if (other.datum != null) return false;
        } else if (!datum.equals(other.datum)) return false;
        if (kategorie_fk == null) {
            if (other.kategorie_fk != null) return false;
        } else if (!kategorie_fk.equals(other.kategorie_fk)) return false;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        if (rolle_fk == null) {
            if (other.rolle_fk != null) return false;
        } else if (!rolle_fk.equals(other.rolle_fk)) return false;
        if (status == null) {
            if (other.status != null) return false;
        } else if (!status.equals(other.status)) return false;
        if (user_fk == null) {
            if (other.user_fk != null) return false;
        } else if (!user_fk.equals(other.user_fk)) return false;
        return true;
    }
    @Override
    public String toString() {
        return "id=" + Artikel_ID + ", version=" + Artikel_Version + ", user=" + user_fk + ", rolle=" + rolle_fk + ", kategorie= " + kategorie_fk + ", status=" + status;
    }
    @Override
    public String getTitle() {
        return getName();
    }
    ;
    @Override
    public void setTitle(String newTitle) {
        setName(newTitle);
    }
    ;
    public String getName() {
        return name;
    }
    public void setName(String artikel_Name) {
        name = artikel_Name;
    }
    @Override
    public String getText() {
        return Artikel_Text;
    }
    ;
    @Override
    public void setText(String newText) {
        Artikel_Text = newText;
    }
    ;
    public String getArtikel_Text() {
        return Artikel_Text;
    }
    public void setArtikel_Text(String artikel_Text) {
        Artikel_Text = artikel_Text;
    }
    @Override
    public String getTimeStamp() {
        return getDatum();
    }
    ;
    @Override
    public void setTimeStamp(String timeStamp) {
        setDatum(timeStamp);
    }
    ;
    public String getDatum() {
        return datum;
    }
    public void setDatum(String datum) {
        this.datum = datum;
    }
}

public class Topico {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String titulo;
    private String descricao;
    @ManyToOne(cascade = CascadeType.ALL)
    private Assunto assunto;
    @ManyToOne(cascade = CascadeType.ALL)
    private Usuario autor;
    @OneToMany(cascade = CascadeType.ALL)
    private List<Mensagem> mensagens = new ArrayList<Mensagem>();
    public Topico() {
    }
    public Topico(String titulo, String descricao, Assunto assunto, Usuario autor) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.assunto = assunto;
        this.autor = autor;
    }
    public Assunto getAssunto() {
        return assunto;
    }
    public void setAssunto(Assunto assunto) {
        this.assunto = assunto;
    }
    public Usuario getAutor() {
        return autor;
    }
    public void setAutor(Usuario autor) {
        this.autor = autor;
    }
    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public List<Mensagem> getMensagens() {
        return mensagens;
    }
    public void setMensagens(List<Mensagem> mensagens) {
        this.mensagens = mensagens;
    }
}

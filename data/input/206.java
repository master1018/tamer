public class Condition {
    public String signo;
    public Campo identificador;
    public Campo atributo;
    public Campo valor;
    private LinkedList<Condition> subcondiciones;
    public Condition(String signo, Campo identificador, Campo atributo, Campo valor) {
        this.signo = signo;
        this.identificador = identificador;
        this.atributo = atributo;
        this.valor = valor;
        this.subcondiciones = new LinkedList<Condition>();
    }
    public LinkedList<Condition> DarListaSubcondiciones() {
        return subcondiciones;
    }
}

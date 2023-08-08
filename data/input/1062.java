public class PartidaPoker {
    ArrayList<JugadorPoker> jugadores;
    BarajaFrancesa baraja;
    Bote bote;
    public PartidaPoker(ArrayList<JugadorPoker> j) {
        jugadores.addAll(j);
        baraja = new BarajaFrancesa();
        bote = new Bote();
    }
    public void comenzar() {
        Iterator it = jugadores.iterator();
        while (it.hasNext()) {
            JugadorPoker j = (JugadorPoker) it.next();
            j.asignarCartas(baraja.sacarCarta(), baraja.sacarCarta());
        }
        it = jugadores.iterator();
        while (it.hasNext()) {
            JugadorPoker j = (JugadorPoker) it.next();
        }
    }
}

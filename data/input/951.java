public class ColetorDeDados {
    public String colectAllText() {
        AssuntoBusiness business = new AssuntoBusiness();
        List<Assunto> assuntos = business.getTodosOsAssuntos();
        String texto = "";
        for (Assunto a : assuntos) {
            texto += " " + a.getTitulo();
            texto += " " + a.getDescricao();
            for (Topico t : a.getTopicos()) {
                texto += " " + t.getTitulo();
                texto += " " + t.getDescricao();
                for (Mensagem m : t.getMensagens()) {
                    texto += " " + m.getTexto();
                }
            }
        }
        return texto;
    }
}

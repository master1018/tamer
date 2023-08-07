public class ActionBean {
    public Response add(int idService, String node) {
        ActionDAO dao = new ActionDAO();
        try {
            dao.addOrUpdate(0, idService, node);
        } catch (ApplicationException ae) {
            return new Response(TypeResponse.ERROR, "Erreur Application : " + ae.getMessage());
        }
        return new Response(TypeResponse.ALERT, "Ajout R�ussi");
    }
    public Response update(int idAction, int idService, String node) {
        ActionDAO dao = new ActionDAO();
        try {
            dao.addOrUpdate(idAction, idService, node);
        } catch (ApplicationException ae) {
            return new Response(TypeResponse.ERROR, "Erreur Application : " + ae.getMessage());
        }
        return new Response(TypeResponse.ALERT, "Modification R�ussie");
    }
    public Response supprimer(int idAction) {
        ActionDAO dao = new ActionDAO();
        try {
            dao.remove(idAction);
        } catch (ApplicationException ae) {
            return new Response(TypeResponse.ERROR, "Erreur Application : " + ae.getMessage());
        }
        return new Response(TypeResponse.ALERT, "Suppression R�ussie");
    }
    public Response getAll() {
        ActionDAO dao = new ActionDAO();
        try {
            return new Response(TypeResponse.NOTHING, dao.getAll());
        } catch (ApplicationException ae) {
            return new Response(TypeResponse.ERROR, "Erreur Application : " + ae.getMessage(), null);
        }
    }
    public Response getAllByIdInteraction(int idInteraction) {
        ActionDAO dao = new ActionDAO();
        try {
            TreeSet<Action> actions = new TreeSet<Action>(new Comparator<Action>() {
                public int compare(Action o1, Action o2) {
                    if (o1.getIdAction() < o2.getIdAction()) {
                        return -1;
                    } else if (o1.getIdAction() > o2.getIdAction()) {
                        return 1;
                    }
                    return 0;
                }
            });
            actions.addAll(dao.getAllByIdInteraction(idInteraction));
            return new Response(TypeResponse.NOTHING, actions);
        } catch (ApplicationException ae) {
            return new Response(TypeResponse.ERROR, "Erreur Application : " + ae.getMessage(), null);
        }
    }
    public Response getById(int idAction) {
        ActionDAO dao = new ActionDAO();
        try {
            return new Response(TypeResponse.NOTHING, dao.getById(idAction));
        } catch (ApplicationException ae) {
            return new Response(TypeResponse.ERROR, "Erreur Application : " + ae.getMessage(), null);
        }
    }
}

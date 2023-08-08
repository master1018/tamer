public class FavoritesDAO {
    public FavoritesDAO() {
        checkTable();
    }
    public boolean isFavourite(int teamId) {
        String query = "select * from TEAMANALYZER_FAVORITES where TEAMID=" + teamId;
        ResultSet rs = Commons.getModel().getAdapter().executeQuery(query);
        try {
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
        }
        return false;
    }
    public List<Team> getTeams() {
        List<Team> list = new ArrayList<Team>();
        String query = "select * from TEAMANALYZER_FAVORITES";
        ResultSet rs = Commons.getModel().getAdapter().executeQuery(query);
        try {
            while (rs.next()) {
                Team team = new Team();
                team.setTeamId(rs.getInt(1));
                team.setName(rs.getString(2));
                list.add(team);
            }
        } catch (SQLException e) {
            return new ArrayList<Team>();
        }
        return list;
    }
    public void addTeam(Team team) {
        Commons.getModel().getAdapter().executeUpdate("insert into TEAMANALYZER_FAVORITES (TEAMID, NAME) values (" + team.getTeamId() + ", '" + team.getName() + "')");
    }
    public void removeTeam(int teamId) {
        String query = "delete from TEAMANALYZER_FAVORITES where TEAMID=" + teamId;
        Commons.getModel().getAdapter().executeUpdate(query);
    }
    private void checkTable() {
        try {
            ResultSet rs = Commons.getModel().getAdapter().executeQuery("select * from TEAMANALYZER_FAVORITES");
            rs.next();
        } catch (Exception e) {
            Commons.getModel().getAdapter().executeUpdate("CREATE TABLE TEAMANALYZER_FAVORITES(TEAMID integer,NAME varchar(20))");
        }
    }
}

public class StandardGroupOrganizer implements GroupOrganizer {
    public String getName() {
        return "-- None --";
    }
    public List<Pair<String, List<Group>>> organize(List<String> subFolders, List<Group> groups) {
        List<Pair<String, List<Group>>> result = new ArrayList<Pair<String, List<Group>>>();
        if (groups.size() == 1 && subFolders.size() > 0) {
            result.add(new Pair<String, List<Group>>("", groups));
        } else {
            for (Group g : groups) {
                List<Group> children = new ArrayList<Group>();
                children.add(g);
                Pair<String, List<Group>> p = new Pair<String, List<Group>>();
                p.setLeft(g.getName());
                p.setRight(children);
                result.add(p);
            }
        }
        return result;
    }
}

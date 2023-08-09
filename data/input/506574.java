public class TaskHelper {
    public static String checkSinglePath(String attribute, Path path) {
        String[] paths = path.list();
        if (paths.length != 1) {
            throw new BuildException(String.format("Path value for '%1$s' is not valid.", attribute));
        }
        return paths[0];
    }
}

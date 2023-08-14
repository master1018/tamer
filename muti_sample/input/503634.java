public class ExtractStringContribution extends RefactoringContribution {
    @SuppressWarnings("unchecked")
    @Override
    public RefactoringDescriptor createDescriptor(
            String id,
            String project,
            String description,
            String comment,
            Map arguments,
            int flags)
                throws IllegalArgumentException {
        return new ExtractStringDescriptor(project, description, comment, arguments);
    }
    @SuppressWarnings("unchecked")
    @Override
    public Map retrieveArgumentMap(RefactoringDescriptor descriptor) {
        if (descriptor instanceof ExtractStringDescriptor) {
            return ((ExtractStringDescriptor) descriptor).getArguments();
        }
        return super.retrieveArgumentMap(descriptor);
    }
}

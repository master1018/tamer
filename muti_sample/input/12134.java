public class TreePath implements Iterable<Tree> {
    public static TreePath getPath(CompilationUnitTree unit, Tree target) {
        return getPath(new TreePath(unit), target);
    }
    public static TreePath getPath(TreePath path, Tree target) {
        path.getClass();
        target.getClass();
        class Result extends Error {
            static final long serialVersionUID = -5942088234594905625L;
            TreePath path;
            Result(TreePath path) {
                this.path = path;
            }
        }
        class PathFinder extends TreePathScanner<TreePath,Tree> {
            public TreePath scan(Tree tree, Tree target) {
                if (tree == target)
                    throw new Result(new TreePath(getCurrentPath(), target));
                return super.scan(tree, target);
            }
        }
        try {
            new PathFinder().scan(path, target);
        } catch (Result result) {
            return result.path;
        }
        return null;
    }
    public TreePath(CompilationUnitTree t) {
        this(null, t);
    }
    public TreePath(TreePath p, Tree t) {
        if (t.getKind() == Tree.Kind.COMPILATION_UNIT) {
            compilationUnit = (CompilationUnitTree) t;
            parent = null;
        }
        else {
            compilationUnit = p.compilationUnit;
            parent = p;
        }
        leaf = t;
    }
    public CompilationUnitTree getCompilationUnit() {
        return compilationUnit;
    }
    public Tree getLeaf() {
        return leaf;
    }
    public TreePath getParentPath() {
        return parent;
    }
    public Iterator<Tree> iterator() {
        return new Iterator<Tree>() {
            public boolean hasNext() {
                return next != null;
            }
            public Tree next() {
                Tree t = next.leaf;
                next = next.parent;
                return t;
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
            private TreePath next = TreePath.this;
        };
    }
    private CompilationUnitTree compilationUnit;
    private Tree leaf;
    private TreePath parent;
}

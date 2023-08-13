class FileTreeWalker {
    private final boolean followLinks;
    private final LinkOption[] linkOptions;
    private final FileVisitor<? super Path> visitor;
    private final int maxDepth;
    FileTreeWalker(Set<FileVisitOption> options,
                   FileVisitor<? super Path> visitor,
                   int maxDepth)
    {
        boolean fl = false;
        for (FileVisitOption option: options) {
            switch (option) {
                case FOLLOW_LINKS : fl = true; break;
                default:
                    throw new AssertionError("Should not get here");
            }
        }
        this.followLinks = fl;
        this.linkOptions = (fl) ? new LinkOption[0] :
            new LinkOption[] { LinkOption.NOFOLLOW_LINKS };
        this.visitor = visitor;
        this.maxDepth = maxDepth;
    }
    void walk(Path start) throws IOException {
        FileVisitResult result = walk(start,
                                      0,
                                      new ArrayList<AncestorDirectory>());
        Objects.requireNonNull(result, "FileVisitor returned null");
    }
    private FileVisitResult walk(Path file,
                                 int depth,
                                 List<AncestorDirectory> ancestors)
        throws IOException
    {
        BasicFileAttributes attrs = null;
        if ((depth > 0) &&
            (file instanceof BasicFileAttributesHolder) &&
            (System.getSecurityManager() == null))
        {
            BasicFileAttributes cached = ((BasicFileAttributesHolder)file).get();
            if (!followLinks || !cached.isSymbolicLink())
                attrs = cached;
        }
        IOException exc = null;
        if (attrs == null) {
            try {
                try {
                    attrs = Files.readAttributes(file, BasicFileAttributes.class, linkOptions);
                } catch (IOException x1) {
                    if (followLinks) {
                        try {
                            attrs = Files.readAttributes(file,
                                                         BasicFileAttributes.class,
                                                         LinkOption.NOFOLLOW_LINKS);
                        } catch (IOException x2) {
                            exc = x2;
                        }
                    } else {
                        exc = x1;
                    }
                }
            } catch (SecurityException x) {
                if (depth == 0)
                    throw x;
                return FileVisitResult.CONTINUE;
            }
        }
        if (exc != null) {
            return visitor.visitFileFailed(file, exc);
        }
        if (depth >= maxDepth || !attrs.isDirectory()) {
            return visitor.visitFile(file, attrs);
        }
        if (followLinks) {
            Object key = attrs.fileKey();
            for (AncestorDirectory ancestor: ancestors) {
                Object ancestorKey = ancestor.fileKey();
                if (key != null && ancestorKey != null) {
                    if (key.equals(ancestorKey)) {
                        return visitor.visitFileFailed(file,
                            new FileSystemLoopException(file.toString()));
                    }
                } else {
                    boolean isSameFile = false;
                    try {
                        isSameFile = Files.isSameFile(file, ancestor.file());
                    } catch (IOException x) {
                    } catch (SecurityException x) {
                    }
                    if (isSameFile) {
                        return visitor.visitFileFailed(file,
                            new FileSystemLoopException(file.toString()));
                    }
                }
            }
            ancestors.add(new AncestorDirectory(file, key));
        }
        try {
            DirectoryStream<Path> stream = null;
            FileVisitResult result;
            try {
                stream = Files.newDirectoryStream(file);
            } catch (IOException x) {
                return visitor.visitFileFailed(file, x);
            } catch (SecurityException x) {
                return FileVisitResult.CONTINUE;
            }
            IOException ioe = null;
            try {
                result = visitor.preVisitDirectory(file, attrs);
                if (result != FileVisitResult.CONTINUE) {
                    return result;
                }
                try {
                    for (Path entry: stream) {
                        result = walk(entry, depth+1, ancestors);
                        if (result == null || result == FileVisitResult.TERMINATE)
                            return result;
                        if (result == FileVisitResult.SKIP_SIBLINGS)
                            break;
                    }
                } catch (DirectoryIteratorException e) {
                    ioe = e.getCause();
                }
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    if (ioe == null)
                        ioe = e;
                }
            }
            return visitor.postVisitDirectory(file, ioe);
        } finally {
            if (followLinks) {
                ancestors.remove(ancestors.size()-1);
            }
        }
    }
    private static class AncestorDirectory {
        private final Path dir;
        private final Object key;
        AncestorDirectory(Path dir, Object key) {
            this.dir = dir;
            this.key = key;
        }
        Path file() {
            return dir;
        }
        Object fileKey() {
            return key;
        }
    }
}

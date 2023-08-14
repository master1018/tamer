public class UnpackerImpl extends TLGlobals implements Pack200.Unpacker {
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        props.addListener(listener);
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        props.removeListener(listener);
    }
    public UnpackerImpl() {}
    @SuppressWarnings("unchecked")
    public SortedMap properties() {
        return props;
    }
    Object _nunp;
    public String toString() {
        return Utils.getVersionString();
    }
    public void unpack(InputStream in, JarOutputStream out) throws IOException {
        if (in == null) {
            throw new NullPointerException("null input");
        }
        if (out == null) {
            throw new NullPointerException("null output");
        }
        assert(Utils.currentInstance.get() == null);
        TimeZone tz = (props.getBoolean(Utils.PACK_DEFAULT_TIMEZONE))
                      ? null
                      : TimeZone.getDefault();
        try {
            Utils.currentInstance.set(this);
            if (tz != null) TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            final int verbose = props.getInteger(Utils.DEBUG_VERBOSE);
            BufferedInputStream in0 = new BufferedInputStream(in);
            if (Utils.isJarMagic(Utils.readMagic(in0))) {
                if (verbose > 0)
                    Utils.log.info("Copying unpacked JAR file...");
                Utils.copyJarFile(new JarInputStream(in0), out);
            } else if (props.getBoolean(Utils.DEBUG_DISABLE_NATIVE)) {
                (new DoUnpack()).run(in0, out);
                in0.close();
                Utils.markJarFile(out);
            } else {
                (new NativeUnpack(this)).run(in0, out);
                in0.close();
                Utils.markJarFile(out);
            }
        } finally {
            _nunp = null;
            Utils.currentInstance.set(null);
            if (tz != null) TimeZone.setDefault(tz);
        }
    }
    public void unpack(File in, JarOutputStream out) throws IOException {
        if (in == null) {
            throw new NullPointerException("null input");
        }
        if (out == null) {
            throw new NullPointerException("null output");
        }
        try (FileInputStream instr = new FileInputStream(in)) {
            unpack(instr, out);
        }
        if (props.getBoolean(Utils.UNPACK_REMOVE_PACKFILE)) {
            in.delete();
        }
    }
    private class DoUnpack {
        final int verbose = props.getInteger(Utils.DEBUG_VERBOSE);
        {
            props.setInteger(Pack200.Unpacker.PROGRESS, 0);
        }
        final Package pkg = new Package();
        final boolean keepModtime
            = Pack200.Packer.KEEP.equals(
              props.getProperty(Utils.UNPACK_MODIFICATION_TIME, Pack200.Packer.KEEP));
        final boolean keepDeflateHint
            = Pack200.Packer.KEEP.equals(
              props.getProperty(Pack200.Unpacker.DEFLATE_HINT, Pack200.Packer.KEEP));
        final int modtime;
        final boolean deflateHint;
        {
            if (!keepModtime) {
                modtime = props.getTime(Utils.UNPACK_MODIFICATION_TIME);
            } else {
                modtime = pkg.default_modtime;
            }
            deflateHint = (keepDeflateHint) ? false :
                props.getBoolean(java.util.jar.Pack200.Unpacker.DEFLATE_HINT);
        }
        final CRC32 crc = new CRC32();
        final ByteArrayOutputStream bufOut = new ByteArrayOutputStream();
        final OutputStream crcOut = new CheckedOutputStream(bufOut, crc);
        public void run(BufferedInputStream in, JarOutputStream out) throws IOException {
            if (verbose > 0) {
                props.list(System.out);
            }
            for (int seg = 1; ; seg++) {
                unpackSegment(in, out);
                if (!Utils.isPackMagic(Utils.readMagic(in)))  break;
                if (verbose > 0)
                    Utils.log.info("Finished segment #"+seg);
            }
        }
        private void unpackSegment(InputStream in, JarOutputStream out) throws IOException {
            props.setProperty(java.util.jar.Pack200.Unpacker.PROGRESS,"0");
            new PackageReader(pkg, in).read();
            if (props.getBoolean("unpack.strip.debug"))    pkg.stripAttributeKind("Debug");
            if (props.getBoolean("unpack.strip.compile"))  pkg.stripAttributeKind("Compile");
            props.setProperty(java.util.jar.Pack200.Unpacker.PROGRESS,"50");
            pkg.ensureAllClassFiles();
            Set<Package.Class> classesToWrite = new HashSet<>(pkg.getClasses());
            for (Package.File file : pkg.getFiles()) {
                String name = file.nameString;
                JarEntry je = new JarEntry(Utils.getJarEntryName(name));
                boolean deflate;
                deflate = (keepDeflateHint)
                          ? (((file.options & Constants.FO_DEFLATE_HINT) != 0) ||
                            ((pkg.default_options & Constants.AO_DEFLATE_HINT) != 0))
                          : deflateHint;
                boolean needCRC = !deflate;  
                if (needCRC)  crc.reset();
                bufOut.reset();
                if (file.isClassStub()) {
                    Package.Class cls = file.getStubClass();
                    assert(cls != null);
                    new ClassWriter(cls, needCRC ? crcOut : bufOut).write();
                    classesToWrite.remove(cls);  
                } else {
                    file.writeTo(needCRC ? crcOut : bufOut);
                }
                je.setMethod(deflate ? JarEntry.DEFLATED : JarEntry.STORED);
                if (needCRC) {
                    if (verbose > 0)
                        Utils.log.info("stored size="+bufOut.size()+" and crc="+crc.getValue());
                    je.setMethod(JarEntry.STORED);
                    je.setSize(bufOut.size());
                    je.setCrc(crc.getValue());
                }
                if (keepModtime) {
                    je.setTime(file.modtime);
                    je.setTime((long)file.modtime * 1000);
                } else {
                    je.setTime((long)modtime * 1000);
                }
                out.putNextEntry(je);
                bufOut.writeTo(out);
                out.closeEntry();
                if (verbose > 0)
                    Utils.log.info("Writing "+Utils.zeString((ZipEntry)je));
            }
            assert(classesToWrite.isEmpty());
            props.setProperty(java.util.jar.Pack200.Unpacker.PROGRESS,"100");
            pkg.reset();  
        }
    }
}

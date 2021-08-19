package org.unicode.cldr.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.unicode.cldr.util.XPathParts.Comments;

public class SandboxLocales {
    private File base;
    private File main;
    private File annotations;

    /**
     * for writing XML
     */
    private String dtdDir = new File(CLDRConfig.getInstance().getCldrBaseDirectory(), "common/dtd")
        .getAbsolutePath();


    /**
     * Create a new SandboxLocales and populate it.
     * Unconditionally writes all 'scratch' locales (see SpecialLocales.txt) to the
     * main and annotations directories. Does not delete other locales that might be present in that directory.
     * @param base
     * @throws IOException
     */
    public SandboxLocales(File base) throws IOException {
        this.base = base;

        this.main = makeLocalesIn("main");
        this.annotations = makeLocalesIn("annotations");
    }

    private File makeLocalesIn(String subdir) throws UnsupportedEncodingException, FileNotFoundException {
        File dir = new File(base, subdir);
        if(!dir.isDirectory()) {
            dir.mkdirs();
        }
        // now, make the actual locales
        // NOTE: Cannot use SpecialLocales.getByType here,
        //        because we can't use CLDRLocale here,
        //        because CLDRLocale depends on inheritance data
        //        and SandboxLocales is used by SurveyTool during early startup before
        //        inheritance data is available.
        for(final String id : SpecialLocales.getScratchLocaleIds()) {
            File xml = new File(dir, id + ".xml");
            CLDRFile f = new CLDRFile(new ScratchXMLSource(id));
            write(f, xml);
        }
        return dir;
    }

    private void write(CLDRFile f, File outFile) throws UnsupportedEncodingException, FileNotFoundException {
        try (PrintWriter u8out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF8"))) {
            Map<String, String> options = new TreeMap<>();
            options.put("DTD_DIR", dtdDir+"/"); // use an absolute path to the DTDs.
            f.write(u8out, options);
        }
    }

    /**
     * get the sandbox/main directory
     * @return
     */
    public File getMainDir() {
        return main;
    }

    /**
     * get the annotations/main directory
     * @return
     */
    public File getAnnotationsDir() {
        return annotations;
    }

    /**
     * From DummyXMLSource
     * Just enough XMLSource to create an empty file.
     * @author srl
     *
     */
    private static class ScratchXMLSource extends XMLSource {
        ScratchXMLSource(String id) {
            super.setLocaleID(id);
            super.setInitialComment("GENERATED FILE\n"
                + "Note- This is a sandbox (scratch) locale.\n"+
            "It is not a part of the CLDR release.\nDo not modify it, and especially do not"
            + " check it in to the CLDR source repository.\n\nThis notice generated by SandboxLocales.java");
        }
        Map<String, String> valueMap = CldrUtility.newConcurrentHashMap();
        private Comments comments = new Comments();

        @Override
        public XMLSource freeze() {
            throw new RuntimeException("not implemented");
        }

        @Override
        public void putFullPathAtDPath(String distinguishingXPath,
            String fullxpath) {
            throw new RuntimeException("not implemented");
        }

        @Override
        public void putValueAtDPath(String distinguishingXPath, String value) {
            valueMap.put(distinguishingXPath, value);
        }

        @Override
        public void removeValueAtDPath(String distinguishingXPath) {
            throw new RuntimeException("not implemented");
        }

        @Override
        public String getValueAtDPath(String path) {
            return valueMap.get(path);
        }

        @Override
        public String getFullPathAtDPath(String path) {
            throw new RuntimeException("not implemented");
        }

        @Override
        public Comments getXpathComments() {
            return comments;
        }

        @Override
        public void setXpathComments(Comments comments) {
            this.comments = comments;
        }

        @Override
        public Iterator<String> iterator() {
            return valueMap.keySet().iterator();
        }

        @Override
        public void getPathsWithValue(String valueToMatch, String pathPrefix,
            Set<String> result) {
            throw new RuntimeException("not implemented");
        }
    }

    /**
     * Return this sandbox as a factory
     * @param commonMain File to commonMain, because you gotta have root
     * @return
     */
    public Factory getFactory(File commonMain) {
        File dirs[] = { getMainDir(),  getAnnotationsDir(), commonMain };
        return SimpleFactory.make(dirs, ".*");
    }
}
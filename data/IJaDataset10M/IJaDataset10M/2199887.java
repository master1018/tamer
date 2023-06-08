package au.edu.diasb.annotation.danno.test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import junit.framework.TestCase;
import au.edu.diasb.annotation.danno.impl.sesame.SesameAnnoteaTypeFactory;
import au.edu.diasb.annotation.danno.importer.danno.DannoAnnoteaImporter;
import au.edu.diasb.annotation.danno.model.RDFContainer;
import au.edu.diasb.annotation.danno.model.RDFTooComplexException;
import au.edu.diasb.chico.mvc.MimeTypes;

public class DannoAnnoteaImporterTest extends TestCase {

    public void testConstructor() {
        new DannoAnnoteaImporter(new SesameAnnoteaTypeFactory());
    }

    public void testImport() throws Exception {
        DannoAnnoteaImporter di = new DannoAnnoteaImporter(new SesameAnnoteaTypeFactory());
        InputStream is = TestUtils.getResourceAsStream("/danno-sample-1.xml");
        String[] expectedRDF = new String[] { "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" + "<rdf:Description rdf:nodeID=\"node14h85g43ex1\">\n" + "<rdf:type rdf:resource=\"http://www.w3.org/2000/10/annotation-ns#Annotation\"/>\n" + "<rdf:type rdf:resource=\"http://www.w3.org/2000/10/annotationType#Comment\"/>\n" + "<annotates xmlns=\"http://www.w3.org/2000/10/annotation-ns#\" rdf:resource=\"http://maenad-auselit.cloud.itee.uq.edu.au/danno/\"/>\n" + "<language xmlns=\"http://purl.org/dc/elements/1.0/\">en</language>\n" + "<title xmlns=\"http://purl.org/dc/elements/1.0/\">Aus-e-lit Website</title>\n" + "<creator xmlns=\"http://purl.org/dc/elements/1.0/\">Andrew Hyland</creator>\n" + "<created xmlns=\"http://www.w3.org/2000/10/annotation-ns#\">2009-09-15T20:45:42.962-07:00</created>\n" + "<modified xmlns=\"http://www.w3.org/2000/10/annotation-ns#\">Tue Oct 06 2009 13:42:43 GMT+1000</modified>\n" + "<context xmlns=\"http://www.w3.org/2000/10/annotation-ns#\">xpointer(string-range(/html[1]/body[1]/h1[1], \"\", 1, 9))</context>\n" + "<body xmlns=\"http://www.w3.org/2000/10/annotation-ns#\" rdf:nodeID=\"node14h85g43ex2\"/>\n" + "</rdf:Description>\n" + "<rdf:Description rdf:nodeID=\"node14h85g43ex2\">\n" + "<ContentType xmlns=\"http://www.w3.org/1999/xx/http#\">text/html</ContentType>\n" + "<Body xmlns=\"http://www.w3.org/1999/xx/http#\" rdf:parseType=\"Literal\"><html xmlns=\"http://www.w3.org/TR/REC-html40\"><head xmlns=\"http://www.w3.org/TR/REC-html40\"><title xmlns=\"http://www.w3.org/TR/REC-html40\">Aus-e-lit Website</title></head><body xmlns=\"http://www.w3.org/TR/REC-html40\"><A xmlns=\"http://www.w3.org/TR/REC-html40\" href=\"http://www.itee.uq.edu.au/%7Eeresearch/projects/aus-e-lit/\">http://www.itee.uq.edu.au/~eresearch/projects/aus-e-lit/</A></body></html></Body>\n" + "</rdf:Description>\n" + "<rdf:Description rdf:nodeID=\"node14h85g43ex1\">\n" + "<originalId xmlns=\"http://metadata.net/2009/09/danno#\" rdf:resource=\"http://maenad-auselit.cloud.itee.uq.edu.au/danno/annotea/23E585B850412996\"/>\n" + "<imported xmlns=\"http://metadata.net/2009/09/danno#\">NOW</imported>\n" + "<source xmlns=\"http://metadata.net/2009/09/danno#\">Danno Export</source>\n" + "</rdf:Description>\n" + "</rdf:RDF>", "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" + "<rdf:Description rdf:nodeID=\"node14h86135cx1\">\n" + "<rdf:type rdf:resource=\"http://www.w3.org/2000/10/annotation-ns#Annotation\"/>\n" + "<rdf:type rdf:resource=\"http://www.w3.org/2000/10/annotationType#Comment\"/>\n" + "<annotates xmlns=\"http://www.w3.org/2000/10/annotation-ns#\" rdf:resource=\"http://maenad-auselit.cloud.itee.uq.edu.au/danno/\"/>\n" + "<language xmlns=\"http://purl.org/dc/elements/1.0/\">en</language>\n" + "<title xmlns=\"http://purl.org/dc/elements/1.0/\">eResearch website</title>\n" + "<creator xmlns=\"http://purl.org/dc/elements/1.0/\">Anna Gerber</creator>\n" + "<created xmlns=\"http://www.w3.org/2000/10/annotation-ns#\">2009-09-07T23:07:22.844-07:00</created>\n" + "<modified xmlns=\"http://www.w3.org/2000/10/annotation-ns#\">Tue Oct 06 2009 13:42:43 GMT+1000</modified>\n" + "<context xmlns=\"http://www.w3.org/2000/10/annotation-ns#\">xpointer(string-range(/html[1]/body[1]/p[2], \"\", 86, 18))</context>\n" + "<body xmlns=\"http://www.w3.org/2000/10/annotation-ns#\" rdf:nodeID=\"node14h86135cx2\"/>\n" + "</rdf:Description>\n" + "<rdf:Description rdf:nodeID=\"node14h86135cx2\">\n" + "<ContentType xmlns=\"http://www.w3.org/1999/xx/http#\">text/html</ContentType>\n" + "<Body xmlns=\"http://www.w3.org/1999/xx/http#\" rdf:parseType=\"Literal\"><html xmlns=\"http://www.w3.org/TR/REC-html40\"><head xmlns=\"http://www.w3.org/TR/REC-html40\"><title xmlns=\"http://www.w3.org/TR/REC-html40\">eResearch website</title></head><body xmlns=\"http://www.w3.org/TR/REC-html40\"><BR xmlns=\"http://www.w3.org/TR/REC-html40\"></BR>The website is at<BR xmlns=\"http://www.w3.org/TR/REC-html40\"></BR><BR xmlns=\"http://www.w3.org/TR/REC-html40\"></BR><A xmlns=\"http://www.w3.org/TR/REC-html40\" href=\"http://itee.uq.edu.au/%7Eeresearch\">http://itee.uq.edu.au/~eresearch</A><BR xmlns=\"http://www.w3.org/TR/REC-html40\"></BR></body></html></Body>\n" + "</rdf:Description>\n" + "<rdf:Description rdf:nodeID=\"node14h86135cx1\">\n" + "<originalId xmlns=\"http://metadata.net/2009/09/danno#\" rdf:resource=\"http://maenad-auselit.cloud.itee.uq.edu.au/danno/annotea/CC0A67442A13065B\"/>\n" + "<imported xmlns=\"http://metadata.net/2009/09/danno#\">NOW</imported>\n" + "<source xmlns=\"http://metadata.net/2009/09/danno#\">Danno Export</source>\n" + "</rdf:Description>\n" + "</rdf:RDF>", "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" + "<rdf:Description rdf:nodeID=\"node14h87arstx1\">\n" + "<rdf:type rdf:resource=\"http://www.w3.org/2001/03/thread#Reply\"/>\n" + "<rdf:type rdf:resource=\"http://www.w3.org/2000/10/annotationType#Comment\"/>\n" + "<inReplyTo xmlns=\"http://www.w3.org/2001/03/thread#\" rdf:resource=\"http://maenad-auselit.cloud.itee.uq.edu.au/danno/annotea/0435824CE288AC2F\"/>\n" + "<root xmlns=\"http://www.w3.org/2001/03/thread#\" rdf:resource=\"http://maenad-auselit.cloud.itee.uq.edu.au/danno/annotea/3FF48E0DF114DBFB\"/>\n" + "<language xmlns=\"http://purl.org/dc/elements/1.0/\">en</language>\n" + "<title xmlns=\"http://purl.org/dc/elements/1.0/\">A New Reply Reply</title>\n" + "<creator xmlns=\"http://purl.org/dc/elements/1.0/\">Anonymous</creator>\n" + "<created xmlns=\"http://www.w3.org/2000/10/annotation-ns#\">2009-09-16T18:47:58.108-07:00</created>\n" + "<modified xmlns=\"http://www.w3.org/2000/10/annotation-ns#\">Tue Oct 06 2009 13:42:43 GMT+1000</modified>\n" + "<body xmlns=\"http://www.w3.org/2000/10/annotation-ns#\" rdf:nodeID=\"node14h87arstx2\"/>\n" + "</rdf:Description>\n" + "<rdf:Description rdf:nodeID=\"node14h87arstx2\">\n" + "<ContentType xmlns=\"http://www.w3.org/1999/xx/http#\">text/html</ContentType>\n" + "<Body xmlns=\"http://www.w3.org/1999/xx/http#\" rdf:parseType=\"Literal\"><html xmlns=\"http://www.w3.org/TR/REC-html40\"><head xmlns=\"http://www.w3.org/TR/REC-html40\"><title xmlns=\"http://www.w3.org/TR/REC-html40\">A New Reply Reply</title></head><body xmlns=\"http://www.w3.org/TR/REC-html40\">1234</body></html></Body>\n" + "</rdf:Description>\n" + "<rdf:Description rdf:nodeID=\"node14h87arstx1\">\n" + "<originalId xmlns=\"http://metadata.net/2009/09/danno#\" rdf:resource=\"http://maenad-auselit.cloud.itee.uq.edu.au/danno/annotea/3D3CEF5B64826855\"/>\n" + "<imported xmlns=\"http://metadata.net/2009/09/danno#\">NOW</imported>\n" + "<source xmlns=\"http://metadata.net/2009/09/danno#\">Danno Export</source>\n" + "</rdf:Description>\n" + "</rdf:RDF>", "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" + "<rdf:Description rdf:nodeID=\"node14h87f2qox1\">\n" + "<rdf:type rdf:resource=\"http://www.w3.org/2000/10/annotation-ns#Annotation\"/>\n" + "<rdf:type rdf:resource=\"http://www.w3.org/2000/10/annotationType#Comment\"/>\n" + "<annotates xmlns=\"http://www.w3.org/2000/10/annotation-ns#\" rdf:resource=\"http://maenad-auselit.cloud.itee.uq.edu.au/danno/\"/>\n" + "<language xmlns=\"http://purl.org/dc/elements/1.0/\">en</language>\n" + "<title xmlns=\"http://purl.org/dc/elements/1.0/\">RDF</title>\n" + "<creator xmlns=\"http://purl.org/dc/elements/1.0/\">Andrew Hyland</creator>\n" + "<created xmlns=\"http://www.w3.org/2000/10/annotation-ns#\">2009-09-15T23:53:46.095-07:00</created>\n" + "<modified xmlns=\"http://www.w3.org/2000/10/annotation-ns#\">Tue Oct 06 2009 13:42:43 GMT+1000</modified>\n" + "<context xmlns=\"http://www.w3.org/2000/10/annotation-ns#\">xpointer(string-range(/html[1]/body[1]/p[3], \"\", 69, 3))</context>\n" + "<body xmlns=\"http://www.w3.org/2000/10/annotation-ns#\" rdf:nodeID=\"node14h87f2qox2\"/>\n" + "</rdf:Description>\n" + "<rdf:Description rdf:nodeID=\"node14h87f2qox2\">\n" + "<ContentType xmlns=\"http://www.w3.org/1999/xx/http#\">text/html</ContentType>\n" + "<Body xmlns=\"http://www.w3.org/1999/xx/http#\" rdf:parseType=\"Literal\"><html xmlns=\"http://www.w3.org/TR/REC-html40\"><head xmlns=\"http://www.w3.org/TR/REC-html40\"><title xmlns=\"http://www.w3.org/TR/REC-html40\">RDF</title></head><body xmlns=\"http://www.w3.org/TR/REC-html40\">The spec address:<BR xmlns=\"http://www.w3.org/TR/REC-html40\"></BR>http://www.w3.org/TR/REC-rdf-syntax/<BR xmlns=\"http://www.w3.org/TR/REC-html40\"></BR><BR xmlns=\"http://www.w3.org/TR/REC-html40\"></BR></body></html></Body>\n" + "</rdf:Description>\n" + "<rdf:Description rdf:nodeID=\"node14h87f2qox1\">\n" + "<originalId xmlns=\"http://metadata.net/2009/09/danno#\" rdf:resource=\"http://maenad-auselit.cloud.itee.uq.edu.au/danno/annotea/3FF48E0DF114DBFB\"/>\n" + "<imported xmlns=\"http://metadata.net/2009/09/danno#\">NOW</imported>\n" + "<source xmlns=\"http://metadata.net/2009/09/danno#\">Danno Export</source>\n" + "</rdf:Description>\n" + "</rdf:RDF>", "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" + "<rdf:Description rdf:nodeID=\"node14h87i4b3x1\">\n" + "<rdf:type rdf:resource=\"http://www.w3.org/2001/03/thread#Reply\"/>\n" + "<rdf:type rdf:resource=\"http://www.w3.org/2000/10/annotationType#Comment\"/>\n" + "<inReplyTo xmlns=\"http://www.w3.org/2001/03/thread#\" rdf:resource=\"http://maenad-auselit.cloud.itee.uq.edu.au/danno/annotea/3FF48E0DF114DBFB\"/>\n" + "<root xmlns=\"http://www.w3.org/2001/03/thread#\" rdf:resource=\"http://maenad-auselit.cloud.itee.uq.edu.au/danno/annotea/3FF48E0DF114DBFB\"/>\n" + "<language xmlns=\"http://purl.org/dc/elements/1.0/\">en</language>\n" + "<title xmlns=\"http://purl.org/dc/elements/1.0/\">A New Reply</title>\n" + "<creator xmlns=\"http://purl.org/dc/elements/1.0/\">Anonymous</creator>\n" + "<created xmlns=\"http://www.w3.org/2000/10/annotation-ns#\">2009-09-16T18:09:15.032-07:00</created>\n" + "<modified xmlns=\"http://www.w3.org/2000/10/annotation-ns#\">Tue Oct 06 2009 13:42:43 GMT+1000</modified>\n" + "<body xmlns=\"http://www.w3.org/2000/10/annotation-ns#\" rdf:nodeID=\"node14h87i4b3x2\"/>\n" + "</rdf:Description>\n" + "<rdf:Description rdf:nodeID=\"node14h87i4b3x2\">\n" + "<ContentType xmlns=\"http://www.w3.org/1999/xx/http#\">text/html</ContentType>\n" + "<Body xmlns=\"http://www.w3.org/1999/xx/http#\" rdf:parseType=\"Literal\"><html xmlns=\"http://www.w3.org/TR/REC-html40\"><head xmlns=\"http://www.w3.org/TR/REC-html40\"><title xmlns=\"http://www.w3.org/TR/REC-html40\">A New Reply</title></head><body xmlns=\"http://www.w3.org/TR/REC-html40\">A reply to the 'RDF' annotation. <BR xmlns=\"http://www.w3.org/TR/REC-html40\"></BR></body></html></Body>\n" + "</rdf:Description>\n" + "<rdf:Description rdf:nodeID=\"node14h87i4b3x1\">\n" + "<originalId xmlns=\"http://metadata.net/2009/09/danno#\" rdf:resource=\"http://maenad-auselit.cloud.itee.uq.edu.au/danno/annotea/0435824CE288AC2F\"/>\n" + "<imported xmlns=\"http://metadata.net/2009/09/danno#\">NOW</imported>\n" + "<source xmlns=\"http://metadata.net/2009/09/danno#\">Danno Export</source>\n" + "</rdf:Description>\n" + "</rdf:RDF>" };
        try {
            List<RDFContainer> res = di.importAll(is, "http://foo", null);
            assertEquals(5, res.size());
            int i = 0;
            for (RDFContainer rdf : res) {
                TestUtils.assertEquivalentRDF(expectedRDF[i++], serializeAndMassage(rdf));
            }
        } finally {
            is.close();
        }
    }

    private String serializeAndMassage(RDFContainer rdf) throws RDFTooComplexException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        rdf.serialize(bos, MimeTypes.XML_RDF);
        String str = bos.toString();
        return str.replaceAll("<imported xmlns=\"http://metadata.net/2009/09/danno#\">(.+?)</imported>", "<imported xmlns=\"http://metadata.net/2009/09/danno#\">NOW</imported>");
    }
}
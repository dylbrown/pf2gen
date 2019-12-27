package ui;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

class PDFFiller {
    private static final PDFFiller instance;

    static {
        instance = new PDFFiller();
    }

    private PDFFiller(){}

    public static PDDocument getPDF() {return instance.pdf();}

    private PDDocument getSource() {
        PDDocument source=null;
        File file = new File("data/");
        if(file.exists()) {
            try {
                source = PDDocument.load(new File("data/testSheet.pdf"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            try {
                source = PDDocument.load(new URL("https://dylbrown.github.io/pf2gen_data/data/sheet.pdf").openStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return source;
    }

    private PDDocument pdf(){
        java.util.logging.Logger
                .getLogger("org.apache.pdfbox").setLevel(Level.ALL);
        PDDocument pdf = getSource();
        PDAcroForm form = pdf.getDocumentCatalog().getAcroForm();
        try {
            System.out.println(form.hasXFA());
            Document document = form.getXFA().getDocument();
            printDocument(document, new FileOutputStream(new File("text.txt")));
            form.getField("CONSTITUTION").setValue("10");
            //form.flatten();
        } catch (IOException | SAXException | ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
        return pdf;
    }
    public static void printDocument(Document doc, OutputStream out) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc),
                new StreamResult(new OutputStreamWriter(out, StandardCharsets.UTF_8)));
    }
}





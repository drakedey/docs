import org.docx4j.Docx4J;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.jaxb.Context;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.utils.SingleTraversalUtilVisitorCallback;
import org.docx4j.wml.*;

import javax.xml.bind.JAXBElement;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public interface CommonTrait
{
    default List<Object> getAllElementFromObject(Object obj, Class<?> toSearch)
    {
        List<Object> result = new ArrayList<>();
        System.out.println(obj.getClass());
        if (obj instanceof JAXBElement) obj = ((JAXBElement<?>) obj).getValue();
        if (obj instanceof MainDocumentPart) {
            int ct = 0;
            List<Integer> indexex = new ArrayList<>();
            for (Object o: ((MainDocumentPart) obj).getContent()) {
                if(o.toString().contains("PlaceholderForTable1")) {
                    System.out.println("ENCONTRO");
                    System.out.println(o.toString());
                    indexex.add(ct);
                }
                ct++;
            }

        }

        if (obj.getClass().equals(toSearch))
        {
            result.add(obj);
        } else if (obj instanceof ContentAccessor)
        {
            List<?> children = ((ContentAccessor) obj).getContent();
            for (Object child : children)
            {
                result.addAll(getAllElementFromObject(child, toSearch));
            }

        }
        return result;
    }

    default void test(MainDocumentPart documentPart, WordprocessingMLPackage wordMLPackage) throws Exception {
        int writableWidthTwips = wordMLPackage.getDocumentModel()
                .getSections().get(0).getPageDimensions().getWritableWidthTwips();
        int columnNumber = 7;
        ObjectFactory factory = Context.getWmlObjectFactory();
        P p = factory.createP();
        R r = factory.createR();
        Text t = factory.createText();
        t.setValue("Welcome To Baeldung");
        r.getContent().add(t);
        p.getContent().add(r);
        Tbl tbl = TblFactory.createTable(7, 7, writableWidthTwips/columnNumber);
        List<Object> rows = tbl.getContent();
        for (Object row : rows) {
            Tr tr = (Tr) row;
            List<Object> cells = tr.getContent();
            for(Object cell : cells) {
                Tc td = (Tc) cell;
                td.getContent().add(p);
            }
        }
        int ct = 0;
        List<Integer> tableIndexes = new ArrayList<>();
        List<Object> documentContents = documentPart.getContent();
        for (Object o: documentContents) {
            System.out.println("O: " + o.getClass() + " = " + ct );
            if (o.toString().contains("PlaceholderForTable1")) {
                tableIndexes.add(ct);
            }
            ct++;
        }

        for (Integer i: tableIndexes) {
            documentPart.getContent().remove(i.intValue());
            documentPart.getContent().add(i.intValue(), tbl);
        }
        documentContents = documentPart.getContent();
        Docx4J.save(wordMLPackage, new File("/tmp/OUT_generated.docx"));
    }

    default Object prepareVariables(Object body)
    {
        SingleTraversalUtilVisitorCallback paragraphVisitor
                = new SingleTraversalUtilVisitorCallback(
                new VariablePrepare.TraversalUtilParagraphVisitor());
        paragraphVisitor.walkJAXBElements(body);
        return body;
    }
}

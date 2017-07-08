package docs;

import de.lutana.geodataextractor.util.FileExtension;
import static docs.BasePublicationTest.DOC_FOLDER;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class PdfPublicationTest extends BasePublicationTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public String documentFile;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		Collection<Object[]> list = new ArrayList<>();
		File[] files = DOC_FOLDER.listFiles(new FileExtension.Filter("pdf"));
		for (File file : files) {
			list.add(new Object[]{file.getName()});
		}
		return list;
    }

	@org.junit.Test
    public void testDocuments() {
		runDocumentTest(this.documentFile);
    }
	
}

package system;

import model.player.PC;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ui.controls.SaveLoadController;
import ui.ftl.TemplateFiller;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class LoadThroughTest {

    static File[] getTestCharacters() {
        URL url = LoadThroughTest.class.getResource("/characters");
        File directory;
        try {
            if (url != null) {
                directory = new File(url.toURI());
            } else {
                return null;
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return directory.listFiles();
    }

    @ParameterizedTest
    @MethodSource("getTestCharacters")
    void loadAndExportSheet(File character) {
        PC pc = SaveLoadController.getInstance().load(character);
        TemplateFiller filler = new TemplateFiller(pc);
        filler.getSheet("printableSheet.html.ftl");
    }
}

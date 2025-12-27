package patmal.course.enigma.engineManager.loadManager;

import patmal.course.enigma.loader.schema.BTEEnigma;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;

public class LoadManagerImpl implements LoadManager {
    @Override
    public BTEEnigma load(String filePath) {
        basicFileChecks(filePath);

        return parseXml(filePath);
    }

    private BTEEnigma parseXml(String filePath) {
        try {
            JAXBContext context = JAXBContext.newInstance(BTEEnigma.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            return (BTEEnigma) unmarshaller.unmarshal(new File(filePath));

        } catch (JAXBException e) {
            throw new LoadException(
                    "Failed to parse XML file '" + filePath + "': " + e.getMessage(), e);
        }
    }

    private void basicFileChecks(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new LoadException("File path must not be empty.");
        }

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new LoadException("File does not exist: " + filePath);
        }

        if (!filePath.toLowerCase().endsWith(".xml")) {
            throw new LoadException("File must have '.xml' extension.");
        }
    }
}

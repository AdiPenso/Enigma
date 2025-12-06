package loadManager;

import engine.ConfigurationException;
import generated.BTEEnigma;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;

public class LoadManagerImpl implements LoadManager {
    @Override
    public BTEEnigma load(String filePath) /*throws LoadManagerException*/ {
        basicFileChecks(filePath);

        BTEEnigma dto = parseXml(filePath);

        return dto;
    }

    private BTEEnigma parseXml(String filePath) {
        try {
            // JAXBContext is a factory that "knows" how to map between XML and Java classes
            JAXBContext context = JAXBContext.newInstance(BTEEnigma.class);

            // Unmarshaller is the component that performs XML -> Java conversion
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // unmarshal(...) reads the XML file and creates a full object graph in memory
            return (BTEEnigma) unmarshaller.unmarshal(new File(filePath));

        } catch (JAXBException e) {
            // schema-wise problem, or XML not matching the XSD
            throw new ConfigurationException(
                    "Failed to parse XML file '" + filePath + "': " + e.getMessage(), e);
        }
    }

    private void basicFileChecks(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new ConfigurationException("File path must not be empty.");
        }

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new ConfigurationException("File does not exist: " + filePath);
        }

        if (!filePath.toLowerCase().endsWith(".xml")) {
            throw new ConfigurationException("File must have '.xml' extension.");
        }
    }
}

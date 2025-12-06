package testsForEngine;

import engine.ConfigurationException;
import engine.Engine;
import engine.EngineImpl;

public class PlayGround {
    public static void main(String[] args) {
        final Engine engine = new EngineImpl(); // הנחה שיש מימוש של המנוע בשם EngineImpl
        String filePath = "C:\\Users\\user\\Desktop\\LastYear\\PTML\\testingFiles\\ex1-sanity-small.xml";

            try {
                engine.loadXml(filePath);
                System.out.println("XML file loaded successfully.");
            } catch (ConfigurationException e) {
                // שגיאה לוגית בקובץ – מה שאנחנו זרקנו מהמנוע
                System.out.println("Failed to load XML: " + e.getMessage());
                System.out.println("Last valid configuration (if any) is kept.");
            } catch (Exception e) {
                // לכל מקרה בלתי צפוי – שלא יפיל את התוכנית
                System.out.println("Unexpected error while loading XML: " + e.getMessage());
            }
        }
}

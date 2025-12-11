package testsForEngine;

import engine.ConfigurationException;
import engine.Engine;
import engine.EngineImpl;

public class PlayGround4 {
            public static void main(String[] args) {

                Engine engine = new EngineImpl();

                // ---- 1. נטען קובץ XML תקין ----
                String filePath = "C:\\Users\\user\\Desktop\\LastYear\\PTML\\testingFiles\\ex1-our-test.xml";

                try {
                    System.out.println("Loading XML...");
                    engine.loadXml(filePath);
                    System.out.println("XML loaded successfully!");
                } catch (ConfigurationException e) {
                    System.out.println("XML configuration error: " + e.getMessage());
                    return;
                } catch (Exception e) {
                    System.out.println("Unexpected error: " + e.getMessage());
                    return;
                }

                // ---- 2. הפעלת קוד אוטומטי ----
                try {
                    System.out.println("\nGenerating automatic code...");
                    engine.codeAutomatic();
                    System.out.println("Automatic code generated!");
                } catch (Exception e) {
                    System.out.println("Error during automatic code creation: " + e.getMessage());
                    return;
                }

                // ---- 3. הצפנת מחרוזת, שמירתה, ואז פיענוח ----
                String plainText = "HELLOWORLD";

                // הצפנה
                String cipherText = engine.processText(plainText);
                System.out.println("\nPlain text : " + plainText);
                System.out.println("Cipher text: " + cipherText);

                // איפוס המכונה לאותו קוד התחלתי
                engine.resetToLastCode();

                // פיענוח – פשוט מריצים שוב את אותה פונקציה על הטקסט המוצפן
                String decrypted = engine.processText(cipherText);
                System.out.println("Decrypted  : " + decrypted);

                // בדיקה לוגית
                if (plainText.equals(decrypted)) {
                    System.out.println("\n SUCCESS: decrypted text equals original plain text.");
                } else {
                    System.out.println("\n ERROR: decrypted text does NOT match original!");
                }
            }
        }

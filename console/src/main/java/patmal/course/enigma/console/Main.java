package patmal.course.enigma.console;

import patmal.course.enigma.engineManager.engine.Engine;
import patmal.course.enigma.engineManager.engine.EngineImpl;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Engine engine = new EngineImpl();
        new ConsoleUI(engine).showMenu();
        }
}
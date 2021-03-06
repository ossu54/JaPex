package logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ui.UI;

public class Main {
  public static Input input;
  public static LinkedList<State> stateQueue = new LinkedList<State>();
  public static int currentStateNr = 0;
  public static int currentByteNrIndex = 0;
  public static UI ui;
  public static int varNr;
  public static boolean reachedReturn;

  public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
      IllegalAccessException, UnsupportedLookAndFeelException, IOException {

    try {
      // Set cross-platform Java L&F (also called "Metal")
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (UnsupportedLookAndFeelException e) {
      System.out.println(e.getStackTrace());
    } catch (ClassNotFoundException e) {
      System.out.println(e.getStackTrace());
    } catch (InstantiationException e) {
      System.out.println(e.getStackTrace());
    } catch (IllegalAccessException e) {
      System.out.println(e.getStackTrace());
    }

    ui = new UI();
    Main.initalizeInput();
    ui.setVisible(true);

  }

  public static void readJava(String path) throws IOException {
    Runtime runtime = Runtime.getRuntime();
    Process process = runtime.exec("javac " + path);
    process.destroy();
    readClass(path.substring(0, path.lastIndexOf('.')) + ".class");
  }

  public static void readClass(String path) throws IOException {
    Runtime runtime = Runtime.getRuntime();
    Process process = runtime.exec("javap -c " + path);
    InputStream is = process.getInputStream();
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);
    String line;
    String input = "";
    while (!((line = br.readLine().trim()).startsWith("public static"))) {

    }
    input += line.trim() + "\n";

    while (!(line = br.readLine()).trim().equals("}")) {
      input += line.trim() + "\n";
    }
    process.destroy();
    ui.txaInput.setText(input.substring(0,input.length()-1));
  }

  public static void initalizeInput() {
    ui.disableButtons();
    ui.clearAll();
    currentStateNr = 0;
    currentByteNrIndex = 0;
    reachedReturn = false;
    stateQueue = new LinkedList<State>();
    String in = ui.txaInput.getText();
    input = new Input(in);
    while (!reachedReturn) {
      int currentByteNrIndexCheck = currentByteNrIndex;
      OpCode.toMatch(input.byteNrToLine.get(input.byteNrList.get(currentByteNrIndex)));
      currentByteNrIndex =
          (currentByteNrIndexCheck == currentByteNrIndex) ? currentByteNrIndex + 1
              : currentByteNrIndex;
      currentStateNr++;
    }
    currentStateNr = 0;
    ui.enableButtons();
  }

  public static void toStart() {
    currentStateNr = 0;
    ui.clearAll();
  }

  public static void nextState() {
    if (currentStateNr < stateQueue.size()) {
      updateUI(true);
      currentStateNr++;

    } else {
      ui.lblCurrent.setText("You've reached the end of the method.");
    }

  }

  public static void previousState() {
    if (currentStateNr > 1) {
      currentStateNr--;
      updateUI(false);
    } else {
      toStart();
    }
  }

  public static void updateUI(boolean next) {
    int stateIndex = (next ? currentStateNr : currentStateNr - 1);
    ui.lblCurrent.setText(stateQueue.get(stateIndex).getLine());
    updateLocals(stateIndex);
    updateStack(stateIndex);
  }

  private static void updateStack(int stateIndex) {
    ui.clearStack();
    int varNr = 0;
    boolean first = true;
    for (int i = stateQueue.get(stateIndex).getOperandStack().size() - 1; i >= 0; i--) {
      StoredValue value = stateQueue.get(stateIndex).getOperandStack().get(i);
      if (!first && value == stateQueue.get(stateIndex).getOperandStack().get(i + 1)) {
        ui.addStack(varNr - 1 + ":  " + value.toString());
      } else {
        first = false;
        ui.addStack(varNr + ":  " + value.toString());
      }
      varNr++;
    }
  }

  private static void updateLocals(int stateIndex) {
    ui.clearLocals();
    boolean first = true;
    int varNr = 0;
    for (int i = 0; i < stateQueue.get(stateIndex).getLocalVariables().size(); i++) {
      StoredValue storedValue = stateQueue.get(stateIndex).getLocalVariables().get(i);
      if (!first && storedValue == stateQueue.get(stateIndex).getLocalVariables().get(i - 1)) {
        ui.addLocals(varNr++ - 1 + ":  " + storedValue.toString());
      } else {
        first = false;
        ui.addLocals(varNr++ + ":  " + storedValue.toString());
      }
    }
  }
}

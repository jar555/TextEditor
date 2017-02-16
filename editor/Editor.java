package editor;

import javafx.application.Application;
import java.util.LinkedList;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ScrollBar;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class Editor extends Application {
    private int WINDOW_HEIGHT= 500;
    private int WINDOW_WIDTH = 500;

    private Group baseRoot = new Group();
    private static String filename;
    private boolean redo;

    /** The Text to display on the screen. */
    public Text displayText = new Text(250, 250, "");
    public int fontSize = 12;
    private String fontName = "Verdana";

    private TextLinkedList<Text> textList = new TextLinkedList<Text>();
    private LinkedList<Text> undoList = new LinkedList<Text>();
    private LinkedList<Integer> undoIndex = new LinkedList<Integer>();
    private LinkedList<Boolean> addOrDelete = new LinkedList<Boolean>();
    private LinkedList<Text> redoList = new LinkedList<Text>();
    private LinkedList<Integer> redoIndex = new LinkedList<Integer>();
    private LinkedList<Boolean> redoAddorDelete = new LinkedList<Boolean> ();

    /** The cursor to display on the screen */
    private Rectangle cursor = new Rectangle(0, 0, Color.BLACK);
    ScrollBar baseScrollbar = new ScrollBar();

    private void textRender() {
        for (int i = 0; i != textList.size(); i ++) {
            Text text = textList.get(i);
            baseRoot.getChildren().remove(text);
            baseRoot.getChildren().add(text);
        }
        baseScrollbar.setMax(textList.getEnterHeight());
    }

    public void textAdd(String letter, int i) {
        Text text = new Text();
        text.setText(letter);
        textSetPosition(text);
        textList.add(text, i);
        textRender();
        cursorRender();
    }

    public void enterAdd(String letter, int i) {
        Text text = new Text();
        text.setText(letter);
        enterSetPosition(text);
        textList.add(text, i);
        textRender();     
    }

    public void textSetPosition(Text text) {
        if (textList.isEmpty()) {
            text.setTextOrigin(VPos.TOP);
            text.setFont(Font.font (fontName, fontSize));
            text.setX(5);
            text.setY(0);
        } else {
            int lastPosition = textList.getCursorIndex() - 1;
            text.setTextOrigin(VPos.TOP);
            text.setFont(Font.font (fontName, fontSize));
            text.setX(textList.getX(lastPosition));
            text.setY(textList.getY(lastPosition));
        }
    }

    private boolean checkWrap(Text text) {
        int x = (int) text.getX() + (int) (text.getLayoutBounds().getWidth() * 1.5);
        int width = (int) baseScrollbar.getLayoutX();
        return x + 2> width;
    }

    private void wrap(int index) {
        int count = 0;
        for (int i = index; i != 0; i --) {
            if (textList.get(i).getText().equals(" ")) {
                count ++;
                break;
            }
            count ++;
        }
        textInsert("\r", index - count + 1);
        textUpdate(textList.getCursorIndex());
        textList.setCursorIndex(index + 2);
    }

    public void enterSetPosition(Text text) {
        double enterHeight = text.getLayoutBounds().getHeight() / 2;
        if (textList.isEmpty()) {
            text.setTextOrigin(VPos.TOP);
            text.setFont(Font.font (fontName, fontSize));
            text.setX(5);
            text.setY(text.getLayoutBounds().getHeight() / 2);
        } else {
            int lastPosition = textList.getIndex() - 1;
            double lastTextWidth = textList.get(lastPosition).getLayoutBounds().getWidth(); 
            text.setTextOrigin(VPos.TOP);
            text.setFont(Font.font (fontName, fontSize));
            text.setX(5);
            text.setY(textList.getY(lastPosition) + enterHeight);
        }      
    }

    public void cursorRender() {
        if (textList.isEmpty() || textList.getCursorIndex() < 1) {
            baseRoot.getChildren().remove(cursor);
            cursor.setWidth(1);
            cursor.setHeight(fontSize + 2.58);
            cursor.setY(0);
            cursor.setX(5);
            baseRoot.getChildren().add(cursor);    
        } else {
            int index = textList.getCursorIndex() - 1;
            double height = textList.getHeight(index);
            double textWidth = textList.get(index).getLayoutBounds().getWidth(); 
            baseRoot.getChildren().remove(cursor);
            cursor.setWidth(1);
            cursor.setHeight(height);
            cursor.setY(textList.get(index).getY());
            cursor.setX(textList.get(index).getX() + textWidth);
            baseRoot.getChildren().add(cursor);
        }
    }

    public void cursorEnterRender() {
        if (textList.isEmpty() || textList.getCursorIndex() <= 1) {
            baseRoot.getChildren().remove(cursor);
            cursor.setWidth(1);
            cursor.setHeight((fontSize + 2.58));
            cursor.setY(0);
            cursor.setX(5);
            baseRoot.getChildren().add(cursor);    
        } else {
            int index = textList.getCursorIndex() - 1; // minus 1 might not be needed
            double height = textList.getHeight(index);
            double textWidth = textList.get(index).getLayoutBounds().getWidth(); 
            baseRoot.getChildren().remove(cursor);
            cursor.setWidth(1);
            cursor.setHeight(height / 2);
            cursor.setY(textList.get(index).getY());
            cursor.setX(textList.get(index).getX() + textWidth);
            baseRoot.getChildren().add(cursor);
        }        
    }

    public boolean checkUpdatePosition() {
        if (textList.getCursorIndex() != textList.getIndex()) {
            return true;
        }
        return false;
    }

    private void textUpdate(int cursor) {
        TextLinkedList<Text> tempTextList = new TextLinkedList<Text>(); 
        TextLinkedList<Text> backup = textList;
        textList = tempTextList;
        for (int i = 0; i != backup.size(); i ++) {
            baseRoot.getChildren().remove(backup.get(i));
            textAdd(backup.get(i).getText(), i);
        }
        textList.setCursorIndex(cursor);
    }

    private void textInsert(String letter, int cursor ) {
        Text text = new Text();
        text.setTextOrigin(VPos.TOP);
        text.setFont(Font.font (fontName, fontSize));
        text.setText(letter);
        textList.add(text, cursor);
        textUpdate(cursor + 1);
        textList.setCursorIndex(cursor + 1);
        cursorRender();
    }

    private boolean isEnter(Text t) {
        if (t.getText().equals("\r")) {
            return true;
        }
        return false;
    }

    private void moveUp() {  //Doesn't work
        int curIndex = textList.getCursorIndex() - 1;
        int nextIndex = 0;
        if (curIndex < 0) {
            curIndex = 0;
        }
        double curX = textList.get(curIndex).getX();
        double curY = textList.get(curIndex).getY();
        double upX = curX;
        double upY = textList.getHeight(curIndex) - curY;

        for (int i = curIndex; i != 0; i --) {
            if (textList.get(i).getY() != curY && 
                    textList.get(i).getX() - 1.1 < curX && textList.get(i).getX() + 1.1 > curX
                        && !textList.get(i).getText().equals("\r")) {
                nextIndex = i + 1;
                break;
            }
        }
    }

    private void regularOrEnter() {
        int cursorIndex = textList.getCursorIndex() - 1;
        if (cursorIndex < 0) {
            cursorRender();
        } else if (textList.get(cursorIndex).getText().equals("\r")) {
            cursorEnterRender();
        } else {
            cursorRender();
        }
    }

    private boolean previousEnter() {
        if (textList.getCursorIndex() - 1 < 1) {
            return false;
        } else if (textList.get(textList.getCursorIndex() - 1).getText().equals("\r")) {
            return true;
        }
        return false;
    }

    public void saveText() {
        try {
            File inputFile = new File(filename);
            FileWriter writer = new FileWriter(filename);
            for (int i = 0; i != textList.size(); i ++) {
                String charRead = textList.get(i).getText();
                if (charRead.equals("\r") || charRead.equals("\n")) {
                    writer.write("\r\n");
                } else {
                    writer.write(charRead);
                }
            } 
            writer.close();
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("File not found! Exception was: " + fileNotFoundException);
        } catch (IOException ioException) {
            System.out.println("Error when copying; exception was: " + ioException);
        }
    }

    public void open(String filename) {
        try {
            FileReader reader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(reader);
            int intRead = -1;
            int index = 0;

            while ((intRead = bufferedReader.read()) != -1) {
                char charRead = (char) intRead;
                if (Character.toString(charRead).equals("\n\r")) {    
                    textAdd("\r", index);
                } else {
                    textAdd(Character.toString(charRead), index);
                }
                index ++;
            }
            textList.setCursorIndex(0);
            cursorRender();
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("File not found! Exception was: " + fileNotFoundException);
        } catch (IOException ioException) {
            System.out.println(ioException);
        }
    }

    private void addToUndo(Text t, int i, boolean b) {
        if (undoList.size() == 100) {
            undoList.removeFirst();
            undoIndex.removeFirst();
            addOrDelete.removeFirst();
        }
            undoList.add(t);
            undoIndex.add(i);
            addOrDelete.add(b);
    }

    private void addtoRedo(Text t, int i, boolean b) {
        if (redoList.size() == 100) {
            redoList.removeFirst();
            redoIndex.removeFirst();
            redoAddorDelete.removeFirst();
        } else {

        }
    }

    private void undo() {
        if (undoList.isEmpty()) {

        } else {
            Text last = undoList.getLast();
            int i = undoIndex.getLast();
            boolean b = addOrDelete.getLast();

            if (!b) {
                Text deleted = textList.remove(i - 1);
                baseRoot.getChildren().remove(deleted);
                textRender();
                regularOrEnter();
                redoIndex.add(undoIndex.removeLast());
                redoList.add(undoList.removeLast());
                redoAddorDelete.add(addOrDelete.removeLast());
            } else {
                textInsert(last.getText(), undoIndex.getLast());
                redoList.add(undoList.removeLast());
                redoIndex.add(undoIndex.removeLast());
                redoAddorDelete.add(addOrDelete.removeLast());
            }
        }
        redo = true;
    }

    private void redo() {
        if (redoList.isEmpty()) {

        } else {
            Text last = redoList.getLast();
            int i = redoIndex.getLast();
            boolean b = redoAddorDelete.getLast();  

            if (!b) {
                textInsert(last.getText(), redoIndex.getLast() - 1);
                undoList.add(redoList.removeLast());
                undoIndex.add(redoIndex.removeLast());
                addOrDelete.add(redoAddorDelete.removeLast());
            } else {
                Text deleted = textList.remove(i);
                baseRoot.getChildren().remove(deleted);
                textRender();
                regularOrEnter();
                undoIndex.add(redoIndex.removeLast());
                undoList.add(redoList.removeLast());
                addOrDelete.add(redoAddorDelete.removeLast());
            }
        }
    }

    private class CursorBlink implements EventHandler<ActionEvent> {
        private int index = 1;
        private int width = 0;

        CursorBlink() {
            changeWidth();
        }

        private void changeWidth() {
            if (index == 1) {
                width = 1;
                index ++;
                cursor.setWidth(width);
                baseRoot.getChildren().remove(cursor);
                baseRoot.getChildren().add(cursor);
            } else {
                index --;       
                width = 0;
                cursor.setWidth(width);
                baseRoot.getChildren().remove(cursor);
                baseRoot.getChildren().add(cursor);
            }
        }

        @Override
        public void handle(ActionEvent event) {
            changeWidth();
        }
    }

    private void cursorWidthRender() {
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        CursorBlink cursorChange = new CursorBlink();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), cursorChange);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    private boolean special(String s) {
        if (!s.equals("\u0010") && !s.equals("\u0013") && !s.equals("\u001A")
            && !s.equals("\u0019")) {
            return true;
        }
        return false;
    }

    /** An EventHandler to handle keys that get pressed. */
    private class KeyEventHandler implements EventHandler<KeyEvent> {
        Text deletedText;

        @Override
        public void handle(KeyEvent keyEvent) {
            if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                String charTyped = keyEvent.getCharacter();
                if (charTyped.length() > 0 && charTyped.charAt(0) != 13 && charTyped.charAt(0) != 8 && special(charTyped)) {
                    if (textList.getCursorIndex() == textList.getIndex()) {
                        textAdd(charTyped, textList.getCursorIndex());
                    } else {
                        textInsert(charTyped, textList.getCursorIndex());
                    }
                    if (checkWrap(textList.get(textList.getCursorIndex() - 1))) {
                        wrap(textList.getCursorIndex() - 1);
                    }
                    addToUndo(textList.get(textList.getCursorIndex() - 1), textList.getCursorIndex(), false);
                    keyEvent.consume();
                    redo = false;
                } else if (charTyped.length() > 0 && charTyped.charAt(0) == 13) { //enter key
                    enterAdd(charTyped, textList.getCursorIndex());
                    cursorEnterRender();
                    addToUndo(textList.get(textList.getCursorIndex() - 1), textList.getCursorIndex(), false);
                    keyEvent.consume();
                    redo = false;
                } else {
                }
            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                KeyCode code = keyEvent.getCode();
                if (code == KeyCode.BACK_SPACE) {
                    if (textList.getCursorIndex() <= 0) {
                        keyEvent.consume();
                    } else {
                        deletedText = textList.remove(textList.getCursorIndex() - 1);
                        if (textList.getCursorIndex() != textList.getIndex()) {
                            textList.update(textList.getCursorIndex());
                            textUpdate(textList.getCursorIndex());
                        }                        
                        baseRoot.getChildren().remove(deletedText);
                        if (deletedText.getText() == "\r" || previousEnter()) {
                            cursorEnterRender();
                        } else {
                            cursorRender();
                        }
                        textRender();
                    }
                    addToUndo(deletedText, textList.getCursorIndex(), true);
                    redo = false;
                    keyEvent.consume();
                                        
                } else if (code == KeyCode.LEFT) {
                    if (textList.getCursorIndex() <= 0) {
                        keyEvent.consume();
                    } else {
                        textList.cursorLeft();
                        regularOrEnter();
                        keyEvent.consume();   
                    }       
                } else if (code == KeyCode.RIGHT) {
                    if (textList.getCursorIndex() >= textList.getIndex()) {
                        keyEvent.consume();
                    } else {
                        textList.cursorRight();
                        regularOrEnter();
                        keyEvent.consume();    
                    }
                } else if (code == KeyCode.UP) {
                    keyEvent.consume();
                } else if (code == KeyCode.DOWN) {
                    keyEvent.consume();
                } else if (keyEvent.isShortcutDown()) {
                    if (code == KeyCode.PLUS || code == KeyCode.EQUALS) {
                        fontSize += 4;
                    } else if (code == KeyCode.MINUS) {
                        fontSize -= 4;
                    } else if (code == KeyCode.S) {
                        saveText();
                    } else if (code == KeyCode.P) {
                        textList.printCursor();
                    } else if (code == KeyCode.Z) {
                        undo();
                    } else if (code == KeyCode.Y) {
                        if (redo) {
                            redo();
                        }
                    }
                }
            }
        }
    }


    @Override
    public void start(Stage primaryStage) {
        // Create a Node that will be the parent of all things displayed on the screen.
        Group root = new Group();
        root.getChildren().add(baseRoot);
        // The Scene represents the window: its height and width will be the height and width
        // of the window displayed.
        int windowWidth = WINDOW_WIDTH;
        int windowHeight = WINDOW_HEIGHT;
        Scene scene = new Scene(root, windowWidth, windowHeight, Color.WHITE);

        // To get information about what keys the user is pressing, create an EventHandler.
        // EventHandler subclasses must override the "handle" function, which will be called
        // by javafx.
        EventHandler<KeyEvent> keyEventHandler =
                new KeyEventHandler();
        // Register the event handler to be called for all KEY_PRESSED and KEY_TYPED events.
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);

        primaryStage.setTitle("Editor");
        ScrollBar scrollbar = baseScrollbar;
        scrollbar.setOrientation(Orientation.VERTICAL);
        scrollbar.setMin(0);
        scrollbar.setMax(textList.getEnterHeight());
        scrollbar.setPrefHeight(windowHeight);
        scrollbar.setUnitIncrement(3);
        scrollbar.setBlockIncrement(3);
        root.getChildren().add(scrollbar);
        double usableScreenWidth = windowWidth - scrollbar.getLayoutBounds().getWidth();
        scrollbar.setLayoutX(usableScreenWidth);
        cursorRender();
        cursorWidthRender();

        scrollbar.valueProperty().addListener(new ChangeListener<Number>() {
        @Override public void changed(
                ObservableValue<? extends Number> observableValue,
                Number oldValue,
                Number newValue) {
            double scrollbarValue = (double) newValue;
            baseRoot.setLayoutY(-scrollbarValue);
            }
        });

        scene.widthProperty().addListener(new ChangeListener<Number>() {
        @Override public void changed(
                ObservableValue<? extends Number> observableValue,
                Number oldWidth,
                Number newWidth) {
            double usableScreenWidth = (double) newWidth - scrollbar.getLayoutBounds().getWidth();
            scrollbar.setLayoutX(usableScreenWidth);
            WINDOW_WIDTH = (int) Math.round((double) newWidth);
            }
        });

        scene.heightProperty().addListener(new ChangeListener<Number>() {
        @Override public void changed(
                ObservableValue<? extends Number> observableValue,
                Number oldHeight,
                Number newHeight) {
            scrollbar.setPrefHeight((double) newHeight);
            WINDOW_HEIGHT = (int) Math.round((double) newHeight);
            }
        });


        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    @Override
    public void init() {
        filename = getParameters().getRaw().get(0);
        open(filename);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

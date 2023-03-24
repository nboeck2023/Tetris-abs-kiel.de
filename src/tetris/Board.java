package tetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;

import org.jetbrains.annotations.NotNull;
import tetris.Shape.Tetrominoes;

/*
Spielbrett mit der Spiellogik aufbauen
 */
public class Board extends JPanel
        implements ActionListener {

    // Liste der Variablen, die im Spiel benötigt werden
    private final int BOARDWIDTH = 13; // es passen 13 Quadrate auf die Breite
    private final int BOARDHEIGHT = 26; // es passen 26 Quadrate auf die Höhe

    private Timer timer; // Objekt für die Animation
    private boolean isFallingFinished = false; // gibt Wahrheitswert, ob eine Form noch fällt
    private boolean isStarted = false; // diese Variable bestimmt, ob das Spiel läuft
    private boolean isPaused = false; // diese Variable bestimmt, ob das Spiel pausiert ist
    private int numLinesRemoved = 0; // Zählervariable für entfernte Spielreihen
    private int punkte = 0; // Punktzahl für den Spieler
    private int curX = 0; // aktuelle x-Position der aktuellen Form, wenn sie fällt
    private int curY = 0; // aktuelle y-Position fder aktuellen Form, wenn sie fällt
    private JLabel statusbar; // Objekt für die JLabel, die die Information über Punkte, Level und Highscore anzeigt
    private Shape curPiece; // Objekt für unsere aktuelle Form
    private Tetrominoes[] board; // Liste für alle gesetzten Reihen oder Steine, auf unserem Spielfeld
    private int level = 1; // Variable für die Schwierigkeit unseres Spiels

    BufferedReader reader = new BufferedReader(new FileReader("Dateien/texte/highscore"));
    private int highscore = Integer.parseInt(reader.readLine());

    // Konstruktor mit Parameter aus sich selbst
    public Board(Tetris tetris) throws IOException {

        initBoard(tetris);
    }

    private void initBoard(Tetris parent) { // ein Objekt aus der Klasse Tetris übergeben

        setFocusable(true); // diese Methode muss auf true gesetzt werden, damit die Tastatur ausgelesen werden kann
        curPiece = new Shape(); // Instanz aus der Klasse Shape
        timer = new Timer(400, this); // Timer erstellen
        timer.start(); // timer starten -> Animation beginnt

        statusbar = parent.getStatusbar(); // Statusbar aus dem Elternelement ziehen → alle Änderungen werden live übertragen
        statusbar.setText("Pkt.: " + punkte + "     LEVEL: " + level + "        HSC: " + highscore);
        // Spielfeld aufbauen, als Liste, in der die gespielten Steine gespeichert werden
        board = new Tetrominoes[BOARDHEIGHT * BOARDWIDTH];
        addKeyListener(new TAdapter()); // Methode für die Tastatureingabe
        clearBoard(); // Spielfeld mit leeren Formen füllen
    }

    // Methode für die einzelnen Events = Das Fallen der Formen
    @Override
    public void actionPerformed(ActionEvent e) {
        // Wenn die Form nicht mehr fällt
        if (isFallingFinished) {

            isFallingFinished = false; // Variable wieder auf false setzen damit eine neue Form kommen kann
            try {
                newPiece(); // neue Form ausgeben
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            levelUP(); // erhöht bei Bedarf die Schwierigkeit
        } else {
            try {
                oneLineDown(); // Form eine Zeile fallen lassen
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    // Methode, die die Breite des zu zeichnenden Quadrats zurückgibt
    private int squareWidth() {
        return (int) getSize().getWidth() / BOARDWIDTH;
    }

    // Methode, die die Höhe des zu zeichnenden Quadrats zurückgibt
    private int squareHeight() {
        return (int) getSize().getHeight() / BOARDHEIGHT;
    }

    //Methode, die die Form zurückgibt, die sich an einer bestimmten Koordinate befindet, → es kann NoShape oder eine physische Form sein
    private Tetrominoes shapeAt(int x, int y) {
        return board[(y * BOARDWIDTH) + x];
    }

    // Der start des Spiels und das Verlassen der Pause
    public void start() throws IOException {
        // Wenn pausiert ist, soll sofort aus dieser Methode hinausgegangen werden
        if (isPaused) {
            return;
        }
        // Startvariablen setzen
        isStarted = true;
        isFallingFinished = false;
        numLinesRemoved = 0;
        punkte = 0;
        clearBoard();

        newPiece();
        timer.start();
    }

    private void pause() {
        // nicht ausführen, wenn das Spiel bereits pausiert
        if (!isStarted) {
            return;
        }
        // isPaused auf das Gegenteil des aktuellen Werts setzen
        isPaused = !isPaused;
        // Wenn Spiel pausiert ist
        if (isPaused) {

            timer.stop();
            statusbar.setText("Spiel ist pausiert");
        } else {

            timer.start(); // starten timer erneut
            statusbar.setText("Pkt.: " + punkte + "     LEVEL: " + level + "        HSC: " + highscore);
        }
        repaint();
    }

    /*
    Innerhalb der doDrawing-Methode werden alle Objekte auf dem Spielfeld gezeichnet.
    Im ersten Schritt werden alle bereits gespielten Formen oder deren Überbleibsel,
    die sich am Boden des Spielfelds befinden gezeichnet.
    Im zweiten Schritt zeichen wir die aktuelle Form.
     */
    private void doDrawing(Graphics g) {
        // Dimension setzen, um die Größen der mQuadrate zu ermitteln
        Dimension size = getSize();
        // oberen Spielrand ermitteln
        int boardTop = (int) size.getHeight() - BOARDHEIGHT * squareHeight();
        // Erster Schritt
        for (int i = 0; i < BOARDHEIGHT; ++i) { // jede Zeile von oben nach unten durchgehen
            for (int j = 0; j < BOARDWIDTH; ++j) { // jede Spalte von links nach rechts einzeln durchgehen
                // Wert für die Form an dieser Stelle setzen
                Tetrominoes shape = shapeAt(j, BOARDHEIGHT - i - 1);
                // Wenn Form nicht die NoShape ist
                if (shape != Tetrominoes.NoShape) {
                    // EinQuadrat an dieser Stelle zeichnen.
                    // Der Shape-Parameter bestimmt aus welcher Form das Quadrat stammt
                    drawSquare(g, j * squareWidth(),
                            boardTop + i * squareHeight(), shape);
                }
            }
        }
        // Zweiter Schritt.
        // Wenn die aktuelle Form keine NoShape ist.
        if (curPiece.getShape() != Tetrominoes.NoShape) {
            // Array für die Koordinaten zum Zeichnen dieser Form durchlaufen
            for (int i = 0; i < 4; ++i) {
                // neue Position der Form herausfinden
                int x = curX + curPiece.x(i); // x-Koordinate bestimmen
                int y = curY - curPiece.y(i); // y-Koordinate bestimmen
                // Quadrat der aktuellen Form zeichnen
                drawSquare(g, x * squareWidth(),
                        boardTop + (BOARDHEIGHT - y - 1) * squareHeight(), curPiece.getShape());
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        doDrawing(g);
    }

    // Auf Knopfdruck die Form direkt zumBoden des Spielfeldes fallen lassen (hier mit der Leertaste)
    private void dropDown() throws IOException {

        int newY = curY; // Wert für neuen y-Wert speichern → dieser ist am Anfang gleich dem aktuellen Wert

        // Schleife die so lange läuft bis die Form am Boden ist
        while (newY > 0) {

            // wenn tryMove-Methode false ist, Schleife abbrechen
            if (!tryMove(curPiece, curX, newY - 1)) {
                break;
            }
            --newY;
        }
        // Methode, wenn Form sich nicht mehr bewegt
        pieceDropped();
    }

    // Beschleunigte, gesteuerte Bewegung nach unten (wenn Pfeil unten Taste gedrückt wird)
    private void oneLineDown() throws IOException {
        // überprüfe, ob sich der Stein nicht mehr bewegt
        if (!tryMove(curPiece, curX, curY - 1)) {
            pieceDropped(); // Methode, wenn sich Form nicht mehr bewegt
        }
    }

    /*
    Die clearBoard Methode füllt das Spielbrett mit leeren Formen auf.
    Dies wir später für die Kollisionskontrolle benötigt.
     */
    private void clearBoard() {

        for (int i = 0; i < BOARDHEIGHT * BOARDWIDTH; ++i) {
            board[i] = Tetrominoes.NoShape;
        }
    }

    // Diese Methode wird aufgerufen, wenn eine Form seine Bewegung beendet hat
    private void pieceDropped() throws IOException {
        // Die gefallene Form in ds SpielfeldArray speichern
        for (int i = 0; i < 4; ++i) {
            // die Positionen der gefallenen Tertrisform als Quadrate in eine Variable speichern
            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            // y = 0 - 12 = Reihe 0 y 13 - 25 = Reihe 1 usw.
            // die x-Werte ergeben dann die einzelnen Positionen innerhalb der Reihe.
            // jeder Spielstein bekommt eine identifizierbare ID innerhalb des Spielfeldes.
            board[(y * BOARDWIDTH) + x] = curPiece.getShape(); // hier speichern wir die 2D Form in eine 1D Liste
        }
        // Methode zum Entfernen von vollen Reihen
        removeFullLines();
        // Wenn das Fallen beendet ist, eine neuen Tetrisstein ausgeben
        if (!isFallingFinished) {
            newPiece();
        }
    }

    // Methode zur Ausgabe einer neuen Tetrisform
    private void newPiece() throws IOException {

        curPiece.setRandomShape(); // eine zufällige Form aus dem TetrominoesArray ziehen
        curX = BOARDWIDTH / 2 + 1; // initial xPosition auf der Hälfte der xAchse speichern
        curY = BOARDHEIGHT - 1 + curPiece.minY(); // initial yPosition am oberen Spielfeldrand, an dem untersten Quadrat ausgeben
        // Wenn die neue Form nicht mehr bewegt werden kann, oder nicht mehr vollständig platziert werden kann, ist das Spiel beendet
        if (!tryMove(curPiece, curX, curY)) {

            curPiece.setShape(Tetrominoes.NoShape); // NoShape als aktuelle Form ausgeben
            timer.stop(); // Timer stoppen → es findet keine Animation mehr statt -> actionPerformed-Methode wird nicht mehr ausgelöst
            isStarted = false; // isStarted auf false setzen, um Spiel zu beenden
            statusbar.setText("Game Over   Punkte: " + punkte + "       HSC: " + highscore); // Text in der Statusbar für Game Over ausgeben
            isHighscoreBeaten(); // Methode zur Überprüfung, ob der aktuelle Highscore geschlagen wurde
        }
    }

    private void isHighscoreBeaten() throws IOException {
        if (punkte > highscore) {

            BufferedWriter writer = new BufferedWriter(new
                    FileWriter("Dateien/texte/highscore"));
            highscore = punkte;
            String sHighscore = String.valueOf(highscore);
            writer.write(sHighscore);
            writer.close();
            reader.close();
            return;
        }
    }

    // Methode zur Überprüfung, ob sich die Form noch bewegen kann → Methode zur Kollisionsabfrage
    private boolean tryMove(Shape newPiece, int newX, int newY) { // Form, x-Position, y-Position
        // Überprüfen, ob ein Quadrat einer mit dem Boden oder einer anderen Form kollidiert
        for (int i = 0; i < 4; ++i) {
            // x und y-Position bestimmen
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);
            // überprüfen, ob aktuelle Form sich an einem Spielfeldrand befindet
            if (x < 0 || x >= BOARDWIDTH || y < 0 || y >= BOARDHEIGHT) {
                return false;
            }

            // Überprüfen, ob aktuelle Form mit einer anderen Form kollidiert
            if (shapeAt(x, y) != Tetrominoes.NoShape) {
                return false;
            }
        }
        // Wenn alle Quadrate der aktuellen bewegungsfähig sind
        curPiece = newPiece; // auf gleiches Objekt zeigen
        curX = newX; // auf gleiche x-Position zeigen
        curY = newY; // auf gleiche y-Position zeigen

        repaint(); // mit aktualisierten Koordinaten die paintComponent-Methode aufrufen

        return true;
    }

    //Methode zum Entfernen von vollen Reihen
    private void removeFullLines() {
        //Variable die die Anzahl  der vollen Reihen speichert
        int numberFullLines = 0;
        //Reihen von oben nach unten durchlaufen
        for (int i = BOARDHEIGHT-1; i >= 0; --i) {
            boolean lineISFull = true; //Variable die prueft, ob es sich um eine volle Reihe handelt
            //Reihe von links nach rechts durchlaufen und überprüfen ob sich an der Stelle eine noshapefor
            for (int j = 0; j < BOARDWIDTH; ++j) {
                if (shapeAt(j, i) == Tetrominoes.NoShape) { //Wenn an dieser Stelle eine Noshape
                    lineISFull = false;
                    break;
                }
            }
                // Wenn es sich um eine volle REihe handelt
                if (lineISFull) {
                    ++numberFullLines;//
                    for (int k = i; k < BOARDHEIGHT - 1; ++k) {
                        for (int j = 0; j < BOARDWIDTH; ++j) {//Reihen von links nach rechts durchgehen
                            //Eindimensionales Array = Zweidimensionales Array
                            board[(k * BOARDWIDTH) + j] = shapeAt(j, k + 1);

                        }

                    }

                }
            }


        // wenn die Variable für volle Reihen größer 0 ist
        if (numberFullLines > 0) {
            numLinesRemoved += numberFullLines;
            playerPoints(numberFullLines);
            statusbar.setText("PKt:" + punkte + " Level" + level + "   HSC" + highscore);
            isFallingFinished = true; // bestimmen, das das Fallen abgeschlossen ist
            curPiece.setShape(Tetrominoes.NoShape);
            repaint();// zurückspringen in die Paintcomponent Methode
        }


    }

    /**
     * Jede Form wird aus einzelnen Quadrate hergestellt
     * In der drawSquare Methode zeichen wir die einzelnen Quadrate
     */
    private void drawSquare(@NotNull Graphics g, int x, int y, Tetrominoes shape) {
        //Liste mit Farben für die einzelnen Formen
        Color[] colors = {new Color(0, 0, 0), new Color(204, 102, 102),
                new Color(102, 204, 102), new Color(102, 102, 204),
                new Color(204, 204, 102), new Color(204, 102, 204),
                new Color(102, 204, 204), new Color(210, 170, 0)};
        //Jedem Element ein Wert aus der anderen Liste zugeordnet
        Color color = colors[shape.ordinal()];
        //dem gewählten Quadrat jeweilige Farbe setzen und ausfüllen
        g.setColor(color);
        g.fillRect(x, y, squareWidth() - 2, squareHeight() - 2);
        //an der linken und oberen Seite heller ausgeben
        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);//zeichnen der linken Linie die vertikal läuft
        g.drawLine(x, y, x + squareWidth() - 1, y);//zeichnen der oberen Linie, die horizontal läuft
        //an der rechten und unteren Seite die Farbe etwas dunkler ausgeben
        g.setColor(color.darker());
        //Untere Linie
        g.drawLine(x + 1, y + squareHeight(), x + squareWidth(), y + squareWidth());
        //Rechte Seitelinie
        g.drawLine(x + squareWidth() - 1, y + squareHeight(), x + squareWidth() - 1, y + 1);


    }

    public void levelUP() {
        if (numLinesRemoved > 10 && numLinesRemoved < 20) {
            timer.setDelay(300);
            level = 2;
        } else if (numLinesRemoved >= 20 && numLinesRemoved < 30) {
            timer.setDelay(250);
            level = 3;
        } else if (numLinesRemoved >= 30 && numLinesRemoved < 40) {
            timer.setDelay(200);
            level = 3;
        } else if (numLinesRemoved >= 40 && numLinesRemoved < 50) {
            timer.setDelay(150);
            level = 4;
        } else if (numLinesRemoved >= 50 && numLinesRemoved < 60) {
            timer.setDelay(100);
            level = 5;
        }

    }

    /**
     * @param numberFullLines
     */
    private void playerPoints(int numberFullLines) {
        if (level <= 1) {
            if (numberFullLines == 4) {
                punkte += numberFullLines * 4;
            } else {
                punkte += numberFullLines;
            }
        } else if (level < 3) {
            if (numberFullLines == 4) {
                punkte += numberFullLines * 4;
            } else {
                punkte += numberFullLines * 2;
            }
        } else if (level < 4) {
            if (numberFullLines == 4) {

                punkte += numberFullLines * 6;
            } else {
                punkte += numberFullLines * 3;
            }
        } else if (level < 5) {
            if (numberFullLines == 4) {

                punkte += numberFullLines * 8;
            } else {
                punkte += numberFullLines * 5;
            }
        } else if (level < 6) {
            if (numberFullLines == 4) {

                punkte += numberFullLines * 10;
            } else {
                punkte += numberFullLines * 5;
            }
        } else if (level < 7) {
            if (numberFullLines == 4) {

                punkte += numberFullLines * 12;
            } else {
                punkte += numberFullLines * 6;
            }
        }
    }

    class TAdapter extends KeyAdapter{

        public void keyPressed(KeyEvent e){
            //wenn sich die Form um eine NoShape handelt dann Methode beenden
            if(!isStarted || curPiece.getShape() == Tetrominoes.NoShape){
                return;
            }
            //Variable die die Eingabe der Tastatur speichert
            int  key = e.getKeyCode();
            //Wenn die Taste[p] Taste gedrückt wurde, dann soll das Spiel die Pause Methode aufrufen
            if(key == 'p' || key =='P'){
                pause();
                return;
            }
            //Solange Pause läuft, kann keine weiter Eingabe außer p erfolgen
            if(isPaused){
                return;
            }

            //Tasten zur Bewegung der Formen
            switch (key){
                case KeyEvent.VK_LEFT -> tryMove(curPiece,curX-1, curY);
                case KeyEvent.VK_RIGHT -> tryMove(curPiece,curX+1,curY);
                case KeyEvent.VK_DOWN -> {
                    try {
                        oneLineDown();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                case KeyEvent.VK_A -> tryMove(curPiece.rotateLeft(),curX,curY);
                case KeyEvent.VK_B -> tryMove(curPiece.rotateRight(),curX,curY);
                case KeyEvent.VK_SPACE -> {
                    try {
                        dropDown();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                }



            }

        }

    }



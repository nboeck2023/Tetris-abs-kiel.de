package tetris;

/*
Hier werden Informationen über die Tetris-Stücke bereitgestellt.
 */

import java.util.Random;

public class Shape {
    // Namen definieren für die 7 Tetrisformen + einer leeren Form
    protected enum Tetrominoes { NoShape, SShape, ZShape, LineShape,
                TShape, SquareShape, MirroredLShape, LShape};

    private Tetrominoes pieceShape; // Name unter dem wir die Liste ansprechen können
    private int[][] coords; // Array für die eigentlichen Koordinaten der Tetrisformen
    private int[][][] coordsTable; // 3 dimensionales Array für alle Formen
    // Konstruktor
    public Shape() {

       coords = new int[4][2]; // inneres Array Größe 2, dieses darf 4 Mal in das äußere Array
       setShape(Tetrominoes.NoShape); // als erste Form soll eine Leer-Form erstellt werden
    }
    // Setter Methode für die Tetrisform
    protected void setShape(Tetrominoes shape) {
        // Alle mögliche Position der Quadrate unsere einzelnen Tetrisformen.
        // Jede Form erhält ihre Koordinaten aus diesem Array.
        // Das äußerste Array ist der Name aus dem enum für die Form.
        // Das mittlere Array ist die Anzahl an Quadraten, die eine Form ausmacht.
        // Das innere Array ist der jeweilige Mittelpunkt eines Quadrats.
        coordsTable = new int[][][] {
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}}, // NoShape
                {{0, -1}, {0, 0}, {-1, 0}, {-1, 1}}, // SShape
                {{0, -1}, {0, 0}, {1, 0}, {1, 1}}, // ZShape
                {{0, -1}, {0, 0}, {0, 1}, {0, 2}}, // LineShape
                {{-1, 0}, {0, 0}, {1, 0}, {0, 1}}, // TShape
                {{0, 0}, {1, 0}, {0, 1}, {1, 1}}, // SquareShape
                {{-1, -1}, {0, -1}, {0, 0}, {0, 1}}, // MirroredLShape
                {{1, -1}, {0, -1}, {0, 0}, {0, 1}} // LShape
        };
        // Eine Reihe des coordsTable nehmen und in die Variable pieceShape speichern → die Auswahl EINER Form
        for (int i = 0; i < 4; i++) { // Mittlere Array durchlaufen

            for (int j = 0; j < 2; ++j) { // Inneres Array durchlaufen
                coords[i][j] = coordsTable[shape.ordinal()][i][j];
            }
        }
        pieceShape = shape;
    }
    // Setter-Methoden
    private void setX(int index, int x) {
        // Kontrolle der Bewegung entlang der x-Achse
        coords[index][0] = x;
    }
    private void setY(int index, int y) {
        // Kontrolle der Bewegung entlang der y-Achse
        coords[index][1] = y;
    }
    // Getter-Methode
    public int x(int index) { // wenn fertig programmiert Methode auf getX benannt
        return coords[index][0];
    }
    public int y(int index){
        return coords[index][1];
    }
    public Tetrominoes getShape() {
        return pieceShape;
    }

    public void setRandomShape() {

        Random r = new Random();
        int x = Math.abs(r.nextInt()) % 7 + 1; // zufällige Zahl zwischen 1 und 7 bestimmen
        Tetrominoes[] values = Tetrominoes.values(); // enum Liste, die wir speichern
        setShape(values[x]); // Form mit eder Zufallszahl x bestimmt wird
    }

    // Methoden für den kleinsten Y bzw X Wert aus coords-Array
    public int minX() {

        int m = coords[0][0];

        for (int i = 0; i < 4; i++) {
            m = Math.min(m, coords[i][0]);
        }
        return m;
    }

    public int minY() {

        int m = coords[0][1];

        for (int i = 0; i < 4; i++) {
            m = Math.min(m, coords[i][1]);
        }
        return m;
    }

    // Methode für die Linksdrehung der Form
    public Shape rotateLeft() {
        // Wenn es sich um die SquareShape handelt → nichts tun
        if (pieceShape == Tetrominoes.SquareShape) {
            return this; // soll die gleiche Form wieder zurückgeben
        }

        // Objekt als Kopie für die gedrehte Form
        Shape result = new Shape();
        result.pieceShape = pieceShape;
        // Form drehen
        for (int i = 0; i < 4; ++i) {

            result.setX(i, y(i)); // y-Wert ist nun der neue x-Wert
            result.setY(i, -x(i));// -x-Wert ist der neue y-Wert
        }
        return result;
    }
    // Methode für die Rechtsdrehung der Form
    public Shape rotateRight() {
        // Wenn die Form die SquareShape ist → nichts tun
        if (pieceShape == Tetrominoes.SquareShape) {
            return this; // das gleiche Objekt zurückgeben
        }
        // Eine Kopie des zu drehenden Objekts erstellen
        Shape result = new Shape();
        result.pieceShape = pieceShape;
        for (int i = 0; i < 4; ++i) {

            result.setX(i, -y(i)); // -y-Wert ist der neue x-Wert
            result.setY(i, x(i)); // x-Wert ist der neue y-Wert
        }
        return result; // gedrehte Form zurückgeben
    }
}

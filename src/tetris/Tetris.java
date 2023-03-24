package tetris;

import java.awt.BorderLayout;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/*
Den Bildschirm aufbauen mit Spielbrett und einer Statusbar
 */
public class Tetris extends JFrame {

    private JLabel statusbar;

    // Konstruktor
    public Tetris () throws IOException {

        initUI();
    }

    private void initUI() throws IOException {

        statusbar = new JLabel(" 0");
        add(statusbar, BorderLayout.SOUTH);
        Board board = new Board(this); // Das Spielbrettobjekt erstellt
        add(board); // Spielbrett hinzufügen
        board.start(); // Spiel starten

        setSize(225, 425);
        setTitle("Tetris the Game");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public JLabel getStatusbar() {
        return statusbar;
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            Tetris game;
            try {
                game = new Tetris();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            game.setVisible(true);
        });
    }
}

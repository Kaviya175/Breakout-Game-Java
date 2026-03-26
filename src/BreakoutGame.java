import javax.swing.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.Scanner;
import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import javax.sound.sampled.*;
import java.io.File;


public class BreakoutGame extends JPanel implements ActionListener, KeyListener {

    // Screen
    private final int WIDTH = 600;
    private final int HEIGHT = 600;

    // Game loop
    javax.swing.Timer timer = new javax.swing.Timer(10, this);

    // Paddle
    int paddleX = 250;
    int paddleWidth = 100;

    // Ball
    int ballX = 300, ballY = 400;
    int ballXDir = 1, ballYDir = -2;
    //boolean running = false;
    //boolean paused = false;
    Clip clip;
    boolean musicOn = false;

    private void playMusic() {
        try {
            File file = new File("background.wav"); 
            if (file.exists()) {
                AudioInputStream audio = AudioSystem.getAudioInputStream(file);
                clip = AudioSystem.getClip();
                clip.open(audio);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                musicOn = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Bricks
    int rows = 6, cols = 6;
    int[][] bricks = new int[rows][cols];

    // Game state
    boolean running = false;
    boolean paused = false;
    int score = 0;
    String playerName = "";

    // Leaderboard
    HashMap<String, Integer> scores = new HashMap<>();
    String fileName = "scores.txt";

    // Constructor
    public BreakoutGame() {
        setFocusable(true);
        addKeyListener(this);
        loadScores();
        initBricks();
        getPlayerName();
        playMusic();
        timer.start();

    }

    // Initialize bricks
    private void initBricks() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i < 2) bricks[i][j] = 3;
                else if (i < 4) bricks[i][j] = 2;
                else bricks[i][j] = 1;
            }
        }
    }

    // Draw everything
    public void paint(Graphics g) {
        // Background
        g.setColor(new Color(234, 218, 184));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Title / Start screen
        if (!running) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("BREAKOUT", 200, 200);

            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press ENTER to Start", 190, 250);
            drawLeaderboard(g);
            return;
        }

        // Paddle
        g.setColor(Color.GRAY);
        g.fillRect(paddleX, 550, paddleWidth, 10);

        // Ball
        g.setColor(Color.BLACK);
        g.fillOval(ballX, ballY, 15, 15);

        // Bricks
        drawBricks(g);

        // Score
        g.drawString("Score: " + score, 10, 20);

        // Pause
        if (paused) {
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("PAUSED", 230, 300);
        }

        drawLeaderboard(g);
    }

    // Draw bricks
    private void drawBricks(Graphics g) {
        int brickWidth = WIDTH / cols;
        int brickHeight = 40;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (bricks[i][j] > 0) {
                    if (bricks[i][j] == 3) g.setColor(Color.BLUE);
                    else if (bricks[i][j] == 2) g.setColor(Color.GREEN);
                    else g.setColor(Color.RED);

                    g.fillRect(j * brickWidth, i * brickHeight, brickWidth, brickHeight);
                    g.setColor(Color.BLACK);
                    g.drawRect(j * brickWidth, i * brickHeight, brickWidth, brickHeight);
                }
            }
        }
    }

    // Game loop
    public void actionPerformed(ActionEvent e) {
        if (!running || paused) return;

        ballX += ballXDir;
        ballY += ballYDir;

        // Wall collision
        if (ballX <= 0 || ballX >= WIDTH - 15) ballXDir *= -1;
        if (ballY <= 0) ballYDir *= -1;

        // Paddle collision
        Rectangle ballRect = new Rectangle(ballX, ballY, 15, 15);
        Rectangle paddleRect = new Rectangle(paddleX, 550, paddleWidth, 10);

        if (ballRect.intersects(paddleRect)) {
            ballYDir *= -1;
        }

        // Brick collision
        int brickWidth = WIDTH / cols;
        int brickHeight = 40;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (bricks[i][j] > 0) {
                    Rectangle brickRect = new Rectangle(j * brickWidth, i * brickHeight, brickWidth, brickHeight);

                    if (ballRect.intersects(brickRect)) {
                        bricks[i][j]--;
                        ballYDir *= -1;
                        score += 10;
                    }
                }
            }
        }

        // Game Over
        if (ballY > HEIGHT) {
            running = false;
            saveScore();
            resetGame();
        }

        repaint();
    }

    // Keyboard controls
    public void keyPressed(KeyEvent e) {
        int paddleSpeed = 30; // add at top

        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            paddleX -= paddleSpeed;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            paddleX += paddleSpeed;
        }
        if (e.getKeyCode() == KeyEvent.VK_M) {
            if (clip != null) {   // ✅ ADD THIS CHECK
                if (musicOn) {
                    clip.stop();
                    musicOn = false;
                } else {
                    clip.start();
                    musicOn = true;
                }
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            running = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_P) {
            paused = !paused;
        }
        //Boundary fix
        if (paddleX < 0) paddleX = 0;
        if (paddleX > 600 - paddleWidth) paddleX = 600 - paddleWidth;
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    // Reset game
    private void resetGame() {
        ballX = 300;
        ballY = 400;
        score = 0;
        initBricks();
    }

    // Player name input
    private void getPlayerName() {
        playerName = JOptionPane.showInputDialog(this, "Enter your name:");
        if (playerName == null || playerName.isEmpty()) {
            playerName = "Player";
        }
    }

    // Load scores
    private void loadScores() {
        try {
            File file = new File(fileName);
            if (!file.exists()) return;

            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String[] data = sc.nextLine().split(",");
                scores.put(data[0], Integer.parseInt(data[1]));
            }
            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Save score
    private void saveScore() {
        scores.put(playerName, Math.max(scores.getOrDefault(playerName, 0), score));

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            for (String name : scores.keySet()) {
                writer.write(name + "," + scores.get(name));
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Draw leaderboard
    private void drawLeaderboard(Graphics g) {
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Leaderboard:", 400, 20);

        java.util.List<Map.Entry<String, Integer>> list = new ArrayList<>(scores.entrySet());
        list.sort((a, b) -> b.getValue() - a.getValue());

        for (int i = 0; i < Math.min(3, list.size()); i++) {
            g.drawString((i + 1) + ". " + list.get(i).getKey() + ": " + list.get(i).getValue(),
                    400, 40 + (i * 20));
        }
    }

    // Main method
    public static void main(String[] args) {
        JFrame frame = new JFrame("Breakout Game");
        BreakoutGame game = new BreakoutGame();

        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(game);
        frame.setVisible(true);
    }
}

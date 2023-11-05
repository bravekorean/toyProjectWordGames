package WordGames;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;

public class FallingWordFrame extends JFrame {
    private GamePanel gamePanel = null;
    private ControlPanel controlPanel = null;

    FallingWordFrame() {
        setTitle("단어게임");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container c = getContentPane();
        gamePanel = new GamePanel();
        controlPanel = new ControlPanel(gamePanel);
        c.add(gamePanel, BorderLayout.CENTER);
        c.add(controlPanel, BorderLayout.NORTH);
        setVisible(true);

        gamePanel.startGame();
    }

    class ControlPanel extends JPanel {
        private GamePanel gamePanel;
        private JTextField input = new JTextField(15);
        private JLabel scoreLabel = new JLabel("점수: 0");

        public ControlPanel(GamePanel gamePanel) {
            this.gamePanel = gamePanel;
            this.setLayout(new BorderLayout());
            input.setHorizontalAlignment(JTextField.LEFT);
            add(input, BorderLayout.WEST);
            add(scoreLabel, BorderLayout.CENTER);
            JButton exitButton = new JButton("종료");
            exitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    gamePanel.stopGame();
                }
            });
            add(exitButton, BorderLayout.EAST);
            input.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JTextField tf = (JTextField) e.getSource();
                    String text = tf.getText();
                    if (text.equals(""))
                        System.exit(0);

                    if (!gamePanel.isGameOn())
                        return;

                    boolean match = gamePanel.matchWord(text);
                    if (match) {
                        gamePanel.removeWord(text);
                        tf.setText("");
                        controlPanel.updateScore(1);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.GREEN);
            g.fillRect(0, 0, getWidth(), getHeight());
        } // 원래 jar파일에 맞춰서 작업중이었으나 전체 코드를 수정해야해서 시간상 그냥 영역 꽉채우서ㅓ 사용함 

        public void updateScore(int points) {
            String currentText = scoreLabel.getText();
            String[] parts = currentText.split(": ");
            int currentScore = Integer.parseInt(parts[1]);
            currentScore += points;
            scoreLabel.setText("점수: " + currentScore); // 원래 기존 패널에서 다 처리하고 있었으나 ... 
            // 하나만 맞추고 나서 교착상태인지 뭔지 수많은 오류 로그와 함께 문제가 생겨 따로 변경했음 ..
        }
    }

    class GamePanel extends JPanel {
        private Vector<FallingWordThread> threads = new Vector<>();
        private ImageIcon icon;
        private Image img;
        private ScheduledExecutorService executorService;
        private Random random = new Random();
        private Words wordHandler = new Words("./resource/words.txt");

        public GamePanel() {
            setLayout(null);
            icon = new ImageIcon("./images/bg.jpg");
            img = icon.getImage();
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
        }

        public void startGame() {
            executorService = Executors.newScheduledThreadPool(1); // 스케줄링 활용 활용은 했으나 이해를 못했음 ...
            executorService.scheduleWithFixedDelay(this::addWord, 700, 2000, TimeUnit.MILLISECONDS);
            // 단어 생성을 0.7초 지연하고, 2초 간격으로 단어를 생성함. 그래서 생성된 단어가 추가되서 내려옴.
        }

        public void stopGame() {
            executorService.shutdownNow(); // 단어 생성 중단
            for (FallingWordThread thread : threads) {
                thread.stopFalling(); // 실행된 모든 스레드 작업중지 
            }
        }

        public void addWord() {
            String newWord = wordHandler.getRandomWord();

            JLabel label = new JLabel();
            label.setText(newWord);
            label.setSize(200, 30);
            label.setLocation(random.nextInt(getWidth() - 100), 0);
            label.setForeground(Color.YELLOW);
            label.setFont(new Font("Tahoma", Font.ITALIC, 20));

            add(label);
            FallingWordThread thread = new FallingWordThread(label);
            thread.start();
            threads.add(thread); // 한꺼번에 관리하기 위해 벡터에 담음 
        }

        public void removeWord(String word) {
            Component[] components = getComponents();
            for (Component component : components) {
                if (component instanceof JLabel && ((JLabel) component).getText().equals(word)) {
                    remove(component);
                    break;
                }
            }
            revalidate();
            repaint(); // 입력된 단어가 맞을 때, 그 단어를 지우는 작업 
        }

        public boolean isGameOn() {
            return !threads.isEmpty(); // 스레드들이 활동하지않을 때, 게임의 상태는 false이다 
        }

        public boolean matchWord(String text) {
            for (FallingWordThread thread : threads) {
                if (thread.getWord().equals(text)) {
                    return true;
                } // 맞는지 검증하는 작업 
            }
            return false;
        }

        class FallingWordThread extends Thread {
            private JLabel label;
            private boolean falling = true;

            public FallingWordThread(JLabel label) {
                this.label = label;
            }

            public void run() {
                while (falling) {
                    try {
                        Thread.sleep(200);
                        int y = label.getY() + 5;
                        label.setLocation(label.getX(), y);
                        repaint();
                        if (y >= getHeight()) {
                            falling = false;
                            removeWord(label.getText());
                            break;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            public String getWord() {
                return label.getText();
            } // 기존의 startGame에서 다 처리하려고했으나, 스레드 처리를 위해 따로 분할함 

            public void stopFalling() {
                falling = false;
            }
        }
    }
	

	
	public static void main(String[] args) {
		new FallingWordFrame();
	}
}


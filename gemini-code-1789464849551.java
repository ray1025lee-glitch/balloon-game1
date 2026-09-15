import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class BalloonGame extends JFrame {
    private final ArrayList<Balloon> balloons = new ArrayList<>();
    private final Random random = new Random();
    private final int GRAVITY_Y = 15; // 視窗頂部停留高度

    public BalloonGame() {
        setTitle("飄浮氣球小遊戲（Java 桌面版）");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // 自定義畫布面板
        GamePanel canvas = new GamePanel();
        add(canvas);

        // 初始生成 10 顆氣球
        Timer initTimer = new Timer(100, e -> {
            ((Timer) e.getSource()).stop();
            for (int i = 0; i < 10; i++) {
                int startX = random.nextInt(700) + 50;
                int startY = random.nextInt(200) + 350;
                balloons.add(new Balloon(startX, startY));
            }
        });
        initTimer.start();

        // 遊戲主循環計時器 (大約 60 FPS)
        Timer gameTimer = new Timer(16, e -> {
            updateGame();
            canvas.repaint();
        });
        gameTimer.start();

        setVisible(true);
    }

    private void updateGame() {
        for (Balloon b : balloons) {
            b.update();
        }
    }

    // 氣球物件類別
    private class Balloon {
        double x, y;
        double vx, vy;
        Color color;
        boolean isStopped = false;
        double bounceTimer = 0;
        double floatAngle;
        double scaleX = 1.0, scaleY = 1.0;
        ArrayList<Particle> particles = null;

        public Balloon(int startX, int startY) {
            this.x = startX;
            this.y = startY;
            this.vx = (random.nextDouble() - 0.5) * 1.5;
            this.vy = -2.0 - random.nextDouble() * 2.0;
            this.floatAngle = random.nextDouble() * Math.PI * 2;
            
            // 隨機顏色
            Color[] colors = {
                Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, 
                Color.PINK, Color.CYAN, Color.ORANGE, Color.MAGENTA
            };
            this.color = colors[random.nextInt(colors.length)];
        }

        public void update() {
            if (!isStopped) {
                x += vx;
                y += vy;

                // 左右邊界限制
                if (x < 0) { x = 0; vx *= -1; }
                if (x > 735) { x = 735; vx *= -1; }

                // 檢查是否到達頂端
                if (y <= GRAVITY_Y) {
                    y = GRAVITY_Y;
                    isStopped = true;
                    bounceTimer = 0;
                }
            } else {
                // 頂端彈跳與左右漂浮
                bounceTimer += 0.15;
                floatAngle += 0.03;
                x += Math.sin(floatAngle) * 0.8;

                if (x < 0) x = 0;
                if (x > 735) x = 735;

                if (bounceTimer < Math.PI) {
                    scaleY = 1.0 - Math.sin(bounceTimer) * 0.15;
                    scaleX = 1.0 + Math.sin(bounceTimer) * 0.1;
                } else {
                    scaleX = 1.0;
                    scaleY = 1.0;
                }
            }
        }

        public boolean contains(int px, int py) {
            // 氣球判定範圍 (寬 50, 高 65)
            return px >= x && px <= x + 50 && py >= y && py <= y + 65;
        }
    }

    // 破裂小泡泡類別
    private class Particle {
        double x, y, dx, dy;
        double alpha = 1.0;

        public Particle(double x, double y) {
            this.x = x;
            this.y = y;
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = 20 + random.nextDouble() * 30;
            this.dx = Math.cos(angle) * dist;
            this.dy = Math.sin(angle) * dist;
        }

        public void update() {
            x += dx * 0.1;
            y += dy * 0.1;
            alpha -= 0.05;
        }
    }

    private final ArrayList<Particle> globalParticles = new ArrayList<>();

    // 畫布面板
    private class GamePanel extends JPanel {
        public GamePanel() {
            setBackground(new Color(135, 206, 235)); // 天藍色背景

            addMouseListener(new MouseAdapter() {
                @Override
                public void MousePressed(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        // 左鍵點擊：生成氣球
                        if (e.getY() > 50 && e.getY() < 530) {
                            balloons.add(new Balloon(e.getX() - 25, e.getY() - 32));
                        }
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        // 右鍵點擊：戳破氣球
                        Iterator<Balloon> it = balloons.iterator();
                        while (it.hasNext()) {
                            Balloon b = it.next();
                            if (b.contains(e.getX(), e.getY())) {
                                // 產生泡泡粒子
                                for (int i = 0; i < 8; i++) {
                                    globalParticles.add(new Particle(b.x + 25, b.y + 32));
                                }
                                it.remove();
                                break;
                            }
                        }
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 繪製所有氣球
            synchronized (balloons) {
                for (Balloon b : balloons) {
                    g2d.setColor(b.color);
                    int drawW = (int) (50 * b.scaleX);
                    int drawH = (int) (65 * b.scaleY);
                    int drawX = (int) (b.x + (50 - drawW) / 2);
                    int drawY = (int) (b.y + (65 - drawH) / 2);

                    // 畫氣球本體
                    g2d.fillOval(drawX, drawY, drawW, drawH);

                    // 畫氣球結點與線
                    g2d.fillPolygon(
                        new int[]{drawX + 22, drawX + 28, drawX + 25}, 
                        new int[]{drawY + drawH, drawY + drawH, drawY + drawH + 6}, 
                        3
                    );
                    g2d.setColor(new Color(0, 0, 0, 100));
                    g2d.drawLine(drawX + 25, drawY + drawH + 6, drawX + 25, drawY + drawH + 22);
                }
            }

            // 繪製破裂泡泡粒子
            Iterator<Particle> pit = globalParticles.iterator();
            while (pit.hasNext()) {
                Particle p = pit.next();
                p.update();
                if (p.alpha <= 0) {
                    pit.remove();
                } else {
                    g2d.setColor(new Color(255, 255, 255, Math.max(0, (int) (p.alpha * 255))));
                    g2d.fillOval((int) p.x, (int) p.y, 8, 8);
                }
            }

            // 畫下方提示文字
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2d.drawString("左鍵：生成氣球 | 右鍵：戳破氣球", 280, 545);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BalloonGame::new);
    }
}
import java.net.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class Client extends JFrame {

    Socket socket;
    BufferedReader br;
    PrintWriter out;

    private JPanel chatPanel;
    private JScrollPane scrollPane;
    private JTextField messageInput;
    private JButton sendButton;

    public Client() {
        try {
            socket = new Socket("127.0.0.1", 7777);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());

            createGUI();
            handleEvents();
            startReading();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🌟 Custom Bubble (REAL rounded filled bubble)
    class Bubble extends JPanel {
        private String text;
        private Color bgColor;

        Bubble(String text, Color bgColor) {
            this.text = text;
            this.bgColor = bgColor;
            setOpaque(false);
            setLayout(new BorderLayout());

            JLabel label = new JLabel("<html><body style='width: 160px'>" + text + "</body></html>");
            label.setForeground(Color.BLACK);
            label.setFont(new Font("Arial", Font.PLAIN, 14));
            label.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));

            if(bgColor.getRGB() < -10000000){ // dark bg → white text
                label.setForeground(Color.WHITE);
            }

            add(label, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

            super.paintComponent(g);
        }
    }

    private void createGUI() {
        setTitle("Chat 💬");
        setSize(450, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 🩶 Chat area
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(new Color(235, 235, 235));

        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setBorder(null);

        // 🔻 Input area
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(220, 220, 220));

        messageInput = new JTextField();
        messageInput.setFont(new Font("Arial", Font.PLAIN, 14));
        messageInput.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));

        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(0, 132, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);

        bottomPanel.add(messageInput, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void handleEvents() {
        sendButton.addActionListener(e -> sendMessage());

        messageInput.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
    }

    private void sendMessage() {
        String msg = messageInput.getText().trim();
        if (msg.equals("")) return;

        addBubble(msg, true);

        out.println(msg);
        out.flush();

        messageInput.setText("");
        messageInput.requestFocus();
    }

    private void addBubble(String message, boolean isRight) {

        JPanel wrapper = new JPanel(new FlowLayout(
                isRight ? FlowLayout.RIGHT : FlowLayout.LEFT, 2, 1 // 🔥 reduced gaps
        ));
        wrapper.setBackground(new Color(235, 235, 235));

        Color clientColor = new Color(0, 132, 255);           // blue
        Color serverColor = new Color(210, 220, 235);         // bluish grey

        Bubble bubble = new Bubble(message, isRight ? clientColor : serverColor);

        wrapper.add(bubble);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrapper.getPreferredSize().height));
        chatPanel.add(wrapper);

        chatPanel.revalidate();

        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    public void startReading() {
        Runnable r1 = () -> {
            try {
                while (true) {
                    String msg = br.readLine();

                    if (msg == null || msg.equals("exit")) {
                        JOptionPane.showMessageDialog(this, "Server closed chat");
                        socket.close();
                        break;
                    }

                    addBubble(msg, false);
                }
            } catch (Exception e) {
                System.out.println("Connection closed");
            }
        };

        new Thread(r1).start();
    }

    public static void main(String[] args) {
        new Client();
    }
}
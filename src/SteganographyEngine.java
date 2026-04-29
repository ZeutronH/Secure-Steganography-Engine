import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SteganographyEngine extends JFrame {

    private static final String EOF_MARKER = "##END##";
    private static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static final String AES_ALGORITHM = "AES/ECB/PKCS5Padding";

    // --- PREMIUM DARK MODE PALETTE ---
    private static final Color BG_MAIN = new Color(30, 30, 34);       // Deep background
    private static final Color BG_PANEL = new Color(43, 43, 48);      // Elevated panel background
    private static final Color INPUT_BG = new Color(24, 24, 28);      // Text area background
    private static final Color TEXT_MAIN = new Color(230, 230, 235);  // Off-white text
    private static final Color TEXT_MUTED = new Color(150, 150, 160); // Gray text for borders/info

    // Vibrant Accents for Buttons
    private static final Color ACCENT_BLUE = new Color(10, 132, 255);
    private static final Color ACCENT_GREEN = new Color(48, 209, 88);
    private static final Color ACCENT_RED = new Color(255, 69, 58);
    private static final Color ACCENT_PURPLE = new Color(191, 90, 242);
    private static final Color ACCENT_GRAY = new Color(99, 99, 102);

    public SteganographyEngine() {
        setTitle("CipherCore: Secure Steganography Engine");
        setSize(850, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BG_MAIN);
        mainContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setFocusable(false);
        tabbedPane.setBackground(BG_PANEL);
        tabbedPane.setForeground(TEXT_MAIN);

        tabbedPane.addTab("  🔑 RSA Setup  ", createRsaKeyGenPanel());
        tabbedPane.addTab("  🔒 RSA Encode  ", createRsaEncodePanel());
        tabbedPane.addTab("  🔓 RSA Decode  ", createRsaDecodePanel());
        tabbedPane.addTab("  🔒 AES Encode  ", createAesEncodePanel());
        tabbedPane.addTab("  🔓 AES Decode  ", createAesDecodePanel());

        mainContainer.add(tabbedPane, BorderLayout.CENTER);
        add(mainContainer);
    }

    // ==========================================
    // UI STYLING UTILITIES (Dark Mode Magic)
    // ==========================================
    private void styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(250, 42));

        // Hover Effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bgColor.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bgColor); }
        });
    }

    private void styleTextArea(JTextArea txt) {
        txt.setFont(new Font("Monospaced", Font.PLAIN, 15));
        txt.setBackground(INPUT_BG);
        txt.setForeground(TEXT_MAIN);
        txt.setCaretColor(ACCENT_BLUE);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void stylePasswordField(JPasswordField txt) {
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txt.setBackground(INPUT_BG);
        txt.setForeground(TEXT_MAIN);
        txt.setCaretColor(ACCENT_BLUE);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 65)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
    }

    private TitledBorder createModernBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 85), 1, true), title);
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 13));
        border.setTitleColor(TEXT_MUTED);
        return border;
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }

    private JPanel createBasePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_PANEL);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return panel;
    }

    // ==========================================
    // TAB 1: RSA KEY GENERATOR
    // ==========================================
    private JPanel createRsaKeyGenPanel() {
        JPanel panel = createBasePanel();
        GridBagConstraints gbc = createGbc();

        JTextArea infoText = new JTextArea(
                "Welcome to the RSA-2048 Identity Generator.\n\n" +
                        "• Public Key (.pub): Share this freely. Senders use it to encrypt data for you.\n" +
                        "• Private Key (.key): Keep this hidden. Only you can use it to decrypt data."
        );
        infoText.setEditable(false);
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        infoText.setBackground(BG_PANEL);
        infoText.setForeground(TEXT_MAIN);

        JButton btnGenerate = new JButton("Generate & Save RSA Keys");
        styleButton(btnGenerate, ACCENT_PURPLE);

        btnGenerate.addActionListener(e -> {
            try {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(2048);
                KeyPair pair = keyGen.generateKeyPair();
                Files.write(Paths.get("RSA_Public.pub"), pair.getPublic().getEncoded());
                Files.write(Paths.get("RSA_Private.key"), pair.getPrivate().getEncoded());
                JOptionPane.showMessageDialog(this, "Keys generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridy = 0; panel.add(infoText, gbc);
        gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; panel.add(btnGenerate, gbc);
        return panel;
    }

    // ==========================================
    // TAB 2: RSA ENCODE
    // ==========================================
    private JPanel createRsaEncodePanel() {
        JPanel panel = createBasePanel();
        GridBagConstraints gbc = createGbc();

        JLabel imgLabel = new JLabel(" Cover Image: None", JLabel.LEFT);
        imgLabel.setForeground(TEXT_MUTED);
        JButton btnSelectImage = new JButton("1. Select Image (.png)");
        styleButton(btnSelectImage, ACCENT_GRAY);

        JLabel keyLabel = new JLabel(" Public Key: None", JLabel.LEFT);
        keyLabel.setForeground(TEXT_MUTED);
        JButton btnSelectKey = new JButton("2. Load Public Key (.pub)");
        styleButton(btnSelectKey, ACCENT_GRAY);

        JTextArea txtSecret = new JTextArea(4, 30);
        styleTextArea(txtSecret);
        JScrollPane scrollPane = new JScrollPane(txtSecret);
        scrollPane.setBorder(createModernBorder("3. Secret Message (Max 240 chars for RSA-2048)"));

        JButton btnProcess = new JButton("4. RSA Encrypt & Hide Data");
        styleButton(btnProcess, ACCENT_BLUE);

        final File[] selectedImg = {null}, selectedKey = {null};
        btnSelectImage.addActionListener(e -> selectedImg[0] = selectFile("PNG Images", "png", imgLabel));
        btnSelectKey.addActionListener(e -> selectedKey[0] = selectFile("Public Key", "pub", keyLabel));

        btnProcess.addActionListener(e -> {
            if (selectedImg[0] == null || selectedKey[0] == null || txtSecret.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please complete all fields."); return;
            }
            try {
                byte[] keyBytes = Files.readAllBytes(selectedKey[0].toPath());
                PublicKey pubKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
                Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, pubKey);
                byte[] encBytes = cipher.doFinal(txtSecret.getText().getBytes(StandardCharsets.UTF_8));

                BufferedImage stegoImg = embedData(ImageIO.read(selectedImg[0]), Base64.getEncoder().encodeToString(encBytes));
                File output = new File(selectedImg[0].getParent(), "RSA_Stego_" + selectedImg[0].getName());
                ImageIO.write(stegoImg, "png", output);

                JOptionPane.showMessageDialog(this, "Success! Saved as: " + output.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Encryption Failed. Message might be too long.");
            }
        });

        gbc.gridy = 0; panel.add(btnSelectImage, gbc); gbc.gridy = 1; panel.add(imgLabel, gbc);
        gbc.gridy = 2; panel.add(btnSelectKey, gbc); gbc.gridy = 3; panel.add(keyLabel, gbc);
        gbc.gridy = 4; panel.add(scrollPane, gbc); gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; panel.add(btnProcess, gbc);
        return panel;
    }

    // ==========================================
    // TAB 3: RSA DECODE
    // ==========================================
    private JPanel createRsaDecodePanel() {
        JPanel panel = createBasePanel();
        GridBagConstraints gbc = createGbc();

        JLabel imgLabel = new JLabel(" Stego Image: None", JLabel.LEFT);
        imgLabel.setForeground(TEXT_MUTED);
        JButton btnSelectImage = new JButton("1. Load Stego Image (.png)");
        styleButton(btnSelectImage, ACCENT_GRAY);

        JLabel keyLabel = new JLabel(" Private Key: None", JLabel.LEFT);
        keyLabel.setForeground(TEXT_MUTED);
        JButton btnSelectKey = new JButton("2. Load Private Key (.key)");
        styleButton(btnSelectKey, ACCENT_GRAY);

        JTextArea txtOutput = new JTextArea(4, 30);
        styleTextArea(txtOutput);
        txtOutput.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtOutput);
        scrollPane.setBorder(createModernBorder("Decrypted Message Output"));

        JButton btnProcess = new JButton("3. Extract & RSA Decrypt");
        styleButton(btnProcess, ACCENT_RED);

        final File[] selectedImg = {null}, selectedKey = {null};
        btnSelectImage.addActionListener(e -> selectedImg[0] = selectFile("PNG Images", "png", imgLabel));
        btnSelectKey.addActionListener(e -> selectedKey[0] = selectFile("Private Key", "key", keyLabel));

        btnProcess.addActionListener(e -> {
            if (selectedImg[0] == null || selectedKey[0] == null) return;
            try {
                String extractedB64 = extractData(ImageIO.read(selectedImg[0]));
                byte[] keyBytes = Files.readAllBytes(selectedKey[0].toPath());
                PrivateKey privKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
                Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, privKey);
                byte[] decBytes = cipher.doFinal(Base64.getDecoder().decode(extractedB64));
                txtOutput.setText(new String(decBytes, StandardCharsets.UTF_8));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Decryption Failed! Invalid Key or Image.");
            }
        });

        gbc.gridy = 0; panel.add(btnSelectImage, gbc); gbc.gridy = 1; panel.add(imgLabel, gbc);
        gbc.gridy = 2; panel.add(btnSelectKey, gbc); gbc.gridy = 3; panel.add(keyLabel, gbc);
        gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; panel.add(btnProcess, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridy = 5; panel.add(scrollPane, gbc);
        return panel;
    }

    // ==========================================
    // TAB 4: AES ENCODE
    // ==========================================
    private JPanel createAesEncodePanel() {
        JPanel panel = createBasePanel();
        GridBagConstraints gbc = createGbc();

        JLabel imgLabel = new JLabel(" Cover Image: None", JLabel.LEFT);
        imgLabel.setForeground(TEXT_MUTED);
        JButton btnSelectImage = new JButton("1. Select Image (.png)");
        styleButton(btnSelectImage, ACCENT_GRAY);

        JPasswordField txtPass = new JPasswordField(20);
        stylePasswordField(txtPass);

        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setBackground(BG_PANEL);
        passPanel.setBorder(createModernBorder("2. AES Secure Password"));
        passPanel.add(txtPass, BorderLayout.CENTER);

        JTextArea txtSecret = new JTextArea(4, 30);
        styleTextArea(txtSecret);
        JScrollPane scrollPane = new JScrollPane(txtSecret);
        scrollPane.setBorder(createModernBorder("3. Secret Message (Unlimited Length)"));

        JButton btnProcess = new JButton("4. AES Encrypt & Hide Data");
        styleButton(btnProcess, ACCENT_GREEN);

        final File[] selectedImg = {null};
        btnSelectImage.addActionListener(e -> selectedImg[0] = selectFile("PNG Images", "png", imgLabel));

        btnProcess.addActionListener(e -> {
            if (selectedImg[0] == null || txtPass.getPassword().length == 0 || txtSecret.getText().isEmpty()) return;
            try {
                SecretKeySpec secretKey = generateAESKey(new String(txtPass.getPassword()));
                Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                byte[] encBytes = cipher.doFinal(txtSecret.getText().getBytes(StandardCharsets.UTF_8));
                BufferedImage stegoImg = embedData(ImageIO.read(selectedImg[0]), Base64.getEncoder().encodeToString(encBytes));
                File output = new File(selectedImg[0].getParent(), "AES_Stego_" + selectedImg[0].getName());
                ImageIO.write(stegoImg, "png", output);
                JOptionPane.showMessageDialog(this, "Success! Saved as: " + output.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Encryption Failed.");
            }
        });

        gbc.gridy = 0; panel.add(btnSelectImage, gbc); gbc.gridy = 1; panel.add(imgLabel, gbc);
        gbc.gridy = 2; panel.add(passPanel, gbc); gbc.gridy = 3; panel.add(scrollPane, gbc);
        gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; panel.add(btnProcess, gbc);
        return panel;
    }

    // ==========================================
    // TAB 5: AES DECODE
    // ==========================================
    private JPanel createAesDecodePanel() {
        JPanel panel = createBasePanel();
        GridBagConstraints gbc = createGbc();

        JLabel imgLabel = new JLabel(" Stego Image: None", JLabel.LEFT);
        imgLabel.setForeground(TEXT_MUTED);
        JButton btnSelectImage = new JButton("1. Load Stego Image (.png)");
        styleButton(btnSelectImage, ACCENT_GRAY);

        JPasswordField txtPass = new JPasswordField(20);
        stylePasswordField(txtPass);

        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setBackground(BG_PANEL);
        passPanel.setBorder(createModernBorder("2. AES Decryption Password"));
        passPanel.add(txtPass, BorderLayout.CENTER);

        JTextArea txtOutput = new JTextArea(4, 30);
        styleTextArea(txtOutput);
        txtOutput.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtOutput);
        scrollPane.setBorder(createModernBorder("Decrypted Message Output"));

        JButton btnProcess = new JButton("3. Extract & AES Decrypt");
        styleButton(btnProcess, ACCENT_BLUE);

        final File[] selectedImg = {null};
        btnSelectImage.addActionListener(e -> selectedImg[0] = selectFile("PNG Images", "png", imgLabel));

        btnProcess.addActionListener(e -> {
            if (selectedImg[0] == null || txtPass.getPassword().length == 0) return;
            try {
                String extractedB64 = extractData(ImageIO.read(selectedImg[0]));
                SecretKeySpec secretKey = generateAESKey(new String(txtPass.getPassword()));
                Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                byte[] decBytes = cipher.doFinal(Base64.getDecoder().decode(extractedB64));
                txtOutput.setText(new String(decBytes, StandardCharsets.UTF_8));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Decryption Failed! Wrong password.");
            }
        });

        gbc.gridy = 0; panel.add(btnSelectImage, gbc); gbc.gridy = 1; panel.add(imgLabel, gbc);
        gbc.gridy = 2; panel.add(passPanel, gbc); gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; panel.add(btnProcess, gbc);
        gbc.gridy = 4; gbc.fill = GridBagConstraints.HORIZONTAL; panel.add(scrollPane, gbc);
        return panel;
    }

    // ==========================================
    // CORE LOGIC & UTILS
    // ==========================================
    private SecretKeySpec generateAESKey(String password) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        return new SecretKeySpec(Arrays.copyOf(sha.digest(password.getBytes(StandardCharsets.UTF_8)), 16), "AES");
    }

    private File selectFile(String desc, String ext, JLabel lbl) {
        JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
        chooser.setFileFilter(new FileNameExtensionFilter(desc, ext));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            lbl.setText(" Selected: " + f.getName());
            lbl.setForeground(ACCENT_GREEN); // Turn text green on success
            return f;
        }
        return null;
    }

    private BufferedImage embedData(BufferedImage img, String data) {
        byte[] msg = (data + EOF_MARKER).getBytes(StandardCharsets.UTF_8);
        int bitIdx = 0, total = msg.length * 8;
        BufferedImage stego = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = stego.getGraphics(); g.drawImage(img, 0, 0, null); g.dispose();
        for (int y = 0; y < stego.getHeight() && bitIdx < total; y++) {
            for (int x = 0; x < stego.getWidth() && bitIdx < total; x++) {
                int px = stego.getRGB(x, y), bit = (msg[bitIdx / 8] >> (7 - (bitIdx % 8))) & 1;
                stego.setRGB(x, y, (px & 0xFFFFFFFE) | bit); bitIdx++;
            }
        }
        return stego;
    }

    private String extractData(BufferedImage img) {
        StringBuilder txt = new StringBuilder();
        int curByte = 0, bitCnt = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                curByte = (curByte << 1) | (img.getRGB(x, y) & 1); bitCnt++;
                if (bitCnt == 8) {
                    txt.append((char) curByte);
                    if (txt.toString().endsWith(EOF_MARKER)) return txt.substring(0, txt.length() - 7);
                    curByte = 0; bitCnt = 0;
                }
            }
        }
        return "";
    }

    public static void main(String[] args) {
        // --- INJECTING GLOBAL DARK MODE INTO NIMBUS ---
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    // Force Nimbus UI to use dark colors globally (Fixes FileChooser & Dialogs)
                    UIManager.put("control", new Color(43, 43, 48));
                    UIManager.put("info", new Color(43, 43, 48));
                    UIManager.put("nimbusBase", new Color(30, 30, 34));
                    UIManager.put("nimbusLightBackground", new Color(24, 24, 28));
                    UIManager.put("text", new Color(230, 230, 235));
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> new SteganographyEngine().setVisible(true));
    }
}
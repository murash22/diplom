package windows.cbc_attack;

import utils.Conversions;
import widgets.HexTextField;
import algorithm.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class VisualizationCBCAttack extends JPanel {
    Algorithm algorithm;
    int blockSize;
    public byte[] cipherText;

    JPanel mainContainer;

    public HashMap<String, JPanel> blocks = new HashMap<>();
    Font font = new Font("Times New Roman", Font.PLAIN, 30);


    JLabel createXORLabel() {
        try {
            BufferedImage buff = ImageIO.read(Objects.requireNonNull(getClass().getResource("/files/images/xor.png")));
            ImageIcon imageIcon = new ImageIcon(buff);
            Image image = imageIcon.getImage().getScaledInstance(35, 35, Image.SCALE_DEFAULT);
            return new JLabel(new ImageIcon(image));
        } catch (IOException e) {
            e.printStackTrace();
            JLabel label = new JLabel("XOR");
            label.setFont(new Font("Times New Roman", Font.PLAIN, 25));
            return label;
        }
    }

    JPanel createBlock(String name, boolean isEditable, List<String> initialData) {
        JPanel blockContainer = new JPanel(new GridLayout(2, 1));

        JLabel title = new JLabel(name);
        title.setHorizontalAlignment(SwingUtilities.CENTER);
        title.setFont(font);
        blockContainer.add(title);
        JPanel boxes = new JPanel();
        boxes.setLayout(new BoxLayout(boxes, BoxLayout.X_AXIS));
        for (int i = 0; i < blockSize; i++) {
            HexTextField textField = new HexTextField(initialData.get(i), 1, false);
            textField.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 1), new EmptyBorder(5, 3, 5, -10)));
            textField.setFont(new Font(font.getName(), font.getStyle(), 20));
            textField.setEditable(isEditable);
            boxes.add(textField);
        }
        blockContainer.add(boxes);
        blocks.put(name, boxes);
        return blockContainer;
    }

    JLabel createBlockTitle(String name) {
        JLabel title = new JLabel(name);
        title.setBorder(new EmptyBorder(30, 10, 0, 10));
        title.setFont(font);
        return title;
    }

    public JButton resetButton;
    JPanel createMainContainer() {
        JPanel c = new JPanel(new GridLayout(5, 1, 0, 0));
        FlowLayout fl = new FlowLayout(FlowLayout.LEFT, 30, 0);
        String[] unknownBlock = new String[blockSize];
        Arrays.fill(unknownBlock, "??");
        byte[] C1 = Arrays.copyOfRange(cipherText, 0, blockSize);
        byte[] C2 = Arrays.copyOfRange(cipherText, blockSize, 2 * blockSize);
        byte[] C3 = Arrays.copyOfRange(cipherText, 2 * blockSize, cipherText.length);

        JPanel row1 = new JPanel(fl);
        row1.add(createBlockTitle("C:"));
        row1.add(createBlock("C1", false, List.of(Conversions.hex(C1).split(" "))));
        row1.add(createBlock("C2", false, List.of(Conversions.hex(C2).split(" "))));
        row1.add(createBlock("C3", false, List.of(Conversions.hex(C3).split(" "))));
        c.add(row1);

        JPanel row2 = new JPanel(fl);
        row2.add(createBlockTitle("C':"));
        row2.add(createBlock("C'1", true, List.of(Conversions.hex(C1).split(" "))));
        row2.add(createBlock("C'2", true, List.of(Conversions.hex(C2).split(" "))));
        row2.add(createBlock("C'3", true, List.of(Conversions.hex(C3).split(" "))));
        JPanel resetButtonContainer = new JPanel();
        resetButton = new JButton("Сбросить");
        resetButton.setFont(new Font(font.getName(), font.getStyle(), 20));
        resetButtonContainer.setBorder(new EmptyBorder(30, 0, 0, 0));
        resetButton.addActionListener(e -> {
            System.out.println(getSize().getWidth() + " - " + getSize().getHeight());
            copyBlockText("C1", "C'1");
            copyBlockText("C2", "C'2");
            copyBlockText("C3", "C'3");
        });
        resetButtonContainer.add(resetButton);
        row2.add(resetButtonContainer);
        c.add(row2);

        JPanel row3 = createCalculationRow();
        c.add(row3);

        JPanel row4 = new JPanel(fl);
        row4.add(createBlockTitle("P:"));
        row4.add(createBlock("P2", true, List.of(unknownBlock)));
        row4.add(createBlock("P3", true, List.of(unknownBlock)));
        row4.add(createHexToTextConverter());
        c.add(row4);

        JPanel row5 = createServerRow();

        c.add(row5);

        return c;
    }


    public JComboBox<String> sendingBlocksComboBox;
    public JComboBox<String> iteratingComboBox;
    public JButton iterateButton;
    public ServerContainer serverContainer;
    JPanel createServerRow() {
        JPanel c = new JPanel(new GridLayout(1, 2));
        c.setBorder(new EmptyBorder(0, 0, 30, 0));
        JPanel left = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        Font tmpFont = new Font(font.getName(), font.getStyle(), 20);
        sendingBlocksComboBox = new JComboBox<>(new String[] {"C'1+C'2+C'3", "C'1+C'2"});
        sendingBlocksComboBox.setSelectedItem("C'1+C'2+C'3");
        sendingBlocksComboBox.setFont(font);
        sendingBlocksComboBox.setBackground(Color.WHITE);
        JButton sendButton = new JButton("Отправить");
        sendButton.setFont(tmpFont);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 20; gbc.ipady = 10;
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3; gbc.gridheight = 1;
        left.add(sendingBlocksComboBox, gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.gridwidth = 2;
        left.add(Box.createHorizontalStrut(150), gbc);

        gbc.gridx = 5; gbc.gridy = 0; gbc.gridwidth = 2; gbc.gridheight = 1;
        left.add(sendButton, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 5; gbc.gridheight = 1;
        left.add(Box.createVerticalStrut(15), gbc);

        iteratingComboBox = new JComboBox<>();
        String[] iteratingBlocks = new String[] {"C'1", "C'2"};
        for (String b : iteratingBlocks) {
            for (int j = 0; j < blockSize; j++) {
                iteratingComboBox.addItem(b + " [" + (j + 1) + "]");
            }
        }
        iteratingComboBox.setFont(font);
        iteratingComboBox.setBackground(Color.WHITE);
        iterateButton = new JButton("Перебор");
        iterateButton.setFont(tmpFont);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3; gbc.gridheight = 1;
        left.add(iteratingComboBox, gbc);
        gbc.gridx = 5; gbc.gridy = 2; gbc.gridwidth = 2; gbc.gridheight = 1;
        left.add(iterateButton, gbc);

        serverContainer = new ServerContainer();

        sendButton.addActionListener(e -> {
            String[] selectedBlocksNames = ((String) Objects.requireNonNull(sendingBlocksComboBox.getSelectedItem())).split("\\+");
            byte[] encryptedBlocks = new byte[selectedBlocksNames.length * blockSize];
            for (int i = 0; i < selectedBlocksNames.length; i++) {
                byte[] block = getBytesFromTextField(selectedBlocksNames[i]);
                System.arraycopy(block, 0, encryptedBlocks, i*blockSize, blockSize);
            }
            serverContainer.checkPadding(encryptedBlocks);
        });

        iterateButton.addActionListener(e -> {
            iterateButton.setEnabled(false);
            String[] selectedBlocksNames = ((String) Objects.requireNonNull(sendingBlocksComboBox.getSelectedItem())).split("\\+");
            String[] selectedBlockAndIndex = ((String) Objects.requireNonNull(iteratingComboBox.getSelectedItem())).split(" ");
            byte[] encryptedBlocks = new byte[selectedBlocksNames.length * blockSize];
            for (int i = 0; i < selectedBlocksNames.length; i++) {
                byte[] block = getBytesFromTextField(selectedBlocksNames[i]);
                System.arraycopy(block, 0, encryptedBlocks, i*blockSize, blockSize);
            }
            int blockIndex = Integer.parseInt(selectedBlockAndIndex[0].replaceAll("\\D+","")) - 1;
            int byteIndex = Integer.parseInt(selectedBlockAndIndex[1].replaceAll("\\D+", "")) - 1;

            new SwingWorker<Byte, Objects>() {

                @Override
                protected Byte doInBackground() throws Exception {
                    return serverContainer.iterateBytesInBlock(encryptedBlocks, blockIndex, byteIndex);
                }

                @Override
                protected void done() {
                    super.done();
                    try {
                        Byte res = get();
                        JLabel message = new JLabel();
                        message.setFont(new Font(font.getName(), font.getStyle(), 20));
                        if (res == null) {
                            message.setText("Нет подходящего байта");
                        } else {
                            message.setText("Правильное дополнение при " + iteratingComboBox.getSelectedItem() + " = " + String.format("%02X ", res));
                        }
                        JOptionPane.showMessageDialog(null, message);
                        iterateButton.setEnabled(true);
                    } catch (InterruptedException | ExecutionException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }.execute();
        });

        c.add(left);
        c.add(serverContainer);

        return c;
    }

    JPanel createHexToTextConverter() {
        JPanel c = new JPanel(new GridBagLayout());
        c.setBorder(new EmptyBorder(10, 0, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        Font tmpFont = new Font(font.getName(), font.getStyle(), 20);
        JLabel label = new JLabel("Перевод блока в текст:");
        label.setFont(tmpFont);
        gbc.gridx = gbc.gridy = 0;
        c.add(label, gbc);
        JPanel comboBoxContainer = new JPanel(new FlowLayout());
        JComboBox<String> comboBox = new JComboBox<>(new String[] {"P2", "P3", "P2+P3"});
        comboBox.setSelectedItem(null);
        comboBox.setFont(tmpFont);
        comboBox.setBackground(Color.WHITE);
        comboBoxContainer.add(comboBox);
        TextField textField = new TextField("", 2 * blockSize);
        textField.setEditable(false);
        textField.setBackground(Color.WHITE);
        textField.setFont(tmpFont);
        comboBoxContainer.add(textField);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 3;
        c.add(comboBoxContainer, gbc);

        comboBox.addActionListener(e -> {
            String[] selected = ((String) Objects.requireNonNull(comboBox.getSelectedItem())).split("\\+");
            byte[] resBytes = new byte[blockSize * selected.length];
            for (int b = 0; b < selected.length; b++) {
                JPanel selectedBlock = blocks.get(selected[b]);
                Component[] components = selectedBlock.getComponents();
                for (int i = 0; i < components.length; i++) {
                    HexTextField box = (HexTextField) components[i];
                    if (Objects.equals(box.getText(), "??")) {
                        resBytes[b * blockSize + i] = (byte) 0x3F; //  ?
                    } else {
                        resBytes[b * blockSize + i] = Conversions.hex(box.getText())[0];
                    }
                }
            }
            textField.setText(new String(resBytes, StandardCharsets.UTF_8));
        });
        return c;
    }

    JPanel createCalculationRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        String[] zeroBlock = new String[blockSize];
        Arrays.fill(zeroBlock, "00");

        JPanel xorBytes = createXORBytes();
        xorBytes.setBorder(new EmptyBorder(30, 0, 0, 0));

        JPanel xorBlocks = createXORBlocks();
        xorBlocks.setBorder(new EmptyBorder(30, 0, 0, 0));

        JPanel draftBlock = createBlock("D", true, List.of(zeroBlock));

        p.add(xorBytes);
        p.add(xorBlocks);
        p.add(draftBlock);
        return p;
    }

    public JButton xorBlocksButton;
    JPanel createXORBlocks() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        String[] possibleOperands = new String[] {"C1", "C2", "C3", "C'1", "C'2", "C'3", "D"};
        String[] possibleResults = new String[] {"C'1", "C'2", "C'3", "D", "P2", "P3"};
        JComboBox<String> leftBox = new JComboBox<>(possibleOperands);
        leftBox.setSelectedItem("C2");
        leftBox.setFont(font);
        leftBox.setBackground(Color.WHITE);
        JComboBox<String> rightBox = new JComboBox<>(possibleOperands);
        rightBox.setSelectedItem("C'2");
        rightBox.setFont(font);
        rightBox.setBackground(Color.WHITE);

        xorBlocksButton = new JButton("=");
        xorBlocksButton.setFont(font);

        JComboBox<String> resultBox = new JComboBox<>(possibleResults);
        resultBox.setSelectedItem("P3");
        resultBox.setFont(font);
        resultBox.setBackground(Color.WHITE);

        xorBlocksButton.addActionListener(e -> {
            String leftName = (String) leftBox.getSelectedItem();
            String rightName = (String) rightBox.getSelectedItem();
            String resName = (String) resultBox.getSelectedItem();
            Component[] resBlock = blocks.get(resName).getComponents();
            byte[] lb = getBytesFromTextField(leftName);
            byte[] rb = getBytesFromTextField(rightName);
            for (int i = 0; i < resBlock.length; i++) {
                HexTextField res = (HexTextField) resBlock[i];
                byte c = (byte) (lb[i] ^ rb[i]);
                res.setText(String.format("%02X ", c));
            }
        });

        p.add(leftBox);
        p.add(createXORLabel());
        p.add(rightBox);
        p.add(xorBlocksButton);
        p.add(resultBox);
        return p;
    }

    public VisualizationCBCAttack(Algorithm alg, byte[] plainTextData, boolean isHelperNeeded) {
        this.algorithm = alg;
        this.cipherText = alg.encrypt(plainTextData);
        this.blockSize = alg.BLOCK_SIZE;
        mainContainer = createMainContainer();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        if (isHelperNeeded) {
            add(createHelperContainer());
        }
        add(mainContainer);
        setVisible(true);
    }
    Component target;
    JPanel createHelperContainer() {
        JPanel c = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btn = new JButton("Помощь");
        btn.setFont(new Font(font.getName(), font.getStyle(), 25));
        c.add(btn);
        c.setBorder(new EmptyBorder(5, 0, 0, 5));
        btn.addActionListener(e -> {
            JDialog d = new JDialog((Frame) null, "Атака на режим простой замены с зацеплением (Помощь).", true);
            d.getContentPane().add(new VisualizationCBCAttackHelper(algorithm, "        12345678bingo".getBytes()));
            d.pack();
            d.setMinimumSize(new Dimension(d.getWidth(), d.getHeight() + 50));
            d.setResizable(false);
            d.setVisible(true);
        });
        return c;
    }


    void copyBlockText(String from, String to) {
        Component[] src = blocks.get(from).getComponents();
        Component[] dst = blocks.get(to).getComponents();
        for (int i = 0; i < src.length; i++) {
            HexTextField d = (HexTextField) dst[i];
            HexTextField s = (HexTextField) src[i];
            d.setText(s.getText());
        }
    }

    JPanel createXORBytes() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        CompoundBorder border = new CompoundBorder(new LineBorder(Color.BLACK, 1), new EmptyBorder(3, 5, 3, -30));

        HexTextField leftTextField = new HexTextField("00", 1, false);
        leftTextField.setFont(font);
        leftTextField.setBorder(border);
        p.add(leftTextField);

        p.add(createXORLabel());

        HexTextField rightTextField = new HexTextField("00", 1, false);
        rightTextField.setFont(font);
        rightTextField.setBorder(border);
        p.add(rightTextField);

        JButton calculate = new JButton("=");
        calculate.setFont(font);
        p.add(calculate);

        HexTextField result = new HexTextField("00", 1, false);
        result.setFont(font);
        result.setBorder(border);
        p.add(result);

        calculate.addActionListener(e -> {
            byte a = HexFormat.of().parseHex(leftTextField.getText())[0];
            byte b = HexFormat.of().parseHex(rightTextField.getText())[0];
            byte res = (byte) (a ^ b);
            result.setText(String.format("%02X ", res));
        });
        return p;
    }

    public byte[] getBytesFromTextField(String name) {
        Component[] block = blocks.get(name).getComponents();
        byte[] result = new byte[blockSize];
        for (int i = 0; i < blockSize; i++) {
            HexTextField box = (HexTextField) block[i];
            result[i] = HexFormat.of().parseHex(box.getText())[0];
        }
        return result;
    }

    class ServerContainer extends JPanel {

        private JLabel okLabel;
        private JLabel errorLabel;
        private final JPanel answerContainer;

        void loadImages() {
            try {
                BufferedImage okBuff = ImageIO.read(Objects.requireNonNull(getClass().getResource("/files/images/ok.png")));
                ImageIcon okImageIcon = new ImageIcon(okBuff);
                Image okImage = okImageIcon.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT);
                okLabel = new JLabel(new ImageIcon(okImage));
            } catch (Exception e) {
//                e.printStackTrace();
                System.err.println("Не удалось загурзить изображение для \"ответ от сервер\"");
                JLabel label1 = new JLabel("OK");
                label1.setFont(new Font("Times New Roman", Font.PLAIN, 35));
                label1.setForeground(Color.GREEN);
                okLabel = label1;
            }

            try {
                BufferedImage errorBuff = ImageIO.read(Objects.requireNonNull(getClass().getResource("/files/images/error.png")));
                ImageIcon errorImageIcon = new ImageIcon(errorBuff);
                Image errorImage = errorImageIcon.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT);
                errorLabel = new JLabel(new ImageIcon(errorImage));
            } catch (Exception e) {
//                e.printStackTrace();
                System.err.println("Не удалось загурзить изображение для \"ответ от сервер\"");
                JLabel label1 = new JLabel("ERROR");
                label1.setFont(new Font("Times New Roman", Font.PLAIN, 35));
                label1.setForeground(Color.RED);
                errorLabel = label1;
            }
        }

        ServerContainer() {
            setLayout(new FlowLayout());
            JLabel server = new JLabel("Ответ от сервера: ", SwingConstants.CENTER);
            server.setFont(new Font("Times New Roman", Font.PLAIN, 40));
            answerContainer = new JPanel();
            JLabel emptyLabel = new JLabel();
            emptyLabel.setFont(new Font("Times New Roman", Font.PLAIN, 25));
            CompoundBorder answerBorder = new CompoundBorder(new LineBorder(Color.BLACK, 5), new EmptyBorder(10, 10, 10, 10));
            emptyLabel.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 5), new EmptyBorder(35, 35, 35, 35)));
            loadImages();
            okLabel.setBorder(answerBorder);
            errorLabel.setBorder(answerBorder);
            answerContainer.add(emptyLabel);

            add(server);
            add(answerContainer);
        }

        public void checkPadding(byte[] encryptedText) {
            answerContainer.remove(0);
            if (isValidPadding(encryptedText)) {
                answerContainer.add(okLabel);
            } else {
                answerContainer.add(errorLabel);
            }
            answerContainer.updateUI();
        }

        Byte iterateBytesInBlock(byte[] ciphertext, int blockIndex, int byteIndex) {
            for (int i = 0; i < 256; i++) {
                if (isValidPadding(ciphertext)) {
                    return ciphertext[blockSize * blockIndex + byteIndex];
                }
                ciphertext[blockSize * blockIndex + byteIndex]++;
            }
            return null;
        }

        public boolean isValidPadding(byte[] encryptedText) {
            byte[] decryptedData = algorithm.decrypt(encryptedText, false);
            int padIndex = decryptedData.length - 1;
            while (decryptedData[padIndex] == (byte) 0x00 && padIndex > decryptedData.length - blockSize) {
                padIndex--;
            }
            return decryptedData[padIndex] == (byte) 0x80;
        }
    }

}


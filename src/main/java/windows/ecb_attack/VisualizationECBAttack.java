package windows.ecb_attack;

import algorithm.Algorithm;
import utils.Conversions;
import widgets.IntegerTextField;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class VisualizationECBAttack extends JPanel {
    Algorithm algorithm;
    int blockSize;
    JPanel mainContainer;
    public HashMap<String, JTextArea> textAreas = new HashMap<>();
    public ServerContainer serverContainer;
    Font font = new Font("Times New Roman", Font.PLAIN, 30);

    JLabel createLabel(String name, int fontStyle, int fontSize) {
        JLabel label = new JLabel(name);
        label.setFont(new Font("Times New Roman", fontStyle, fontSize));
        return label;
    }

    JTextComponent makeHexTextField(JTextComponent tf) {
        tf.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterText();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterText();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterText();
            }

            private boolean filtering = false;

            private void filterText() {
                if (filtering) return;
                filtering = true;

                EventQueue.invokeLater(() -> {
                    int caretPos = tf.getCaretPosition();
                    String input = tf.getText().toUpperCase();
                    StringBuilder filtered = new StringBuilder();
                    int index = 0;

                    for (int i = 0; i < input.length(); i++) {
                        char c = Character.toUpperCase(input.charAt(i));
                        if ("0123456789ABCDEFabcdef".indexOf(c) >= 0) {
                            filtered.append(c);
                            if (index++ % 2 == 1 && i != input.length() - 1) {
                                filtered.append(" ");
                            }
                        }
                    }

                    tf.setText(filtered.toString());
                    tf.setCaretPosition(Math.min(caretPos, filtered.length()));
                    filtering = false;
                });
            }
        });
        return tf;
    }

    JPanel createLabeledTextField(int rows, int cols, String label, String name, EmptyBorder border) {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        JLabel label1 = createLabel(label, Font.BOLD, 20);
        label1.setHorizontalAlignment(SwingConstants.LEFT);
        c.add(label1);
        JTextArea tf = new JTextArea("", rows, cols);
        tf.setLineWrap(true);
        tf.setFont(new Font(font.getName(), font.getStyle(), 30));
        JScrollPane scrollPane = new JScrollPane(tf, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        c.add(scrollPane);
        c.setBorder(border);
        textAreas.put(name, tf);
        return c;
    }

    JPanel createTopContainer(byte[] secret) {
        JPanel c = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 3;
        c.add(createLabeledTextField(5, 16, "Префикс             ", "prefixField", null), gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        c.add(Box.createHorizontalStrut(150));

        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        c.add(Box.createHorizontalStrut(150), gbc);

        JPanel tmpPanel2 = new JPanel();
        JButton sendBtn = new JButton("Отправить");
        sendBtn.setFont(new Font(font.getName(), font.getStyle(), 20));
        tmpPanel2.add(sendBtn);
        tmpPanel2.setBorder(new EmptyBorder(25, 0, 0, 0));
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        c.add(tmpPanel2, gbc);

        serverContainer = new ServerContainer(secret);
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 3;
        gbc.fill = GridBagConstraints.NONE;
        c.add(serverContainer, gbc);

        sendBtn.addActionListener(e -> {
            JTextArea tf = textAreas.get("prefixField");
            if (tf.getText().length() == 0) {
                JOptionPane.showMessageDialog(null, "Нельзя отправить пустые данные.");
                return;
            }
            serverContainer.sendData(tf.getText().getBytes());
        });

        return c;
    }

//    JPanel createHexToTextConverter() {
//        JPanel c = new JPanel(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        JLabel byteLabel = createLabel("Байты", Font.BOLD, 25);
//        gbc.gridx = 0; gbc.gridy = 0;
//        c.add(byteLabel, gbc);
//
//        JLabel convertedLabel = createLabel("Текст", Font.BOLD, 25);
//        gbc.gridx = 2; gbc.gridy = 0;
//        c.add(convertedLabel, gbc);
//
//        JPanel btnContainer = new JPanel();
//        JButton convertButton = new JButton("=");
//        convertButton.setFont(new Font(font.getName(), font.getStyle(), 20));
//        gbc.gridx = 1; gbc.gridy = 1;
//        gbc.weightx = 0.1; gbc.anchor = GridBagConstraints.CENTER;
//        btnContainer.add(convertButton);
//        btnContainer.setBorder(new EmptyBorder(75, 0, 0, 0));
//        c.add(btnContainer, gbc);
//
//        JTextArea byteTextArea = (JTextArea) makeHexTextField(new JTextArea("", 6, 10));
//        JScrollPane scrollPaneByte = new JScrollPane(byteTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        byteTextArea.setLineWrap(true);
//        byteTextArea.setFont(font);
//        gbc.gridx = 0; gbc.gridy = 1;
//        gbc.gridheight = 2; gbc.weightx = 0.4;
//        c.add(scrollPaneByte, gbc);
//
//        JTextArea convertedTextArea = new JTextArea("", 6, 10);
//        JScrollPane scrollPaneText = new JScrollPane(convertedTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        convertedTextArea.setLineWrap(true);
//        convertedTextArea.setEditable(true);
//        convertedTextArea.setFont(font);
//        gbc.gridx = 2; gbc.gridy = 1;
//        gbc.gridheight = 2; gbc.weightx = 0.4;
//        c.add(scrollPaneText, gbc);
//
//        convertButton.addActionListener(e -> {
//            byte[] bytes = Conversions.hex(byteTextArea.getText());
//            String res = new String(bytes, StandardCharsets.US_ASCII);
//            convertedTextArea.setText(res);
//        });
//
//        return c;
//    }


    public JTextField cipherTextField;
    public JTextField plainTextField;
    public IntegerTextField iterateIndexField;
    public JButton iterateButton;
    JPanel createBottomContainer() {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 1), new EmptyBorder(0, 0, 0, 0)));

        JPanel cipherTextContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cipherTextContainer.add(createLabel("C  = ", Font.PLAIN, 25));
        cipherTextField = (JTextField) makeHexTextField(new JTextField());
        cipherTextField.setFont(new Font(font.getName(), font.getStyle(), 25));
        cipherTextField.setColumns(16);
        cipherTextContainer.add(cipherTextField);

        JPanel plainTextContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        plainTextContainer.add(createLabel("P (префикс) = ", Font.PLAIN, 25));
        plainTextField =new JTextField(16);
        plainTextField.setFont(new Font(font.getName(), font.getStyle(), 25));
        plainTextContainer.add(plainTextField);

        JPanel iterateIndexContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        iterateIndexContainer.add(createLabel("i  = ", Font.PLAIN, 25));
        iterateIndexField =new IntegerTextField(1);
        iterateIndexField.setFont(new Font(font.getName(), font.getStyle(), 25));
        iterateIndexContainer.add(iterateIndexField);

        JPanel textContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String html = "<html><body style='width: %1spx'>%1s";
        String s = "Панель для перебора P[i]-го символа, пока блок C не будет равен первому блоку в ответе от сервера (используются символы ASCII).";
        textContainer.add(createLabel(String.format(html, 550, s), Font.PLAIN, 25));

        JPanel btnContainer = new JPanel();
        iterateButton = new JButton("Перебор");
        iterateButton.setFont(new Font(font.getName(), font.getStyle(), 25));
        btnContainer.add(iterateButton);

        c.add(textContainer);
        c.add(cipherTextContainer);
        c.add(plainTextContainer);
        c.add(iterateIndexContainer);
        c.add(btnContainer);


        iterateButton.addActionListener(e -> {
            int blockSizeInput = 8;
            byte[] C = Conversions.hex(cipherTextField.getText());

            if (C.length == 0) {
                JOptionPane.showMessageDialog(this, "Введите блок C.", "Неверные данные", JOptionPane.ERROR_MESSAGE);
                return;
            }


            byte[] P = plainTextField.getText().getBytes();
            if (P.length == 0) {
                JOptionPane.showMessageDialog(this, "Введите блок P.", "Неверные данные", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String iField = iterateIndexField.getText();
            if (iField.length() == 0) {
                JOptionPane.showMessageDialog(this, "Введите i.", "Неверные данные", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int iterateIndex = Integer.parseInt(iField);
            if (iterateIndex == 0 || iterateIndex > (new String(P, StandardCharsets.US_ASCII).length())) {
                JOptionPane.showMessageDialog(this, "Недопустимый индекс i в префиксе P.", "Неверные данные", JOptionPane.ERROR_MESSAGE);
                return;
            }

            iterateButton.setEnabled(false);
            new SwingWorker<Byte, Objects>() {

                @Override
                protected Byte doInBackground() {
                    return serverContainer.searchValidAnswer(C, P, blockSizeInput, iterateIndex);
                }

                @Override
                protected void done() {
                    super.done();
                    iterateButton.setEnabled(true);
                    try {
                        Byte res = get();
                        if (res == null) {
                            JOptionPane.showMessageDialog(null, "Совпадения не найдены при всех символах ASCII.", "Перебор окончен", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Найдено равенство при P[" + iterateIndex + "] = '" + new String(new byte[]{res}) + "' (0x" + HexFormat.of().toHexDigits(res) + ")");
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }.execute();

        });

        return c;
    }

    JPanel createMainContainer(byte[] secretData) {
        JPanel c = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        c.add(createTopContainer(secretData), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        c.add(Box.createVerticalStrut(50), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.7;
        gbc.anchor = GridBagConstraints.WEST;
        c.add(createBottomContainer(), gbc);

//        gbc.gridx = 1;
//        gbc.gridy = 1;
//        gbc.gridwidth = 1;
//        gbc.gridheight = 1;
//        gbc.weightx = 0.1;
//        gbc.fill = GridBagConstraints.VERTICAL;
//        c.add(Box.createHorizontalStrut(10), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.3; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        c.add(createLabeledTextField(7, 16, "Черновик D                     ", "answerField", new EmptyBorder(0, 10, 0, 0)), gbc);
//        c.add(createHexToTextConverter(), gbc);
        c.setBorder(new EmptyBorder(0, 10, 10, 10));
        return c;
    }

    public VisualizationECBAttack(Algorithm alg, byte[] secretData, boolean isHelperNeeded) {
        this.algorithm = alg;
        this.blockSize = alg.BLOCK_SIZE;
        mainContainer = createMainContainer(secretData);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        if (isHelperNeeded) {
            add(createHelperContainer());
        }
        add(mainContainer);
    }

    JPanel createHelperContainer() {
        JPanel c = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btn = new JButton("Помощь");
        btn.setFont(new Font(font.getName(), font.getStyle(), 25));
        c.add(btn);
        c.setBorder(new EmptyBorder(5, 0, 0, 5));
        btn.addActionListener(e -> {
            JDialog d = new JDialog((Frame) null, "Атака на режим простой замены (Помощь).", true);
            d.getContentPane().add(new VisualizationECBAttackHelper(algorithm));
            d.pack();
            d.setMinimumSize(new Dimension(d.getWidth(), d.getHeight() + 100));
            d.setResizable(false);
            d.setVisible(true);

        });
        return c;
    }

    class ServerContainer extends JPanel {
        private final byte[] secretData;
        private final JTextArea serverAnswer;

        ServerContainer(byte[] secretData) {
            this.secretData = secretData;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            JLabel serverLabel = createLabel("Ответ от сервера (байты)", Font.BOLD, 20);
            serverLabel.setHorizontalAlignment(SwingConstants.LEFT);
            add(serverLabel);

            serverAnswer = new JTextArea(5, 16);
            serverAnswer.setFont(font);
            serverAnswer.setLineWrap(true);
            serverAnswer.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(serverAnswer, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            add(scrollPane);

            serverAnswer.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void changedUpdate(DocumentEvent e) {
                    filterText();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    filterText();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    filterText();
                }

                private boolean filtering = false;
                private void filterText() {
                    if (filtering) return;
                    filtering = true;

                    EventQueue.invokeLater(() -> {
                        String input = serverAnswer.getText().toUpperCase();
                        StringBuilder filtered = new StringBuilder();
                        int index = 0;

                        for (int i = 0; i < input.length(); i++) {
                            char c = Character.toUpperCase(input.charAt(i));
                            if ("0123456789ABCDEF".indexOf(c) >= 0) {
                                filtered.append(c);
                                if (index++ % 2 == 1 && i != input.length() - 1) {
                                    filtered.append(index % (2 * blockSize) == 0 ? "\n" : " ");
                                }
                            }
                        }

                        serverAnswer.setText(filtered.toString());
                        filtering = false;
                    });
                }
            });

        }

        public byte[] getEncryptedData(byte[] prefix) {
            byte[] both = Arrays.copyOf(prefix, prefix.length + secretData.length);
            System.arraycopy(secretData, 0, both, prefix.length, secretData.length);
            return algorithm.encrypt(both);
        }

        public void sendData(byte[] prefix) {
            byte[] cipherText = getEncryptedData(prefix);
            serverAnswer.setText(HexFormat.of().formatHex(cipherText));
        }

        public Byte searchValidAnswer(byte[] C, byte[] P, int blockSizeInput, int iterateIndex) {
            int index = iterateIndex - 1;
            for (int i = 0; i < 256; i++) {
                byte[] res = getEncryptedData(P);
                int size = Math.min(blockSizeInput, res.length);
                if (Arrays.equals(C, Arrays.copyOfRange(res, 0, size))) {
                    return P[index];
                }
                P[index]++;
            }
            return null;
        }
    }

}
package windows.cbc_attack;

import algorithm.Algorithm;
import widgets.HexTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class VisualizationCBCAttackHelper extends JPanel {
    byte[] cipherText;
    int blockSize;
    VisualizationCBCAttack cbcAttack;
    Font font = new Font("Times New Roman", Font.PLAIN, 20);

    ArrayList<JLabel> lines;

    JLabel helperText;
    String html = "<html><body style='width: %1spx'>%1s";
    JPanel createStepsContainer() {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        helperText = new JLabel("");
        helperText.setFont(font);
        helperText.setBorder(new EmptyBorder(5, 0, 5, 0));
        c.add(helperText);

        String s = "Есть шифротекст C, зашифрованный в режиме CBC и состоящий из 3-х блоков." +
                " И также есть сервер, который расшифровывает шифротекст C, и отвечает 'да' или 'нет', в зависимости от правильности дополнения." +
                " Нам также известен способ дополнения. (0x80, 0x00, 0x00, ...)." +
                " В качестве примера, расшифруем третий блок C3.";
        helperText.setText(String.format(html, 900, s));

        c.setBorder(new EmptyBorder(0, 10, 0, 0));
        return c;
    }


    JPanel createHelperContainer() {
        JPanel c = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 0.9; gbc.anchor = GridBagConstraints.WEST;
        c.add(createStepsContainer(), gbc);

        JPanel nextBtnContainer = new JPanel();
        JButton nextBtn = new JButton("Далее");
        nextBtn.setFont(font);
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 0.1; gbc.anchor = GridBagConstraints.EAST;
        nextBtnContainer.add(nextBtn);
        nextBtnContainer.setBorder(new EmptyBorder(0, 0, 0, 10));
        c.add(nextBtnContainer, gbc);
        var steps = new Object() {
            int stepIndex = -1;
            int decryptedBytesCount = 0;
            final byte[] D = new byte[blockSize];
            int paddingSize;
        };
        nextBtn.addActionListener(e -> {
            steps.stepIndex++;
            cbcAttack.sendingBlocksComboBox.setSelectedItem("C'1+C'2+C'3");
            if (steps.decryptedBytesCount == blockSize) {
                String s = "У нас теперь есть промежуточное значение блока C'3 (которое мы записали в черновик D). Тогда, проXOR-ив блок D с исходным блоком C2, мы получим блок P3 (расшифровка блока C3).";
                helperText.setText(String.format(html, 900, s));
                byte[] C2 = cbcAttack.getBytesFromTextField("C2");
                byte[] D = cbcAttack.getBytesFromTextField("D");
                byte[] P3 = new byte[blockSize];
                for (int i = 0; i < blockSize; i++) {
                    P3[i] = (byte) (C2[i] ^ D[i]);
                }
                updateBlockUI("P3", P3);
                nextBtn.setEnabled(false);
            } else if (steps.stepIndex % 3 == 0) {
                nextBtn.setEnabled(false);
                StringBuilder s = new StringBuilder("Найдем валидный ответ от сервера. "
                        + "Для этого перебираем C'2[" + (blockSize - steps.decryptedBytesCount) + "] пока не получим валидный ответ от сервера. "
                );
                cbcAttack.iteratingComboBox.setSelectedItem("C'2 [" + (blockSize - steps.decryptedBytesCount) + "]");
                new SwingWorker<Byte, Objects>() {

                    @Override
                    protected Byte doInBackground() throws Exception {
                        return cbcAttack.serverContainer.iterateBytesInBlock(C_, 1, blockSize - steps.decryptedBytesCount - 1);
                    }

                    @Override
                    protected void done() {
                        super.done();
                        try {
                            Byte res = get();
                            s.append("В данном случае, правильное дополнение получается при C'2[").append(blockSize - steps.decryptedBytesCount).append("] = ").append(String.format("%02X ", res));
                            helperText.setText(String.format(html, 900, s));
                            C_[blockSize + (blockSize - steps.decryptedBytesCount - 1)] = res;
                            updateCBlocksUI();
                            nextBtn.setEnabled(true);
                        } catch (InterruptedException | ExecutionException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }.execute();
            } else if (steps.stepIndex % 3 == 1) {
                StringBuilder s = new StringBuilder("Далее ищем длину дополнения. Для этого начиная с C'2[1] по C'2[8] меняем байты, пока не затронем дополнение и сервер не вернет ошибку. ");
                byte[] tmp = Arrays.copyOf(C_, C_.length);
                int i, paddingSize = -1;
                for (i = 0; i < 8; i++) {
                    tmp[blockSize + i]++;
                    if (!cbcAttack.serverContainer.isValidPadding(tmp)) {
                        paddingSize = blockSize - i;
                        break;
                    }
                }
                s.append("При изменении C'[").append(i + 1).append("], дополнение ломается. Значит длина дополнения равна ").append(paddingSize);
                StringBuilder tmpS = new StringBuilder(". Раз длина дополнения равна " + paddingSize + ", значит P3[" + (blockSize - paddingSize + 1) + " - " + (blockSize) + "] после XOR с блоком C'2 выглядит как " + "80");
                for (i = 0; i < paddingSize - 1; i++) {
                    tmpS.append(String.format(" %02X", 0));
                }
                s.append(tmpS);
                helperText.setText(String.format(html, 900, s));
                steps.paddingSize = paddingSize;
            } else if (steps.stepIndex % 3 == 2) {
                StringBuilder pad = new StringBuilder("80");
                pad.append(String.valueOf(String.format(" %02X", 0)).repeat(Math.max(0, steps.paddingSize - 1)));
                String padIndices = (blockSize - steps.paddingSize + 1) + " - " + blockSize;
                String s = "Тогда проXOR-ив P3[" + padIndices + "] = [" + pad + "] с C'2[" + padIndices + "], получим промежуточное значение блока C3=C'3 (C'3 мы не меняем)." +
                        " Запишем полученные значения в черновик D и в C'2." +
                        " Теперь, после XOR C'2 с промежуточным значением блока C'3, последние " + steps.paddingSize + " байт превратятся в нули." +
                        " Промежуточное значение блока C'3 - это блок, который получается после процедуры расшифровки блока C'3" +
                        " алгоритмом шифрования, но до XOR с блоком C'2.";
                helperText.setText(String.format(html, 900, s));


                byte[] padBlocks = new byte[steps.paddingSize];
                padBlocks[0] = (byte) 0x80;
                for (int i = 0; i < padBlocks.length; i++) {
                    byte xorRes = (byte) (padBlocks[padBlocks.length - i - 1] ^ C_[2 * blockSize - 1 - i]);
                    C_[2 * blockSize - 1 - i] = xorRes;
                    steps.D[steps.D.length - i - 1] = xorRes;
                }
                updateCBlocksUI();
                updateBlockUI("D", steps.D);
                steps.decryptedBytesCount = steps.paddingSize;

            }
        });

        return c;
    }
    void updateCBlocksUI() {
        String[] bNames = new String[] {"C'1", "C'2", "C'3"};
        int i = 0;
        for (String n : bNames) {
            Component[] comps = cbcAttack.blocks.get(n).getComponents();
            for (Component comp : comps) {
                ((HexTextField) comp).setText(String.format("%02X ", C_[i++]));
            }
        }
    }
    void updateBlockUI(String name, byte[] block) {
        Component[] c = cbcAttack.blocks.get(name).getComponents();
        for (int i = 0; i < c.length; i++) {
            ((HexTextField) c[i]).setText(String.format("%02X ", block[i]));
        }
    }

    byte[] C_;
    public VisualizationCBCAttackHelper(Algorithm alg,  byte[] plainTextData) {
        cbcAttack = new VisualizationCBCAttack(alg, plainTextData, false);
        this.cipherText = alg.encrypt(plainTextData);
        C_ = this.cipherText;
        this.blockSize = alg.BLOCK_SIZE;
        disableInterface();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(createHelperContainer());
        add(cbcAttack);
        setVisible(true);
    }

    void disableInterface() {
        disableBlock("C'1");
        disableBlock("C'2");
        disableBlock("C'3");
        disableBlock("D");
        disableBlock("P2");
        disableBlock("P3");
        cbcAttack.xorBlocksButton.setEnabled(false);
        cbcAttack.resetButton.setEnabled(false);
    }

    void disableBlock(String name) {
        JPanel boxes = cbcAttack.blocks.get(name);
        for (Component c : boxes.getComponents()) {
            ((JTextField) c).setEditable(false);
        }
    }
}

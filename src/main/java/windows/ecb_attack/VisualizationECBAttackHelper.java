package windows.ecb_attack;

import algorithm.Algorithm;
import utils.Conversions;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

public class VisualizationECBAttackHelper extends JPanel {
    byte[] cipherText;
    int blockSize;
    VisualizationECBAttack ecbAttack;
    Font font = new Font("Times New Roman", Font.PLAIN, 20);
    String html = "<html><body style='width: %1spx'>%1s";
    JLabel helperText;
    int helperTextWidth = 870;

    JPanel createStepsContainer() {
        JPanel c = new JPanel();
        helperText = new JLabel("");
        helperText.setFont(font);
        helperText.setBorder(new EmptyBorder(5, 0, 5, 0));
        c.add(helperText);
        String s = "На сервере хранится секретная информация S. Мы можем отправлять на сервер текст любой длины (префикс P)." +
                "<br/>Сервер шифрует P+S и возвращает этот шифротекст С в качестве ответа.<br/>Наша цель - вычислить S, путем измененения префикса P.";
        helperText.setText(String.format(html, helperTextWidth, s));

        c.setBorder(new EmptyBorder(0, 10, 0, 0));
        return c;
    }

    JPanel createHelperContainer() {
        JPanel c = new JPanel(new GridBagLayout());
        c.setPreferredSize(new Dimension((int) c.getSize().getWidth(), 150));
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
            int secretLength = -1;
            final StringBuilder decrypted = new StringBuilder();
        };
        nextBtn.addActionListener(e -> {
            steps.stepIndex++;
            if (steps.decrypted.length() == steps.secretLength) {
                nextBtn.setEnabled(false);
                String s = "Секретная информация расшифрована. S = " + steps.decrypted;
                helperText.setText(String.format(html, helperTextWidth, s));
            } else if (steps.stepIndex == 0) {
                String s = "Сначала вычислим длину S.<br/>Для этого будем увеличивать длину префикса P на 1 и отправлять его на сервер."
                        + " Если, после очередной отправки префикса P, количество блоков BlocksNum в ответе от сервера стало на 1 больше, значит длина length(P+S) стало кратно размеру блоков BlockSize"
                        + " и дополнение вытеснилось на следующий блок. Тогда length(S) = BlockSize * (BlocksNum - 1) - length(P) , и последний блок в ответе от сервера - шифровка дополненения [0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00].";
                StringBuilder pref = new StringBuilder("q");
                int ciphertextLength = ecbAttack.serverContainer.getEncryptedData(pref.toString().getBytes()).length;
                while (ciphertextLength == ecbAttack.serverContainer.getEncryptedData((pref + "q").getBytes()).length) {
                    pref.append("q");
                }
                int prefLength = pref.length();
                steps.secretLength = ciphertextLength - (prefLength + 1);
                s += "<br/>При отправке " + prefLength + " q-шек, сервер возвращает " + (ciphertextLength / blockSize) + " блоков, а при отправке "
                        + (prefLength + 1) + " q-шек, сервер возвращает " + (ciphertextLength / blockSize + 1) + " блоков."
                        + " Значит, длина S = " + (ciphertextLength / blockSize) + " * " + "(BlockSize=" + blockSize + ") - "
                        + (prefLength + 1) + " = " + steps.secretLength;
                helperText.setText(String.format(html, helperTextWidth, s));
            } else if (steps.stepIndex == 1) {
                nextBtn.setEnabled(false);
                StringBuilder s = new StringBuilder();
                String P = "123456701234567";
                s.append("Теперь мы знаем длину S. Тогда пусть префикс P = ").append(P).append(".")
                    .append("<br/>Тогда, (P+S) = | 12345670 | 1234567X | ").append("X".repeat(steps.secretLength - 1))
                    .append("<br/>То есть, первый символ секретной информации S (назовем этот символ X) отделился от остальной части и будет зашифрован cо вторым блоком | 1234567X |.")
                    .append("Это значит, что мы можем перебрать последний символ первого блока в P+S, (символ '0'), пока не совпадут первый и второй блоки в ответе от сервера.");
                ecbAttack.textAreas.get("prefixField").setText(P);
                ecbAttack.plainTextField.setText(P);
                byte[] C = Arrays.copyOfRange(ecbAttack.serverContainer.getEncryptedData(P.getBytes()), blockSize, 2 * blockSize);
                ecbAttack.cipherTextField.setText(Conversions.hex(C));
                ecbAttack.iterateIndexField.setText("8");
                workerForIteratingSymbol(C, P.getBytes(), resSymbol -> {
                    steps.decrypted.append(resSymbol);
                    s.append("<br/>В данном случае, при P = 1234567")
                            .append(resSymbol).append("1234567 первые два блока в ответе от сервера совпадают.")
                            .append(" А значит, первый символ мы расшифровали. Запишем  в черновик D.")
                            .append("<br/>Таким же образом расшифруем оставшуюся часть S.");

                    helperText.setText(String.format(html, helperTextWidth, s));
                    ecbAttack.textAreas.get("answerField").setText(resSymbol);
                    nextBtn.setEnabled(true);
                });

            } else if (steps.stepIndex >= 2) {
                nextBtn.setEnabled(false);
                StringBuilder s = new StringBuilder();
                StringBuilder P1 = new StringBuilder(),
                              P2 = new StringBuilder();
                for (int i = 1; i < (blockSize - steps.decrypted.length()); i++) {
                    P1.append(i);
                    P2.append(i);
                }
                P1.append(steps.decrypted);
                P1.append("0");
                String P = P1.toString() + P2;
                s.append("Теперь пусть P = ").append(P);
                s.append("<br/>Тогда снова переберем 8-й символ первого блока в P, пока не совпадут первые 2 блока в ответе от сервера.");
                ecbAttack.textAreas.get("prefixField").setText(P);
                ecbAttack.plainTextField.setText(P);
                byte[] C = Arrays.copyOfRange(ecbAttack.serverContainer.getEncryptedData(P.getBytes()), blockSize, 2 * blockSize);
                ecbAttack.cipherTextField.setText(Conversions.hex(C));
                ecbAttack.iterateIndexField.setText("8");
                workerForIteratingSymbol(C, P.getBytes(), resSymbol -> {
                    steps.decrypted.append(resSymbol);
                    s.append("<br/>При P = ").append(P1.substring(0, 7))
                            .append(resSymbol).append(P2).append(" первые два блока в ответе от сервера совпадают.")
                            .append(" Запишем найденный символ '").append(resSymbol).append("' в черновик D.")
                            .append("<br/>Таким же образом расшифруем оставшуюся часть S.");

                    helperText.setText(String.format(html, helperTextWidth, s));
                    JTextArea answerField = ecbAttack.textAreas.get("answerField");
                    answerField.setText(answerField.getText() + resSymbol);
                    nextBtn.setEnabled(true);
                });
            }
        });

        return c;
    }

    private interface WorkerHelperI {
        void call(String res);
    };

    void workerForIteratingSymbol(byte[] C, byte[] P, WorkerHelperI func) {
        new SwingWorker<Byte, Void>() {

            @Override
            protected Byte doInBackground() throws Exception {
                return ecbAttack.serverContainer.searchValidAnswer(C, P, blockSize, 8);
            }

            @Override
            protected void done() {
                super.done();
                try {
                    Byte res = get();
                    String resSymbol = new String(new byte[]{res});
                    func.call(resSymbol);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }


    public VisualizationECBAttackHelper(Algorithm alg) {
        String s = "bingo";  // only <= 8 ASCII characters
        ecbAttack = new VisualizationECBAttack(alg, s.getBytes(), false);
        this.cipherText = alg.encrypt(s.getBytes());
        this.blockSize = alg.BLOCK_SIZE;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(createHelperContainer());
        add(ecbAttack);
        setVisible(true);
    }

}

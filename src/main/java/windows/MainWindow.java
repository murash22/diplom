package windows;

import algorithm.Algorithm;
import windows.cbc_attack.VisualizationCBCAttack;
import windows.ecb_attack.VisualizationECBAttack;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MainWindow extends JFrame {

    public MainWindow(String name) {
        super(name);
        JPanel mainContainer = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        mainContainer.setBorder(new EmptyBorder(5, 5, 5, 5));
        JButton ecbAttackBtn = new JButton("Атака на режим простой замены");
        ecbAttackBtn.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        ecbAttackBtn.addActionListener(e -> {
            byte[] input = DialogWindow.get().visualizationDialog("Атака на режим простой замены", DialogWindow.AttackType.ECB);
            if (input == null) return;
            JDialog d = new JDialog(this, "Атака на режим простой замены", true);
            Algorithm alg = new Algorithm("ECB");
            d.getContentPane().add(new VisualizationECBAttack(alg, input, true));
            d.pack();
            d.setResizable(false);
            d.setVisible(true);
        });
        JButton cbcAttackBtn = new JButton("Атака на режим простой замены с зацеплением");
        cbcAttackBtn.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        cbcAttackBtn.addActionListener(e -> {
            byte[] input = DialogWindow.get().visualizationDialog("Атака на режим простой замены с зацеплением", DialogWindow.AttackType.CBC);
            if (input == null) return;
            JDialog d = new JDialog(this, "Атака на режим простой замены с зацеплением", true);
            Algorithm alg = new Algorithm("CBC");
            d.getContentPane().add(new VisualizationCBCAttack(alg, input, true));
            d.pack();
            d.setResizable(false);
            d.setVisible(true);
        });
        gbc.gridx = 0; gbc.gridy = 0;
        mainContainer.add(ecbAttackBtn, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        mainContainer.add(Box.createVerticalStrut(100), gbc);
        gbc.gridx = 0; gbc.gridy = 2;
        mainContainer.add(cbcAttackBtn, gbc);
        setContentPane(mainContainer);

    }

    public void display(int minWidth, int minHeight) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(minWidth, minHeight));
        pack();
        setLocationByPlatform(true);
        setVisible(true);
    }
}

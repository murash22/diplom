package windows;

import widgets.IntegerTextField;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DialogWindow {

    private static DialogWindow instance;
    public enum AttackType{
        CBC, ECB
    }

    HashMap<AttackType, String[]> secretVariants;
    private DialogWindow() {
        secretVariants = new HashMap<>();
        secretVariants.put(AttackType.ECB, secretVariantsECB);
        secretVariants.put(AttackType.CBC, secretVariantsCBC);
    }

    public static DialogWindow get() {
        if (instance == null) {
            instance = new DialogWindow();
        }
        return instance;
    }

    public byte[] visualizationDialog(String name, AttackType type) {
        IntegerTextField variant = new IntegerTextField(2);

        List<JComponent> inputs = new ArrayList<>(
                List.of(
                        new JLabel("Вариант"), variant
                )
        );

        int result = JOptionPane.showConfirmDialog(null, inputs.toArray(), name, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        int maxVariants = secretVariants.get(type).length;
        while (result == JOptionPane.OK_OPTION) {
            int v = variant.getText().length() > 0 ? Integer.parseInt(variant.getText()) : 0;
            if ( 0 < v && v <= maxVariants) {
                return secretVariants.get(type)[v-1].getBytes();
            }
            JOptionPane.showMessageDialog(null, "Доступные варианты: от 1 до " + maxVariants + ".", "Неправильный вариант.", JOptionPane.ERROR_MESSAGE);
            result = JOptionPane.showConfirmDialog(null, inputs.toArray(), name, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        }
        return null;
    }
    String[] secretVariantsECB = new String[] { // Только ASCII символы
            "I LOVE FOOTBALL",
            "cryptography",
    };

    String[] secretVariantsCBC = new String[] { // Только ASCII символы
            "        I LOVE FOOTBALL",
            "        cryptography",
    };




}

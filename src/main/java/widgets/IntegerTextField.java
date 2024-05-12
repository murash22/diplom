package widgets;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class IntegerTextField extends JTextField implements DocumentListener {

    public IntegerTextField() {
        super();
        postConstructor();
    }

    public IntegerTextField(int cols) {
        super(cols);
        postConstructor();
    }

    private void postConstructor() {
        getDocument().addDocumentListener(this);
    }

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
            String input = getText();
            StringBuilder res = new StringBuilder();
            for (int i = 0; i < Math.min(getColumns(), input.length()); i++) {
                if ("01233456789".indexOf(input.charAt(i)) >= 0){
                    res.append(input.charAt(i));
                }
            }
            setText(res.toString());
            filtering = false;
        });
    }
}


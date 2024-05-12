import windows.MainWindow;

import javax.swing.*;
import java.util.Locale;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        Locale.setDefault(new Locale("ru", "RU"));
        MainWindow window = new MainWindow("Дипломный проект");
        SwingUtilities.invokeLater(() -> window.display(600, 400));
//        algorithm.Algorithm alg = new algorithm.Algorithm("ecb");
//        byte[] encrypted = alg.encrypt("123".getBytes());
//        System.out.println(Arrays.toString(encrypted));
//        System.out.println(new String(alg.decrypt(encrypted)));
//        System.out.println(Arrays.toString(alg.decrypt(encrypted)));
    }
}
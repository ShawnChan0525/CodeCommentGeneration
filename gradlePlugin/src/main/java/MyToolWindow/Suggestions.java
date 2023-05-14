package MyToolWindow;

import javax.swing.*;

public class Suggestions {
    private JPanel panel1;
    private JRadioButton a1;
    private JRadioButton a2;
    private JRadioButton a3;
    private JRadioButton a4;
    private JRadioButton a5;
    private JRadioButton b1;
    private JRadioButton b2;
    private JRadioButton b3;
    private JRadioButton b4;
    private JRadioButton b5;
    private JRadioButton c1;
    private JRadioButton c2;
    private JRadioButton c3;
    private JRadioButton c4;
    private JRadioButton c5;
    private JTextField suggestionTextField;
    private JButton submitButton;
    private JButton button1;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Suggestions");
        frame.setContentPane(new Suggestions().panel1);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(350,400);
        frame.setLocation(500,200);
        frame.setVisible(true);
    }
    public JComponent getContent() {
        return panel1;
    }

}

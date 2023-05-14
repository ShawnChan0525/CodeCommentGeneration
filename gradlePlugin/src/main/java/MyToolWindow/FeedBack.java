package MyToolWindow;

import application.formatComment;
import com.intellij.openapi.ui.Messages;
import deepcommenter.HttpClientPool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FeedBack {
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
    private HashMap<String,String> resMap = new HashMap<String, String>();
    private static JFrame frame= new JFrame("Feedback");;

    public FeedBack() {
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String res = null;
                resMap.put("feedback","this is a feedback.");
                resMap.put("score","1");
                try {
                    res = HttpClientPool.getHttpClient().storeFeedbackPost("http://localhost:8080/store_feedback/", resMap);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Messages.showMessageDialog("XXXFailed to connect to server.", "information", Messages.getInformationIcon());
                    return;
                }
                JOptionPane.showMessageDialog(panel1,res);
            }
        });
    }
    public JComponent getContent() {
        return panel1;
    }

    //得到反馈
    public String getFeedback(){//feedback存在哪里，尚未处理
        return suggestionTextField.getText();
    }

    public static void show() {
        frame.setContentPane(new FeedBack().panel1);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(350,400);
        frame.setLocation(500,200);
        frame.setVisible(true);
    }

}

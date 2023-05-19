package action;


import MyToolWindow.MyToolWindow;
import application.Context;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;

import java.io.IOException;

import static MyToolWindow.Icons.LOGO;

public class generateCommentAction extends AnAction {

    private int judge;
    private Context context;
    private static final String ToolWindowName = "iCommenter";
    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取当前编辑器对象
        Project project = e.getProject();
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        context = new Context(project,editor);
        // 获取方法名
        String function = context.getFunctionName();
        if(function == null){
            Messages.showErrorDialog(e.getProject(), "您的输入中不合法，请重新输入","错误提示");
            return;
        }
        // 识别注释
        context.checkComment();
//        int judge;
        if(context.isHasCodeComment()){
            judge = Messages.showYesNoDialog(e.getProject(),"函数\"" + function + "\"已有注释，是否需要为其重新生成注释","提示","是","否",Messages.getQuestionIcon());
        }else{
            judge = Messages.showYesNoDialog(e.getProject(),"您是否要为函数\""+ function + "\"生成注释","提示","是","否",Messages.getQuestionIcon());
        }
        // TODO: 2023/3/12 这里的1变成了魔法数字，会让人摸不着头脑，可以声明成 private static final int NoText = 1;
        if(judge == 1){
            return;
        }
        // 获取函数体
        try {
            context.getBody();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // TODO: 2023/3/12 为什么在这里判断project为空，而不是在获取project的时候判断？而且若project为空，也走不到这里吧？
        if (project == null)
        {
            return;
        }
        ToolWindowManager toolWindowMgr = ToolWindowManager.getInstance(project);
        ToolWindow tw = toolWindowMgr.getToolWindow(ToolWindowName);
        if (tw == null)
        {
            tw = toolWindowMgr.registerToolWindow(ToolWindowName, true, ToolWindowAnchor.RIGHT, true);
        }
        final ToolWindow toolWindow = tw;
        toolWindow.activate(() -> updateContent(toolWindow, project.getName()), true);
    }
    private void updateContent(ToolWindow toolWindow, String projectName)
    {
        MyToolWindow myToolWindow = new MyToolWindow(context);
        myToolWindow.setComment(context.comments);
//        ImageIcon imageIcon = new ImageIcon(icon.);       //预载图片，必须这么做否则会出错
//        label1.setIcon(imageIcon);
//        LOGO.paintIcon();
        toolWindow.setIcon(LOGO);
        ContentManager contentManager = toolWindow.getContentManager();
        contentManager.removeAllContents(true);
        Content content = contentManager.getFactory().createContent(myToolWindow.getContent(), "Comments", false);
        contentManager.addContent(content);
        content.setDescription("Description");
    }
}

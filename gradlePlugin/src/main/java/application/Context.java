package application;

import UploadAndDownload.MyClient;
import Util.Symbol;
import Util.utils;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import deepcommenter.HttpClientPool;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Properties;

public class Context {

    private static final String BEGINNING = "def";
    /** 选中的代码文本 **/
    private String code;
    /** 工程对象 **/
    private Project project;
    /** 文件 **/
    private Document document;
    /** 编辑器 **/
    private Editor editor;
    /** 编辑器中的全部文本 **/
    private String[] text;

    /** 文件中的选中部分 **/
    private SelectionModel selectionModel;
    /** 相关数据 **/
    private int functionBegin;
    private int functionEnd;
    private String name;
    private int model_id;

    // 包括'''
    private int commentBegin;
    private int commentEnd;// f unctionEnd - 1
    /** 缩进量 */
    private int indent;
    /** 注释的形式 */
    private int comment_format;

    /** 判断是否为合法的方法 */
    public boolean isValidFunction;

    /** 是否已有代码注释 **/
    private boolean hasCodeComment;

    /** 返回的代码注释 **/
    public formatComment[] comments;

    private final MessageBusConnection connection;

    public Context(Project p, Editor e) {
        project = p;
        editor = e;
        document = e.getDocument();
        selectionModel = e.getSelectionModel();
        text = document.getText().split("\n");

        code = null;
        functionBegin = functionEnd = -1;
        commentBegin = commentEnd = -1;
        hasCodeComment = false;
        comments = new formatComment[4];
        indent = 0;
        name = null;
        model_id = -1;
        comment_format = 0;
        connection = project.getMessageBus().connect();
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,
                new FileEditorManagerListener() {
                    @Override
                    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                        System.out.println("selection change");
                        if (check()) {
                            connection.disconnect();
                        }
                    }

                    @Override
                    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                        System.out.println("closed");
                        if (check()) {
                            connection.disconnect();
                        }
                    }

                });
    }

    public Project getProject() {
        return project;
    }

    public boolean isHasCodeComment() {
        return hasCodeComment;
    }

    // TODO: 2023/3/20 正确的实现逻辑应该是先框定函数体，再找到FunctionName和comment

    public String getBody2() {
        String functionBody = null;
        int start = selectionModel.getSelectionStartPosition().line; // 用户框选的起始行
        int end = selectionModel.getSelectionEndPosition().line; // 用户框选的结束行
        int indent = 0; // 用于计算方法起始行缩进量
        int pointer = 0;
        String[] text = document.getText().split("\n");
        for (pointer = start; pointer >= 0 && !text[pointer].strip().startsWith(BEGINNING); --pointer)
            ; // "def"
        functionBegin = pointer;
        if (functionBegin == -1)
            return null;
        pointer = end == functionBegin ? end + 1 : end; // 通过缩进判断函数体的end，因此必须从起始行的下一行开始
        functionEnd = functionBegin;
        indent = utils.getSpaceSize(text[functionBegin]); // 获取某行缩进量的方法
        for (; pointer < text.length; ++pointer) {
            if (utils.getSpaceSize(text[pointer]) > indent) {
                functionEnd = pointer;
            } else
                break;
        }
        isValidFunction = functionEnd - functionBegin <= 50;
        TextRange textRange = new TextRange(document.getLineEndOffset(functionBegin),
                document.getLineEndOffset(functionEnd + 1));
        functionBody = document.getText(textRange);
        return functionBody;
    }

    public String getFunctionName2() {
        TextRange textRange = new TextRange(document.getLineEndOffset(functionBegin),
                document.getLineEndOffset(functionBegin + 1));
        String startLine = document.getText(textRange);
        int startIdx = startLine.indexOf(BEGINNING) + 4;
        int endIdx = startLine.indexOf('(');
        return startLine.substring(startIdx, endIdx).strip();
    }

    public String getComment() {
        String comment = null;
        hasCodeComment = false;
        int pointer = functionBegin + 1;
        TextRange textRange;
        if (text[pointer].strip().startsWith("\"\"\"")) {
            commentBegin = pointer;
            for (; pointer <= functionEnd; ++pointer) {
                if (text[pointer].strip().endsWith("\"\"\"")) {
                    commentEnd = pointer;
                    textRange = new TextRange(document.getLineEndOffset(commentBegin),
                            document.getLineEndOffset(commentEnd + 1));
                    comment = document.getText(textRange).strip();
                    hasCodeComment = true;
                    break;
                }
            }
        }
        return comment;
    }

    public String getFunctionName() {
        String function_name = null;
        VisualPosition startPosition = selectionModel.getSelectionStartPosition();
        VisualPosition endPosition = selectionModel.getSelectionEndPosition();
        int start = startPosition.line;
        int end = endPosition.line;
        boolean isContain = false;
        boolean isFind = false;
        int compare = 0;
        String[] text = document.getText().split("\n");
        for (int i = start; i <= end; i++) {
            if (text[i].contains("def")) {
                if (!isFind && !isContain) {
                    functionBegin = i;
                    isContain = true;
                    isFind = true;
                    compare = utils.getSpaceSize(text[functionBegin]);
                } else {
                    return null;
                }
            }
            if (!isContain && !text[i].equals("")) {
                isContain = true;
            }
            if (isFind && i != functionBegin && utils.getSpaceSize(text[i]) == compare) {
                functionEnd = i - 1; // 找到末尾
                break;
            }
        }
        if (functionBegin == -1) {
            int i;

            for (i = start - 1; i >= 0 && !text[i].contains("def"); i--)
                ;
            if (i == -1) {
                return null;
            }
            functionBegin = i;
        }
        if (functionEnd == -1) {
            int i = end == functionBegin ? end + 1 : end;
            compare = utils.getSpaceSize(text[functionBegin]);

            for (; i < text.length && compare != utils.getSpaceSize(text[i]); i++)
                ;
            functionEnd = i - 1;
        }
        String[] tmp = text[functionBegin].split(" ");
        int k;

        for (k = 0; k < tmp.length && !tmp[k].equals("def"); k++) {
        }
        function_name = tmp[k + 1].split("\\(")[0];
        // test
        System.out.println("functionBegin: " + functionBegin);
        System.out.println("functionEnd: "+functionEnd);

        name = text[functionBegin];
        indent = compare + 4; // 应该是begin+1行的空格数-begin行的空格数，这里直接写的4
        return function_name;
    }

    public void checkComment() {
        String[] text = document.getText().split("\n");
        hasCodeComment = false;
        String commentBeginLine = text[functionBegin + 1].strip();
        if(commentBeginLine.startsWith("\"\"\"")){
            hasCodeComment = true;
            commentBegin = functionBegin + 1;
            commentEnd = functionBegin + 1;
            if (commentBeginLine.endsWith("\"\"\"") && commentBeginLine.length() >= 7){}// 单行注释
            else{
                for (int i = functionBegin + 2; i <= functionEnd; ++i) {
                    if (text[i].strip().endsWith("\"\"\"")) {
                        commentEnd = i;
                        break; //多行注释
                    }
                }}
        }
        System.out.println("commentBegin: " + commentBegin);
        System.out.println("commentEnd: " + commentEnd);
    }

    public void getBody() throws IOException {
        int insertOffset = document.getLineStartOffset(functionBegin);
        TextRange textRange = new TextRange(insertOffset, document.getLineEndOffset(functionEnd));
        code = document.getText(textRange);
        // test
        System.out.println("code: " + code);

        ProgressManager.getInstance().run(
                new Task.Modal(project, "Generating Comment", true) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        indicator.setIndeterminate(true);
                        try {
                            String res_comment = HttpClientPool.getHttpClient().post("http://localhost:8080/summary/", code);
                            if (indicator.isCanceled()) {
                                throw new RuntimeException();
                            }
                            try {
                                Thread.sleep(700);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            String[] receive = res_comment.split(",,");
                            comments[0] = new formatComment(receive[0], 0, indent, comment_format);
                            comments[1] = new formatComment(receive[1], 1, indent, comment_format);
                            comments[2] = new formatComment(receive[2], 2, indent, comment_format);
                            comments[3] = new formatComment(receive[3], 3, indent, comment_format);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Messages.showMessageDialog("XXXFailed to connect to server.", "information", Messages.getInformationIcon());
                            return;
                        }

                    }
                });
    }

    public void insert(int index) {
        String s = comments[index].getFormatComment();
        int lineSum = comments[index].getLine();
        System.out.println("lineSum:" + lineSum);
        Runnable runnable;
        String space = utils.getSpace(indent);
        System.out.println("name: " + name);
        System.out.println("indent: " + indent);
        String symbol = Symbol.get(comment_format);

        if (hasCodeComment) {
            int firstOffset = document.getLineStartOffset(commentBegin);
            int endOffset = document.getLineStartOffset(commentEnd+1);
            String before_total = document.getText(new TextRange(firstOffset, endOffset));
            String before = before_total.replace("\"\"\"", "");
            int judge = Messages.showYesNoDialog(project, "已存在注释:\n" + before.replace("\n","") + "\n请选择您的插入方式","提示","直接插入","覆盖原有注释",
                    Messages.getQuestionIcon());
            if(judge == Messages.YES){
                runnable = () -> document.replaceString(firstOffset,endOffset,space + "\"\"\"" + before + s + space + "\"\"\"\n");
                // 单行注释，多的一行是空格
                if(commentEnd == commentBegin){
                    commentEnd += lineSum + 1;
                    functionEnd += lineSum + 1;
                } else {
                    commentEnd += lineSum + 3;
                    functionEnd += lineSum + 3;
                }
            } else if(judge == Messages.NO){
                runnable = () -> document.replaceString(firstOffset,endOffset,space + "\"\"\"\n" + s + space + "\"\"\"\n");
                int tmp = commentEnd;
                commentEnd = commentBegin + lineSum + 1;
                tmp = commentEnd - tmp;
                functionEnd += tmp;
            }
            else{return;}
        } else {
            int insertOffset = document.getLineEndOffset(functionBegin);
            runnable = () -> document.insertString(insertOffset,"\n" + space + "\"\"\"\n" + s + space + "\"\"\"");
            commentBegin = functionBegin + 1;
            commentEnd = commentBegin + lineSum + 1;
            functionEnd += lineSum + 2;
            hasCodeComment = true;
        }
        WriteCommandAction.runWriteCommandAction(project, runnable);
        model_id = index;
        // test
        System.out.println("insert commment: commentBegin: " + commentBegin);
        System.out.println("commentEnd: " + commentEnd);
        System.out.println("functionBegin: " + functionBegin);
        System.out.println("functionEnd" + functionEnd);
    }

    private boolean check() {
        String[] s = document.getText().split("\n");
        for (int i = 0; i < s.length; i++) {
            if (s[i].contains(name)) {
                hasCodeComment = false;
                functionBegin = i;
                this.checkComment();
                String change = document.getText(new TextRange(document.getLineStartOffset(commentBegin),
                        document.getLineEndOffset(commentEnd)));
                System.out.println(model_id + ":");
                System.out.println(change);
                return true;
            }
        }
        return false;
    }
}

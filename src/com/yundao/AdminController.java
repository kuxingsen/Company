package com.yundao;

import bean.Column;
import bean.Message;
import bean.Result;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import utils.DateUtils;
import utils.Md5Utils;
import utils.YundaoDbUtil;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController extends CommonService{

    @RequestMapping(value = "adminLogin", method = RequestMethod.POST)
    @ResponseBody
    public Result adminLogin(String username, String password, HttpSession session) {
        int adminId;
        if((adminId = getAdminId(username, password)) != 0){
            session.setAttribute("username",username);
            session.setAttribute("adminId",adminId);
            session.setAttribute("key", Md5Utils.key(String.valueOf(adminId)));
            return new Result(200);
        }
        return new Result(500, "用户名或密码错误");
    }

    @RequestMapping(value = "adminLoginOut")
    @ResponseBody
    public Result adminLoginOut( HttpSession session) {
        session.setAttribute("username",null);
        session.setAttribute("adminId",null);
        session.setAttribute("key", null);
        return new Result(200);
    }

    @RequestMapping("getUsername")
    @ResponseBody
    public Result<String> getUsername(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if(username != null) {
            List<String> datas = new ArrayList<>();
            datas.add(username);
            return new Result<>(200, datas);
        }
        return new Result<>(500, "未登录");
    }

    @RequestMapping("updateAdmin")
    @ResponseBody
    public Result updateAdmin(HttpSession session, String username, String oldPassword,String newPassword) {
        int adminId = (int) session.getAttribute("adminId");
        if(adminId == getAdminId(username, oldPassword)){
            int result;
            String sql = "update admin set name=?,password=? where id=?";
            try {
                PreparedStatement ps = YundaoDbUtil.executePreparedStatement(sql);
                ps.setString(1, username);
                ps.setString(2, newPassword);
                ps.setInt(3, adminId);
                result = ps.executeUpdate();
                System.out.println(result);
                if(result > 0) {
                    session.setAttribute("username", username);
                    return new Result(200);
                }
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
        return new Result<>(500, "更新失败");
    }

    @RequestMapping("insertMessage")
    @ResponseBody
    public Result insertMessage(@RequestParam(value = "titleImg", required = false) MultipartFile titleImg, @RequestParam(value = "file", required = false) MultipartFile file, Message message, HttpSession session) {
        System.out.println("insert");
        int adminId = (int) session.getAttribute("adminId");
        String imgPath = null, filePath = null;
        if(titleImg != null) {
            imgPath = saveTitleImg(titleImg);
        }
        if(file != null) {
            filePath = saveFile(file);
        }
        System.out.println(message);
        int result;
        String sql = "insert into message(title,title_img,date,content,file,is_push,menu_id,admin_id) values(?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = YundaoDbUtil.executePreparedStatement(sql);
            ps.setString(1, message.getTitle());
            ps.setString(2, imgPath);
            ps.setString(3, DateUtils.today("yyyy-MM-dd"));
            ps.setString(4, message.getContent());
            ps.setString(5, filePath);
            ps.setInt(6, Integer.parseInt(message.getIsPushed()));
            ps.setInt(7, Integer.parseInt(message.getColumnId()));
            ps.setInt(8, adminId);
            result = ps.executeUpdate();
//            System.out.println(result);
            if(result > 0) {
                return new Result(200);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return new Result<>(500, "插入失败");
    }

    @RequestMapping("updateMessage")
    @ResponseBody
    public Result updateMessage(@RequestParam(value = "titleImg", required = false) MultipartFile titleImg, @RequestParam(value = "file", required = false) MultipartFile file, Message message, HttpSession session) {
        String imgPath = null, filePath = null;
        if(titleImg != null) {
            imgPath = saveTitleImg(titleImg);
        }
        if(file != null) {
            filePath = saveFile(file);
        }
        int result;
        String sql = "update message set title=?,title_img=?,content=?,filePath=?,is_push=?,menu_id=?,admin_id=? where id=?";
        try {
            PreparedStatement ps = YundaoDbUtil.executePreparedStatement(sql);
            ps.setString(1, message.getTitle());
            ps.setString(2, imgPath);
            ps.setString(3, message.getContent());
            ps.setString(4, filePath);
            ps.setInt(5, Integer.parseInt(message.getIsPushed()));
            ps.setInt(6, Integer.parseInt(message.getColumnId()));
            ps.setInt(7, (Integer) session.getAttribute("adminId"));
            ps.setString(8, message.getId());
            result = ps.executeUpdate();
            System.out.println(result);
            if(result > 0) {
                return new Result(200);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return new Result<>(500, "更新失败");
    }

    @RequestMapping("deleteMessage")
    @ResponseBody
    public Result deleteMessage(int messageId) {
        int result;
        String sql = "delete from message where id = ?";
        try {
            PreparedStatement ps = YundaoDbUtil.executePreparedStatement(sql);
            ps.setInt(1, messageId);
            result = ps.executeUpdate();
            System.out.println(result);
            if(result > 0) {
                return new Result(200);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return new Result(500, "删除失败");
    }

    @RequestMapping("selectMessage")
    @ResponseBody
    public Result<Message> selectMessage() {
        String sql = "select * from message mess,menu where 1=? and mess.menu_id=menu.id";
        List<Message> messageList = getMessageResult("1", sql);
        if(messageList != null) return new Result<>(200, messageList);
        return new Result<>(500, "没有相应的记录");
    }

    @RequestMapping("selectMessageByMessageId")
    @ResponseBody
    public Result<Message> selectMessageByMessageId(String messageId) {
        String sql = "select * from message mess,menu where mess.id=? and mess.menu_id=menu.id";
        List<Message> messageList = getMessageResult(messageId, sql);
        if(messageList != null) return new Result<>(200, messageList);
        return new Result<>(500, "没有相应的记录");
    }

    @RequestMapping("selectMessageByColumnId")
    @ResponseBody
    public Result<Message> selectMessageByColumnId(String columnId) {
        String sql = "select * from message mess,menu where mess.menu_id=? and mess.menu_id=menu.id";
        List<Message> messageList = getMessageResult(columnId, sql);
        if(messageList != null) return new Result<>(200, messageList);
        return new Result<>(500, "没有相应的记录");
    }

    @RequestMapping("selectColumn")
    @ResponseBody
    public Result<Column> selectColumn() {
        List<Column> columnList = getColumn();
        if(columnList != null) return new Result<>(200,columnList);
        return new Result<>(500, "没有相应的记录");
    }


    //private
    private int getAdminId(String username, String oldPassword) {
        ResultSet result;
        String sql = "select id from admin where name=? and password=?";
        try {
            PreparedStatement ps = YundaoDbUtil.executePreparedStatement(sql);
            ps.setString(1, username);
            ps.setString(2, oldPassword);
            result = ps.executeQuery();
            if(result != null && result.next()) {
//                System.out.println(result.getInt(1));
                return result.getInt("id");
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private List<Message> getMessageResult(String searchId, String sql) {
        ResultSet result;
        try {
            PreparedStatement ps = YundaoDbUtil.executePreparedStatement(sql);
            ps.setInt(1, Integer.parseInt(searchId));
            result = ps.executeQuery();
            if(result != null) {
                List<Message> messageList = new ArrayList<>();
                while(result.next()) {
                    Message message = new Message();
                    message.setId(result.getString("id"));
                    message.setTitle(result.getString("title"));
                    message.setTitleImgPath(result.getString("title_img"));
                    message.setDate(result.getString("date"));
                    message.setColumnId(String.valueOf(result.getInt("menu_id")));
                    message.setColumnName(result.getString("name"));
                    message.setContent(result.getString("content"));
                    message.setIsPushed(String.valueOf(result.getInt("is_push")));
                    String filePath = result.getString("file");
                    if(filePath!= null){
                        filePath = filePath.substring(filePath.indexOf("_")+1);
                    }else {
                        filePath = "";
                    }
                    message.setFilePath(filePath);

                    messageList.add(message);
                }
                return messageList;
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String saveFile(MultipartFile file) {
        String millis = System.currentTimeMillis() + "";
        File dir = new File(FilePath.FILE_PATH);
        if(!dir.getParentFile().exists()) {
            boolean mk = dir.getParentFile().mkdirs();
            System.out.println("create: yundao_dir " + mk);
        }
        if(!dir.exists()) {
            boolean mk = dir.mkdirs();
            System.out.println("create file_path_dir:" + mk);
        }
        String fileName = millis + "_" + file.getOriginalFilename();
        File uploadFile = new File(FilePath.FILE_PATH, fileName);
        if(copyFile(file, uploadFile)) return fileName;
        return null;
    }

    private String saveTitleImg(MultipartFile titleImg) {
        String millis = System.currentTimeMillis() + "";
        File dir = new File(FilePath.TITLE_IMG_PATH);
        if(!dir.getParentFile().exists()) {
            boolean mk = dir.getParentFile().mkdirs();
            System.out.println("create: yundao_dir " + mk);
        }
        if(!dir.exists()) {
            boolean mk = dir.mkdirs();
            System.out.println("create title_img_path_dir:" + mk);
        }
        String originalFileName = titleImg.getOriginalFilename();
        String fileName = millis + "_" + originalFileName.substring(originalFileName.lastIndexOf("."));
        File uploadFile = new File(FilePath.TITLE_IMG_PATH, fileName);
//        System.out.println(fileName);
        if(copyFile(titleImg, uploadFile)) return fileName;
        return null;
    }

    private boolean copyFile(MultipartFile titleImg, File uploadFile) {
        try {
            FileUtils.copyInputStreamToFile(titleImg.getInputStream(), uploadFile);
        } catch(IOException e) {
            e.printStackTrace();
            System.err.println("filePath is not save");
            return false;
        }
        return true;
    }
}

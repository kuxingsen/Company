package com.yundao;

import bean.Banner;
import bean.Column;
import bean.Message;
import bean.Result;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import utils.YundaoDbUtil;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UserController extends CommonService{

    @RequestMapping("selectColumn")
    @ResponseBody
    public Result<Column> selectColumn() {
        List<Column> columnList = getColumn();
        if(columnList != null) return new Result<>(200,columnList);
        return new Result<>(500, "没有相应的记录");
    }

    @RequestMapping("selectBanner")
    @ResponseBody
    public Result<Banner> selectBanner() {
        String sql = "select id,title,title_img from message where is_push = 1 order by id desc limit 0,5";
        ResultSet result;
        try {
            PreparedStatement ps = YundaoDbUtil.executePreparedStatement(sql);
            result = ps.executeQuery();
            System.out.println(result);
            if(result != null) {
                List<Banner> bannerList = new ArrayList<>();
                while(result.next()) {
                    Banner banner = new Banner();
                    banner.setId(result.getString("id"));
                    banner.setTitle(result.getString("title"));
                    banner.setTitle(result.getString("title_img"));

                    bannerList.add(banner);
                }
                System.out.println(bannerList);
                return new Result<>(200, bannerList);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return new Result<>(500, "没有相应的记录");
    }

    @RequestMapping("selectMessageByMessageId")
    @ResponseBody
    public Result<Message> selectMessageByMessageId(String messageId) {
        String sql = "select mess.id,title,date,content,file from message mess,menu where mess.id=? and mess.menu_id=menu.id";
        List<Message> messageList = getMessageResult(messageId, sql);
        if(messageList != null) return new Result<>(200, messageList);
        return new Result<>(500, "没有相应的记录");
    }

    @RequestMapping("selectMessageByColumnId")
    @ResponseBody
    public Result<Message> selectMessageByColumnId(String columnId,@RequestParam(defaultValue = "1") int page) {
        int count = 5;
        int index = count*(page-1);
        String sql = "select mess.id,title,date,content,file from message mess,menu where mess.menu_id=? and mess.menu_id=menu.id limit "+index+","+count;
        List<Message> messageList = getMessageResult(columnId, sql);
        if(messageList != null) return new Result<>(200, messageList);
        return new Result<>(500, "没有相应的记录");
    }

    @RequestMapping("downFile")
    public ResponseEntity<byte[]> downFile(String messageId) throws IOException {
        String sql = "select file from message where id=?";

        ResultSet result;
        String fileName = null;
        try {
            PreparedStatement ps = YundaoDbUtil.executePreparedStatement(sql);
            ps.setString(1, messageId);
            result = ps.executeQuery();
            if(result != null) {
                while(result.next()) {
                    fileName = result.getString("file");
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        if(fileName != null) {
            File downFile = new File(FilePath.FILE_PATH, fileName);
            if (!downFile.getParentFile().exists()) {
                boolean mk=downFile.getParentFile().mkdirs();
                System.err.println("create:"+mk);
            }

            System.out.println(fileName);
            HttpHeaders headers = new HttpHeaders();

            //下载显示的文件名，解决中文名称乱码问题
            String downloadFielName = new String(fileName.getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.ISO_8859_1);
            //通知浏览器以attachment（下载方式）
            headers.setContentDispositionFormData("attachment", downloadFielName);
            //application/octet-stream ： 二进制流数据（最常见的文件下载）。
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            /*
              解决IE不能下载文件问题
             */
            return new ResponseEntity<>(FileUtils.readFileToByteArray(downFile),
                    headers, HttpStatus.OK);
        }
        return null;
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
                    message.setDate(result.getString("date"));
                    message.setContent(result.getString("content"));
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

}

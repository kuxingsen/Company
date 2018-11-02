package bean;

public class Message{
    public String id;
    //标题
    public String title;
    //标题图
    public String titleImgPath
            ;
    //发布时间
    public String date;
    //栏目id及栏目名
    public String columnId;
    public String columnName;
    //富文本内容
    public String content;
    //附件路径
    public String filePath;
    //是否推送
    public String isPushed;

    public Message() {
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", titleImgPath='" + titleImgPath + '\'' +
                ", date='" + date + '\'' +
                ", columnId='" + columnId + '\'' +
                ", columnName='" + columnName + '\'' +
                ", content='" + content + '\'' +
                ", filePath='" + filePath + '\'' +
                ", isPushed=" + isPushed +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleImgPath() {
        return titleImgPath;
    }

    public void setTitleImgPath(String titleImgPath) {
        this.titleImgPath = titleImgPath;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getIsPushed() {
        return isPushed;
    }

    public void setIsPushed(String isPushed) {
        this.isPushed = isPushed;
    }
}

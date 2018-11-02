package com.yundao;

import bean.Column;
import bean.Result;
import utils.YundaoDbUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommonService{



    protected List<Column> getColumn() {
        String sql = "select * from menu";
        ResultSet result;
        try {
            PreparedStatement ps = YundaoDbUtil.executePreparedStatement(sql);
            result = ps.executeQuery();
            System.out.println(result);
            if(result != null) {
                List<Column> columnList = new ArrayList<>();
                while(result.next()) {
                    Column column = new Column();
                    column.setId(result.getString("id"));
                    column.setName(result.getString("name"));

                    columnList.add(column);
                }
                return columnList;
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

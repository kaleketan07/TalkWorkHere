package edu.northeastern.ccs.im.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBUtils {


    public PreparedStatement setUserDetails(PreparedStatement stmt, Object ... args) throws SQLException{
        int i = 1;
        for(Object arg:args){
            if(arg instanceof String)
                stmt.setString(i++,(String) arg);
            else if(arg instanceof Integer)
                stmt.setInt(i++,(int) arg);
        }
        return stmt;
    }
}

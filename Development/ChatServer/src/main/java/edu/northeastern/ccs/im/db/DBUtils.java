package edu.northeastern.ccs.im.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBUtils {

    /**
     * Sets prepared statement arguments. This is used to prepare any preparedStatement with
     * any no. of arguments, this replaces the tedious use of preparedStmt.set() methods.
     *
     * @param stmt the PreparedStatement to set
     * @param args the arguments that need to be set in the statement
     * @return the prepared statement arguments (currently can accept only String and Int)
     * @throws SQLException the sql exception
     */
    public PreparedStatement setPreparedStatementArgs(PreparedStatement stmt, Object ... args) throws SQLException{
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

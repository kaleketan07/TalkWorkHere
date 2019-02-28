package edu.northeastern.ccs.im.exceptions;

public class UniqueFieldException extends Exception {

    public UniqueFieldException() { super(); }

    public UniqueFieldException(String msg){
        super(msg);
    }
}

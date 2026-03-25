package com.futsalmanager.application.exceptions;

public class BusinessException extends RuntimeException{

    public BusinessException(String mensage){
        super(mensage);
    }
}

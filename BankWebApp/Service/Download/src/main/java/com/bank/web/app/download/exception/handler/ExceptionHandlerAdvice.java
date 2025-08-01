package com.bank.web.app.download.exception.handler;

import com.bank.web.app.download.dto.ResponseDTO;
import com.bank.web.app.download.exception.InvalidAccountRequest;
import com.bank.web.app.download.exception.UserNotFoundException;
import com.bank.web.app.download.redis.RedisService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseDTO handlerUserNotFoundHandler(UserNotFoundException exception){
        return new ResponseDTO("400",exception.getMessage(),null);
    }

    @ExceptionHandler(InvalidAccountRequest.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseDTO handlerInvalidAccountHandler(InvalidAccountRequest exception){
        return new ResponseDTO("400",exception.getMessage(),null);
    }


}

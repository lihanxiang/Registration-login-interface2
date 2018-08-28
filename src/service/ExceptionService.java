package service;

import config.exception.UserException;
import domain.User;
import org.springframework.stereotype.Service;

public interface ExceptionService {

    //_user 是从数据库中查找出的记录，user 是用户输入
    void loginException(User user, User db_user) throws UserException;

    void addUserException1(User user) throws UserException;

    void addUserException2(User user) throws UserException;

    void verifyCodeException(String input_code, String code) throws UserException;

    void setInfoException(User user) throws UserException;

    void statusException(String username) throws UserException;
}

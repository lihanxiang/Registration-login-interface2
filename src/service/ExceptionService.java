package service;

import config.exception.UserException;
import domain.User;
import org.springframework.stereotype.Service;

@Service
public interface ExceptionService {

    //_user 是从数据库中查找出的记录，user 是用户输入
    public void loginException(User user, User db_user) throws UserException;

    public void addUserException1(User user) throws UserException;

    public void addUserException2(User user) throws UserException;
}

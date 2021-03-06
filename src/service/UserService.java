package service;

import exception.UserException;
import domain.User;

public interface UserService {
    void addUser(User user) throws UserException;
    void login(User user) throws UserException;
    User showInfo(String username);
    String getStatus(String username);
    void setUserInfo(User user) throws UserException;
    void verifyCode(String userCode, String verifyCode) throws UserException;
    User findUserByName(String username);
}

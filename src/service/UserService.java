package service;

import config.exception.UserException;
import domain.User;
import util.VerifyCode;

import javax.servlet.http.HttpServletRequest;

public interface UserService {
    void addUser(User user) throws UserException;
    void login(User user) throws UserException;
    User showInfo(String username);
    String getStatus(String username);
    void setUserInfo(User user) throws UserException;
    void verifyCode(String userCode, String verifyCode) throws UserException;
}

package service;

import config.exception.UserException;
import domain.User;
import util.VerifyCode;

public interface UserService {
    public void addUser(User user) throws UserException;
    public void login(User user) throws UserException;
}

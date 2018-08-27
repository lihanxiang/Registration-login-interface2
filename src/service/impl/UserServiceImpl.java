package service.impl;

import config.exception.UserException;
import domain.User;
import mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.ExceptionService;
import service.UserService;
import util.VerifyCode;

@Service
public class UserServiceImpl implements UserService{

    private final UserMapper userMapper;

    private final ExceptionService exceptionService;

    @Autowired
    public UserServiceImpl(UserMapper userMapper, ExceptionService exceptionService) {
        this.userMapper = userMapper;
        this.exceptionService = exceptionService;
    }

    public void addUser(User user) throws UserException {
        //先判断用户的输入是否有错
        exceptionService.addUserException1(user);
        //再判断用户的信息是否已被注册
        exceptionService.addUserException2(user);
        userMapper.addUser(user);
    }

    //根据用户输入名字去数据库查找有没有这个用户，如果没有，就会抛出异常
    public void login(User user) throws UserException {
        User db_user = userMapper.findUserByName(user.getUsername());
        exceptionService.loginException(user, db_user);
    }
}

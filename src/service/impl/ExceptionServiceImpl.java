package service.impl;

import config.exception.UserException;
import domain.User;
import mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import service.ExceptionService;

public class ExceptionServiceImpl implements ExceptionService {

    private final UserMapper userMapper;

    @Autowired
    public ExceptionServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public void addUserException1(User user) throws UserException{
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()){
            throw new UserException("用户名不能为空");
        }else if (user.getUsername().length() < 5 || user.getUsername().length() > 15){
            throw new UserException("用户名必须为5-15个字符");
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()){
            throw new UserException("密码不能为空");
        }else if (user.getPassword().length() < 5 || user.getPassword().length() > 15){
            throw new UserException("密码必须为5-15个字符");
        }

        if (user.getPhone() == null || user.getPhone().trim().isEmpty()){
            throw new UserException("电话号码不能为空");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()){
            throw new UserException("邮箱不能为空");
        }
    }

    public void addUserException2(User user) throws UserException{
        //这三者都必须是唯一的
        if (userMapper.findUserByName(user.getUsername()) != null){
            throw new UserException("该用户名已被注册");
        } else if (userMapper.findUserByPhone(user.getPhone()) != null){
            throw new UserException("该电话号码已被注册");
        } else if (userMapper.findUserByEmail(user.getEmail()) != null){
            throw new UserException("该邮箱已被注册");
        }
    }

    public void loginException(User user, User db_user) throws UserException {

        if(db_user == null){
            throw new UserException("该用户不存在");
        }
        if(!user.getPassword().equals(db_user.getPassword())){
            throw new UserException("密码错误");
        }
    }
}

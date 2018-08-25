package controller;

import domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import config.exception.UserException;
import service.ExceptionService;
import service.UserService;
import util.VerifyCode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Controller
public class UserController {

    private final UserService userService;

    private final VerifyCode verifyCode;

    private final ExceptionService exceptionService;

    @Autowired
    public UserController(UserService userService, VerifyCode verifyCode, ExceptionService exceptionService) {
        this.userService = userService;
        this.verifyCode = verifyCode;
        this.exceptionService = exceptionService;
    }

    //在注册之前需要先得到注册的界面
    @RequestMapping("/preAdd")
    public ModelAndView preAdd(){
        return new ModelAndView("addUser");
    }

    @RequestMapping("/addUser")
    public ModelAndView addUser(User user, HttpServletRequest request){
        ModelAndView modelAndView;
        //如果下面的 try 语句块没有抛出异常，则返回 addUserSuccessful.jsp
        modelAndView = new ModelAndView("addUserSuccessful");
        try{
            //先调用添加用户的方法，看看有没有因为不符规定的输入而导致异常抛出
            userService.addUser(user);
            //然后再看有没有因为验证码错误而导致异常抛出
            exceptionService.verifyCodeException(request.getParameter("verifyCode"), verifyCode.getText());
        } catch (UserException e){
            //如果捕获异常，就带着异常信息返回注册界面
            modelAndView = new ModelAndView("addUser");
            modelAndView.addObject("message", e.getMessage());
        }
        return modelAndView;
    }

    //同样的，需要先得到界面
    @RequestMapping("preLogin")
    public ModelAndView preLogin(){
        return new ModelAndView("login");
    }

    //登陆的逻辑和上面是一样的
    @RequestMapping("/login")
    public ModelAndView login(User user, HttpServletRequest request) {
        ModelAndView modelAndView;
        modelAndView = new ModelAndView("loginSuccessful");
        try {
            userService.login(user);
            exceptionService.verifyCodeException(request.getParameter("verifyCode"), verifyCode.getText());
        } catch (UserException e){
            modelAndView = new ModelAndView("login");
            modelAndView.addObject("message", e.getMessage());
        }
        return modelAndView;
    }

    //得到验证码，然后用于 jsp 文件的 <img> 标签的 src 属性中
    @RequestMapping("/getVerifyCode")
    public void setVerifyCode(HttpServletResponse response)
            throws IOException{
        //设置响应格式
        response.setContentType("image/jpg");
        //得到图片
        BufferedImage image = verifyCode.getImage();
        //输出
        verifyCode.output(image, response.getOutputStream());
    }
}

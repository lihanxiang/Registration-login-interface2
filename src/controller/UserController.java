package controller;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import exception.UserException;
import domain.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
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
        //如果下面的 try 语句块没有抛出异常，则返回 addUserSuccessful.jsp
        ModelAndView modelAndView = new ModelAndView("addUserSuccessful");
        try{
            //先调用添加用户的方法，看看有没有因为不符规定的输入而导致异常抛出
            userService.addUser(user);
            //然后再看有没有因为验证码错误而导致异常抛出
            exceptionService.verifyCodeException(request.getParameter("verifyCode"), verifyCode.getText());
        } catch (UserException e){
            //如果捕获异常，就带着异常信息返回注册界面
            modelAndView.setViewName("addUser");
            modelAndView.addObject("message", e.getMessage());
        }
        return modelAndView;
    }

    //同样的，需要先得到界面
    @RequestMapping("preLogin")
    public ModelAndView preLogin(){
        return new ModelAndView("login");
    }

    @RequestMapping("/login")
    public ModelAndView login(User user, HttpServletRequest request){
        ModelAndView modelAndView = new ModelAndView("main");
        //得到 Subject
        Subject subject = SecurityUtils.getSubject();
        //获取 token，用于验证
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(user.getUsername(), user.getPassword());
        try {
            //先检测关于验证码的输入
            userService.verifyCode(request.getParameter("verifyCode"), verifyCode.getText());
            //尝试登录
            subject.login(usernamePasswordToken);
            request.getSession().setAttribute("username", user.getUsername());
            modelAndView.addObject("username", user.getUsername());
        } catch (AuthenticationException | UserException e){
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
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(response.getOutputStream());
        encoder.encode(image);
    }

    //查看用户状态，显示是哪个用户在登录，如果没有登录的用户，就会提示你先登录
    @RequestMapping("/userStatus")
    public ModelAndView userStatus(HttpServletRequest request){
        ModelAndView modelAndView = new ModelAndView("userStatus");
        modelAndView.addObject("username",
                userService.getStatus((String)request.getSession().getAttribute("username")));
        return modelAndView;
    }

    //显示用户信息
    @RequestMapping("showInfo")
    public ModelAndView showInfo(HttpServletRequest request){
        return new ModelAndView("userInfo")
                .addObject("user", userService.showInfo(
                        ((String)request.getSession().getAttribute("username"))));
    }

    //对用户信息进行修改
    @RequestMapping("setUserInfo")
    public ModelAndView setUserInfo(User user){
        ModelAndView modelAndView = new ModelAndView("userInfo");
        try {
            userService.setUserInfo(user);
            //设置提示信息
            modelAndView.addObject("message", "修改成功");
            //跳转
            modelAndView.setViewName("main");
        } catch (UserException e){
            modelAndView.addObject("message", e.getMessage());
        }
        return modelAndView;
    }
}

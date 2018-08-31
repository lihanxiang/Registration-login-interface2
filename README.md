## Registration-login-interface2

### Version 0.2.2

#### 新增的功能都是在 v0.2.1 的基础上进行修改，删去部分代码

可通过点击 tag 来查看之前的代码：

![](https://upload-images.jianshu.io/upload_images/3426615-a2a86f9628363959.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### 1. 新增功能

* 使用 mvc 的拦截器来对特定页面进行拦截，拦截之后跳转至登录界面

* 使用过滤器来进行相同功能的拦截

#### 2. 删去的代码

先删除 ExceptionService 中的 statusException 方法，删去对应的实现，然后在 UserServiceImpl 和 Controller 中删去调用它的代码

这样，就变成了不检测用户是否登录

#### 3. 拦截器

首先新建一个包：interceptor

然后新建一个类：LoginInterceptor，实现了 HandlerInterceptor 接口：

关于 HandlerInterceptor 的使用这里不讲述，如果在 session 中没有获得 **username** 属性，就跳转到登录页面

**拦截是有范围的，在下文的配置文件中自行配置需要拦截的页面**

```
    public class LoginInterceptor implements org.springframework.web.servlet.HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse, Object o) throws Exception {
        HttpSession session = httpServletRequest.getSession();
        if (session.getAttribute("username") == null){
            httpServletResponse.sendRedirect("preLogin.action");
        } else {
            return true;
        }
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
```

在 spring-mvc.xml 中添加拦截器的配置：

```
    <mvc:interceptors>
        <mvc:interceptor>
            <mvc:mapping path="/userStatus.action"/>
            <mvc:mapping path="/showInfo.action"/>
            <mvc:mapping path="/setUserInfo.action"/>
            <bean class="interceptor.LoginInterceptor"/>
        </mvc:interceptor>
    </mvc:interceptors>
```

对指定路径进行映射，LoginInterceptor 中的操作就是对映射的路径而言的，然后指定一下拦截器所在位置，就完成了

##### 效果

运行之后，在浏览器地址栏输入

* **localhost:8080/userStatus.action**
* **localhost:8080/showInfo.action**
* **localhost:8080/setUserInfo.action**

就会跳转到 http://localhost:8080/preLogin.action

拦截器的部分就结束了

#### 4. 过滤器

将 spring-mvc.xml 中配置的拦截器信息注释掉，然后新建包：filter，包中新建类 LoginFilter，实现 **javax.servlet.Filter** 接口：

```
public class LoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, 
                         FilterChain filterChain) throws IOException, ServletException {
        if (((HttpServletRequest) servletRequest).getSession().
                getAttribute("username") == null){
            ((HttpServletResponse)servletResponse).sendRedirect("preLogin.action");
        } else {
            filterChain.doFilter(servletRequest,
                    servletResponse);
        }
    }
}
```

如果检测出没有登录，就会跳转到登录界面，**有一点要注意**：` filterChain.doFilter(servletRequest,
                    servletResponse);`
是一定要写的，如果不写，在登录之后，就不能访问那些需要登录才能访问的页面的，相当于请求已经被过滤器中断了，如果检测出已登录，就要加上这一行代码才能方位个人信息页面

然后在 web.xml 中添加一个过滤器的配置：

```
    <filter>
        <filter-name>loginFilter</filter-name>
        <filter-class>filter.LoginFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>loginFilter</filter-name>
        <url-pattern>/showInfo.action</url-pattern>
        <url-pattern>/userStatus.action</url-pattern>
        <url-pattern>/setUserInfo.action</url-pattern>
    </filter-mapping>
```

这样子，不同于 v0.2.1 的验证登录方式就做完了
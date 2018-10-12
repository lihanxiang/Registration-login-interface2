## Registration-login-interface2

### Version 0.3

#### 使用 Shiro 来进行用户登录以及拦截

可通过点击 tag 来查看之前的代码：

![](https://upload-images.jianshu.io/upload_images/3426615-855ae83e0358b2b8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

##### 1. 准备工作

在 lib 包中添加：

> slf4j-api-1.8.0-beta2（最新）
> shiro-all-1.2.1（一步到位）

##### 2. 需要删掉的部分

因为这个版本是用 Shiro 框架来做登录的部分，所以我们要把原先的登录操作相关代码删去：

* userService 中的 login() 方法

* userController 中的 login() 和 logout() 方法

* 登录的过滤相关操作

##### 3. Shiro 过滤器

Shiro 的基础知识就直接去看大佬的这一系列文章[跟我学 Shiro](http://jinnianshilongnian.iteye.com/blog/2018398)

我们接下来开车了：

###### 1. 在 web.xml 中添加过滤器：

```
	<filter>
        <filter-name>shiroFilter</filter-name>
        <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>shiroFilter</filter-name>
        <url-pattern>*.action</url-pattern>
    </filter-mapping>
```

DelegatingFilterProxy 只是作为代理，它提供了我们接下来写的 spring 包中的配置文件 spring-shiro.xml 和上述的 web.xml 中的 shiroFilter 之间的联系，在这之前还需要改动以下 web.xml 中的一个地方：

因为在启动时，上下文信息不止一个文件，所以这里用通配符 * 来指定 spring 包中的所有文件

```
	<context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/classes/config/spring/spring-*.xml</param-value>
    </context-param>
```

###### 2. CustomizeRealm

我们自己定义一个 Realm，继承自 AuthorizingRealm，因为我们还没有做授权的功能，这里继承 AuthenticatingRealm 也是可以的：

```
	@Autowired
    private UserService userService;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String username = (String)authenticationToken.getPrincipal();
        String password = new String((char[])authenticationToken.getCredentials());
        User user = userService.findUserByName(username);
        if (!username.equals(user.getUsername())){
            throw new AuthenticationException("该用户不存在或输入了错误的用户名");
        }
        if (!password.equals(user.getPassword())){
            throw new AuthenticationException("密码错误");
        }
        return new SimpleAuthenticationInfo(username, password, getName());
    }
```

在自定义的 Realm 中做了相关的输入检测，如果捕获，就输出到登录页面，如果用户输入的信息没有问题，就返回 `SimpleAuthenticationInfo` 用来进行登录

##### 3. spring-shiro.xml

这是 shiro 的配置文件，页面的拦截，登录页面的地址，以及 SecurityManager 等重要信息都在此配置：

```
	<bean id="shiroFilter" class="org.apache.shiro.spring.web.ShiroFilterFactoryBean">
        <property name="securityManager" ref="securityManager"/>
        <!-- 登录页面的地址 -->
        <property name="loginUrl" value="/preLogin.action"/>
        <!-- 如果在未登录的情况下访问受限页面，就跳转至此 -->
        <property name="unauthorizedUrl" value="/preLogin.action"/>
        <!-- 过滤链，从上往下执行 -->
        <property name="filterChainDefinitions">
            <value>
                <!-- 登录页面不拦截 -->
                /preLogin.action = anon
                <!-- 这 3 个和具体用户有关的页面需要拦截 -->
                /showInfo.action = user
                /setUserInfo.action = user
                /userStatus.action = user
                <!-- 登出操作 -->
                /logout.action = logout
            </value>
        </property>
    </bean>

    <bean id="securityManager" class="org.apache.shiro.web.mgt.DefaultWebSecurityManager">
        <property name="realm" ref="customizeRealm"/>
    </bean>

    <bean id="customizeRealm" class="shiro.CustomizeRealm"/>
```

从下往上写：

* 首先是创建我们刚才定义的 Realm 的 Bean

* 然后是 SecurityManager（安全管理器），我们要将 Realm 注入它，SecurityManager 是一个桥梁，与其它组件交互，管理 Subject，所有和安全有关的操作都会通过它（顾名思义），就相当于 Spring MVC 框架中的 DispatcherServlet

* 最后是 shiroFilter，与 web.xml 中的 shiroFilter 相对应（二者名称要相同，否则无法关联，就无法创建 Bean），这里要注入安全管理器，然后指定登录地址与未授权之跳转地址，接下来的需要说明一下我们用到的过滤器：

| 拦截器名称 |   对应功能  |
|-           |-            |
|anon        |不进行拦截，用在登录页面上，或者一些不需要授权的页面                          |
|authc       |如果没有登录，就跳转到 `loginUrl` 指定的页面                                  |
|user        |比 authc 多了一项功能，如果用户通过 RememberMe 方式登录，也可以访问指定页面   |
|logout      |Shiro 清空缓存并跳转到 `loginUrl` 指定的页面                                  |

不能将所有的 .action 路径都拦截，会导致验证码无法输出，使用 `user` 是为了之后做 RememberMe 功能时就不用修改这个文件了

##### 4. Controller

```
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
```

subject.login() 方法会委托给安全管理器，它把 token 传入自定义的 Realm，来获取身份验证信息，如果 Realm 返回的是 `SimpleAuthenticationInfo`，就代表登录成功，如果捕获异常，就在页面输出

页面输入用户名、密码 -> Controller -> Subject -> SecurityManager -> CustomizeRealm -> SimpleAuthenticationInfo -> 登录成功

##### 5. 关于验证码输出的 1 个小 bug

在 Controller 中将验证码的输出部分代码改为：

```
    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(response.getOutputStream());
    encoder.encode(image);
```

到此，Shiro 的登录应用就做完了，接下来会做 Shiro 的 Session 和授权
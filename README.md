## Registration-login-interface2

### Version 0.1

使用 SSM 框架来对原先的 [Registration-login-interface](https://github.com/lihanxiang/Registration-login-interface) 进行重构，页面做细微改动，后台使用框架，来达到同样的效果：

![](https://upload-images.jianshu.io/upload_images/3426615-c1638bca24a9db97.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![](https://upload-images.jianshu.io/upload_images/3426615-f583ceb88b70e7bb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

0.1  版本是使用框架进行重构，接下来的 0.2 版本将会是添加一些功能：用户保持登陆状态，添加一张 SQL 表来存放用户信息，并在页面中进行个人信息添加和修改

#### 1. 文件结构

关于建包和创建哪些类，不多说，直接上一整个项目的图：

![](https://upload-images.jianshu.io/upload_images/3426615-ec1033863d4614b8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


#### 2. 配置文件（config 包）

将 Spring 和 MyBatis 整合的方式，在之前的 [new-p-m](https://github.com/lihanxiang/new-p-m) 和 [mybatis-spring 官方文档](http://www.mybatis.org/spring/zh/index.html) 中都能找到答案，这里直接给出配置：

数据库信息（db.properties)：

jdbc.driver = com.mysql.jdbc.Driver
jdbc.url = jdbc:mysql://localhost:3306/user
jdbc.username = xxx（你自己的用户名）
jdbc.password = xxx（你自己的密码）

spring-mvc.xml:

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc.xsd">

    <!-- 组件扫描 -->
    <context:component-scan base-package="controller"/>
    <context:component-scan base-package="service"/>

    <!-- 注解驱动 -->
    <mvc:annotation-driven/>

    <!-- 配置视图解析器 -->
    <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="WEB-INF/jsp/"/>
        <property name="suffix" value=".jsp"/>
    </bean>

    <!-- 这两行代码是为了不让静态资源被 servlet 拦截 -->
    <mvc:resources mapping="/image/**" location="image"/>
    <mvc:resources mapping="../../css/**" location="css"/>
</beans>
```

spring-mybatis.xml:

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">

    <!-- 将数据库信息配置在外部文件中，使用占位符来代替具体信息的配置 -->
    <context:property-placeholder location="classpath:config/db.properties"/>

    <!-- 配置数据源 -->
    <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource">
        <property name="driverClassName" value="${jdbc.driver}"/>
        <property name="url" value="${jdbc.url}"/>
        <property name="username" value="${jdbc.username}"/>
        <property name="password" value="${jdbc.password}"/>
    </bean>

    <!-- sqlSessionFactory 的配置，这是基于 MyBatis 的应用的核心 -->
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <!-- 使用上面定义的数据源来进行配置 -->
        <property name="dataSource" ref="dataSource"/>
    </bean>

    <!-- 查找下面指定的类路径中的映射器 -->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <!-- 定义 Mapper 配置器的位置 -->
        <property name="basePackage" value="mapper"/>
    </bean>

    <!-- 之后要用到的两个 Bean -->
    <bean id="exceptionService" class="service.impl.ExceptionServiceImpl"/>
    <bean id="verifyCode" class="util.VerifyCode"/>
</beans>
```

#### 3. 实体类（User.java）

为了节省篇幅，这里不给出实体类代码，和 Registration-login-interface 中的实体类是一样的

#### 4. 自定义异常

自定义的异常是调用父类方法来实现的，因用户注册或登陆时输入有误而抛出

public class UserException extends Exception {
    //自定义异常
    public UserException(String message) {
        super(message);
    }
}

#### 5. 映射器

MyBatis-Spring 提供的 MapperFactoryBean 能够进行动态代理，能够将数据映射器**接口**注入到 Service 层中的 Bean 里，注意，一定要是接口，不能是实现类，所以我们这里写了一个 UserMapper：

```
public interface UserMapper {
    void addUser(User user);
    User findUserByName(String username);
    User findUserByPhone(String phone);
    User findUserByEmail(String email);
}
```

映射器接口对应着一个同名的 XML 映射器文件文件：UserMapper.xml

这个映射器中写的是 SQL 语句，这里面有四句，添加，按照名称、电话号码和邮箱进行查找，映射文件的命名空间（namespace）对应着映射器接口的名称，SQL 语句的 id 对应着接口中的方法，不能有误

```
<mapper namespace="mapper.UserMapper">
    <insert id="addUser" parameterType="domain.User">
      INSERT INTO user(username,password,phone,email)
      VALUES (#{username}, #{password}, #{phone}, #{email})
    </insert>

    <select id="findUserByName" parameterType="String" resultType="domain.User">
        SELECT * FROM user WHERE username = #{username}
    </select>

    <select id="findUserByPhone" parameterType="String" resultType="domain.User">
        SELECT * FROM user WHERE phone = #{phone}
    </select>

    <select id="findUserByEmail" parameterType="String" resultType="domain.User">
        SELECT * FROM user WHERE email = #{email}
    </select>
</mapper>
```

#### 5. 验证码

SSM 版本的验证码没有变化，还是 [Registration-login-interface](https://github.com/lihanxiang/Registration-login-interface) 中的验证码，不做更改

#### 6. Service 层

Service 层有两个接口，一个是关于注册和登陆的：

```
public interface UserService {
    public void addUser(User user) throws UserException;
    public void login(User user) throws UserException;
}
```

另一个是检测注册和登陆过程中的错误情况：

@Service
public interface ExceptionService {

    //_user 是从数据库中查找出的记录，user 是用户输入
    public void loginException(User user, User db_user) throws UserException;

    public void addUserException1(User user) throws UserException;

    public void addUserException2(User user) throws UserException;
}
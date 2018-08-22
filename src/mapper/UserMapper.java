package mapper;

import domain.User;

public interface UserMapper {
    void addUser(User user);
    User findUserByName(String username);
    User findUserByPhone(String phone);
    User findUserByEmail(String email);
}

package com.jtravan.dal;

import com.jtravan.dal.model.User;

import java.util.List;

public interface UserService {
    void addUser(User user);
    User getUserById(Integer id);
    List<User> getAllUsers();
    void updateUser(User user);
}

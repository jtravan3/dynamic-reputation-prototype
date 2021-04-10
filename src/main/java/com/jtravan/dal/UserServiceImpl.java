package com.jtravan.dal;

import com.jtravan.dal.model.User;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(@NonNull UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void addUser(User user) {
        userRepository.save(user);
    }

    @Override
    public User getUserById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public List<User> getAllUsers() {
        List<User> rtnList = new LinkedList<>();
        userRepository.findAll().forEach(rtnList::add);
        return rtnList;
    }

    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }
}

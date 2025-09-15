package org.pm.authservice.service;


import org.pm.authservice.model.User;
import org.pm.authservice.respository.UserRespository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRespository userRespository;

    public UserService(UserRespository userRespository) {
        this.userRespository = userRespository;
    }

    public Optional<User> findByEmail(String email){
        return userRespository.findByEmail(email);
    }
}

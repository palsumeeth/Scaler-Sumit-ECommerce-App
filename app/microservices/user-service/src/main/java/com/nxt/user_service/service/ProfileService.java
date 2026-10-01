package com.nxt.user_service.service;

import com.nxt.user_service.dto.UpdateProfileDTO;
import com.nxt.user_service.entity.User;
import com.nxt.user_service.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    @Autowired
    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public User update(Long id, UpdateProfileDTO dto) {
        User u = getById(id);

        /*
        * TODO: Decide how to log updated fields, with keys or keys and values
        *  too.
        * */

        if (dto.getFirstName() != null) u.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) u.setLastName(dto.getLastName());
        if (dto.getEmail() != null) u.setEmail(dto.getEmail());
        return userRepository.save(u);
    }
}
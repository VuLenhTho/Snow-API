package com.vulenhtho.snow.service;

import com.vulenhtho.snow.dto.UserDTO;
import com.vulenhtho.snow.entity.User;

public interface MailService {
    void sendActivationEmail(UserDTO userDTO, String activationKey);

    boolean sendPasswordResetMail(User user);
}

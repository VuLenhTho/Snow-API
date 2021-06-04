package com.vulenhtho.snow.service;

import com.vulenhtho.snow.dto.UserDTO;
import com.vulenhtho.snow.dto.request.UserFilterRequestDTO;
import com.vulenhtho.snow.dto.response.UserInfoWebResponseDTO;
import com.vulenhtho.snow.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {
    User registerUser(UserDTO userDTO);

    User oauth2LoginOrRegister(UserDTO userDTO);

    User createdUser(UserDTO userDTO);

    boolean activateRegistration(String key);

    boolean changePassword(String currentPassword, String newPassword);

    User requestPasswordReset(String mail);

    User completePasswordReset(String newPassword, String key);

    UserDTO getUserById(Long id);

    Page<UserDTO> getAllUserWithFilter(UserFilterRequestDTO filterRequest);

    boolean update(UserDTO userDTO);

    boolean delete(Long id);

    boolean delete(List<Long> ids);

    UserInfoWebResponseDTO getUserLoginInfo();

    String checkDuplicatesUserInfoInCreate(String userName, String email, String phone);

    String checkDuplicatesUserInfoInUpdate(String userName, String email, String phone);
}

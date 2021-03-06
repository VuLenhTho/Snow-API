package com.vulenhtho.snow.mapper;

import com.vulenhtho.snow.dto.UserDTO;
import com.vulenhtho.snow.entity.User;
import com.vulenhtho.snow.repository.RoleRepository;
import com.vulenhtho.snow.util.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public UserMapper(RoleRepository roleRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }


    public UserDTO toDTO(User user) {
        if (user == null) return null;
        UserDTO userDTO = new UserDTO();
        BeanUtils.refine(user, userDTO, BeanUtils::copyNonNull);
        if (user.getRoles() != null && !user.getRoles().isEmpty()){
            userDTO.setRoles(roleMapper.toDTO(user.getRoles()));
        }else userDTO.setRoles(null);

        userDTO.setPassword(null);
        return userDTO;
    }

    public List<UserDTO> toDTO(List<User> users){
        if (users == null) return null;
        return users.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public User toEntity(UserDTO userDTO){
        if (userDTO == null) return null;
        User user = new User();
        BeanUtils.refine(userDTO, user, BeanUtils::copyNonNull);

        if (!CollectionUtils.isEmpty(userDTO.getRoles())){
            user.setRoles(roleMapper.toEntity(userDTO.getRoles()));
        }else user.setRoles(null);

        return user;
    }

    public List<User> toEntity(List<UserDTO> userDTOs){
        if (userDTOs == null) return null;
        return userDTOs.stream().map(this::toEntity).collect(Collectors.toList());
    }

}

package com.vulenhtho.snow.mapper;

import com.vulenhtho.snow.dto.RoleDTO;
import com.vulenhtho.snow.entity.Role;
import com.vulenhtho.snow.util.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleMapper {

    public RoleDTO toDTO(Role role){
        RoleDTO roleDTO = new RoleDTO();
        BeanUtils.refine(role, roleDTO, BeanUtils::copyNonNull);
        return roleDTO;
    }

    public Set<RoleDTO> toDTO(Set<Role> roles){
        return roles.stream().map(this::toDTO).collect(Collectors.toSet());
    }

    public Role toEntity(RoleDTO roleDTO){
        Role role = new Role();
        BeanUtils.refine(roleDTO, role, BeanUtils::copyNonNull);
        return role;
    }

    public Set<Role> toEntity(Set<RoleDTO> roleDTOS){
        return roleDTOS.stream().map(this::toEntity).collect(Collectors.toSet());
    }
}

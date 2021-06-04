package com.vulenhtho.snow.service.impl;

import com.vulenhtho.snow.dto.RoleDTO;
import com.vulenhtho.snow.mapper.RoleMapper;
import com.vulenhtho.snow.repository.RoleRepository;
import com.vulenhtho.snow.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    private final RoleMapper roleMapper;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    public Set<RoleDTO> getAll() {
        return roleMapper.toDTO(new HashSet<>(roleRepository.findAll()));
    }
}

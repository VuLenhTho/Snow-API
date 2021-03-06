package com.vulenhtho.snow.controller.web;

import com.vulenhtho.snow.dto.RoleDTO;
import com.vulenhtho.snow.dto.UserDTO;
import com.vulenhtho.snow.entity.User;
import com.vulenhtho.snow.mapper.UserMapper;
import com.vulenhtho.snow.repository.UserRepository;
import com.vulenhtho.snow.security.jwt.JwtProvider;
import com.vulenhtho.snow.security.jwt.JwtResponse;
import com.vulenhtho.snow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder encoder;

    private final JwtProvider jwtProvider;

    private final UserRepository userRepository;

    private final UserService userService;

    private final UserMapper userMapper;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, PasswordEncoder encoder, JwtProvider jwtProvider, UserRepository userRepository, UserService userService, UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.encoder = encoder;
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/create")
    public ResponseEntity<Void> create(@RequestParam int max, @RequestParam int min){
        for (int i = min; i < max; i++) {
            UserDTO userDTO = new UserDTO();
            userDTO.setUserName("thoFull0" +i);
            userDTO.setPassword("1");
            userDTO.setFullName("tho0" +i);
            userDTO.setSex(true);
            userDTO.setEmail("tho" + i +"@gmail.com");
            userDTO.setPhone("012345678" +i);
            userDTO.setActivated(true);
            userDTO.setLocked(false);
            RoleDTO roleDTO = new RoleDTO();
            roleDTO.setName("ROLE_ADMIN");
            RoleDTO roleDTO1 = new RoleDTO();
            roleDTO1.setName("ROLE_USER");
            userDTO.getRoles().add(roleDTO);
            userDTO.getRoles().add(roleDTO1);
            userService.createdUser(userDTO);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO userDTO) {

        Optional<User> user = userRepository.findByUserName(userDTO.getUserName());

        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not found user with username : " + userDTO.getUserName());
        } else if (encoder.matches(userDTO.getPassword(), user.get().getPassword())) {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userDTO.getUserName(),
                            userDTO.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtProvider.generateJwtToken(authentication);
            return ResponseEntity.ok(new JwtResponse(jwt, userMapper.toDTO(user.get())));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect password");
    }

    @PostMapping("/sign-up")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserDTO userDTO) {
        if(userRepository.findByUserName(userDTO.getUserName()).isPresent()) {
            return new ResponseEntity<>("Fail -> Username is already in use!",
                    HttpStatus.BAD_REQUEST);
        }
        if(userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            return new ResponseEntity<>("Fail -> Email is already in use!",
                    HttpStatus.BAD_REQUEST);
        }
        userService.registerUser(userDTO);
        return ResponseEntity.ok().body("User registered successfully!");
    }


}

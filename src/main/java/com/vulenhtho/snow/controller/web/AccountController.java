package com.vulenhtho.snow.controller.web;

import com.vulenhtho.snow.config.Constant;
import com.vulenhtho.snow.dto.PasswordChangeDTO;
import com.vulenhtho.snow.dto.UserDTO;
import com.vulenhtho.snow.entity.User;
import com.vulenhtho.snow.mapper.UserMapper;
import com.vulenhtho.snow.repository.UserRepository;
import com.vulenhtho.snow.security.jwt.JwtProvider;
import com.vulenhtho.snow.security.jwt.JwtResponse;
import com.vulenhtho.snow.service.UserService;
import com.vulenhtho.snow.service.impl.MailServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final UserRepository userRepository;

    private final UserService userService;

    private final MailServiceImpl mailService;

    private final AuthenticationManager authenticationManager;

    private final JwtProvider jwtProvider;

    private final UserMapper userMapper;


    public AccountController(UserRepository userRepository, UserService userService, MailServiceImpl mailService, AuthenticationManager authenticationManager, JwtProvider jwtProvider, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.mailService = mailService;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerAccount(@RequestBody UserDTO userDTO) {
        try {
            User user = userService.registerUser(userDTO);
//            mailService.sendActivationEmail(userDTO, user.getActivationKey());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/oauth2Login")
    public ResponseEntity<?> registerOrLoginWithOauth2(@RequestBody UserDTO userDTO) {
        try {
            User user = userService.oauth2LoginOrRegister(userDTO);
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUserName(),
                            Constant.OAUTH2_DEFAULT_PASSWORD
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtProvider.generateJwtToken(authentication);
            return ResponseEntity.ok(new JwtResponse(jwt, userMapper.toDTO(user)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/activate")
    public ResponseEntity<Void> activateAccount(@RequestParam String key) {
        if (userService.activateRegistration(key)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(path = "/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
        boolean result = userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
        if (result){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(path = "/reset-password/init")
    public ResponseEntity<Void> requestPasswordReset(@RequestBody UserDTO email) {
        boolean result = mailService.sendPasswordResetMail(userService.requestPasswordReset(email.getEmail()));
        if (result){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(path = "/reset-password/finish")
    public ResponseEntity<Void> finishPasswordReset(@RequestBody UserDTO keyAndPassword) {
        User user = userService.completePasswordReset(keyAndPassword.getPassword(), keyAndPassword.getResetKey());
        if (user != null){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

}

package vn.iotstar.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.iotstar.entity.User;
import vn.iotstar.models.LoginResponse;
import vn.iotstar.models.LoginUserModel;
import vn.iotstar.models.RegisterUserModel;
import vn.iotstar.services.AuthenticationService;
import vn.iotstar.services.JwtService;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(
            JwtService jwtService,
            AuthenticationService authenticationService) {

        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

   

    @PostMapping("/signup")
    public ResponseEntity<User> register(
            @RequestBody RegisterUserModel registerUserModel) {

        User registeredUser =
                authenticationService.signup(registerUserModel);

        return ResponseEntity.ok(registeredUser);
    }

    

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(
            @RequestBody LoginUserModel loginUserModel) {

        Authentication authentication =
                authenticationService.authenticate(loginUserModel);

        User authenticatedUser =
                (User) authentication.getPrincipal();

        String jwtToken =
                jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse =
                new LoginResponse(
                        jwtToken,
                        jwtService.getExpirationTime()
                );

        return ResponseEntity.ok(loginResponse);
    }
}
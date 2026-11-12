package com.fiec.br.back_end.kipper.config;

import com.fiec.br.back_end.kipper.features.user.model.entities.UserRole;
import com.fiec.br.back_end.kipper.features.user.model.entities.Users;
import com.fiec.br.back_end.kipper.features.user.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    public OAuth2SuccessHandler(JwtUtil jwtService,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");

        if (email == null) {
            throw new IllegalStateException(
                    "Google não retornou e-mail. Verifique o escopo 'email' no application.yml.");
        }

        Users user = userRepository.findByEmail(email)
                .orElseGet(() -> createUserFromGoogle(email, name));

        Map<String, Object> claims = new HashMap<>();
        claims.put("name", user.getName());
        claims.put("pictureUrl", picture);

        String token = jwtService.generateToken(claims, user);

        // A partir daqui quem autentica é o JWT. Se a sessão do login OAuth continuasse viva,
        // o navegador mandaria o cookie JSESSIONID e o usuário logado seria um OAuth2User
        // (e não um Users): os controllers quebrariam com ClassCastException (erro 500).
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private Users createUserFromGoogle(String email, String name) {
        Users user = Users.builder()
                .name(name != null ? name : "Usuário Google")
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(UserRole.USER_ROLE)
                .build();
        return userRepository.save(user);
    }
}

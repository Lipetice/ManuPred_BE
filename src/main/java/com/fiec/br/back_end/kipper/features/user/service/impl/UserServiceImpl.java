package com.fiec.br.back_end.kipper.features.user.service.impl;

import com.fiec.br.back_end.kipper.exception.RecursoNaoEncontradoException;
import com.fiec.br.back_end.kipper.config.JwtUtil;
import com.fiec.br.back_end.kipper.features.auth.models.dto.TokenResponseDTO;
import com.fiec.br.back_end.kipper.features.user.model.dto.CreateUserRequestDTO;
import com.fiec.br.back_end.kipper.features.user.model.dto.UserResponseDTO;
import com.fiec.br.back_end.kipper.features.user.model.entities.UserRole;
import com.fiec.br.back_end.kipper.features.user.model.entities.Users;
import com.fiec.br.back_end.kipper.features.user.repositories.UserRepository;
import com.fiec.br.back_end.kipper.features.user.service.UserService;
import com.fiec.br.back_end.kipper.model.service.AuditService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com E-mail: " + username));
    }

    @Override
    public UserResponseDTO createUser(CreateUserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado no sistema.");
        }
        Users user = Users.builder()
                .name(dto.name())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .firebaseUid(dto.firebaseUid())
                .build();
        return UserResponseDTO.fromEntity(userRepository.save(user));
    }

    @Override
    public UserResponseDTO findById(UUID id) {
        return UserResponseDTO.fromEntity(userRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com ID: " + id)));
    }

    @Override
    public UserResponseDTO findByEmail(String email) {
        return UserResponseDTO.fromEntity(userRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com E-mail: " + email)));
    }

    @Override
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream().map(UserResponseDTO::fromEntity).toList();
    }

    @Override
    public void deleteUser(UUID id) {
        Users alvo = userRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado para deleção."));

        Users autor = getUsuarioLogado();
        if (autor != null && autor.getId().equals(id)) {
            throw new IllegalArgumentException("Você não pode deletar a própria conta.");
        }

        userRepository.deleteById(id);
        auditService.registrar("DELETAR_USUARIO",
                "Deletou usuário: " + alvo.getName() + " (" + alvo.getEmail() + ")", autor);
    }

    @Override
    public TokenResponseDTO verifyAndAuthenticateFirebaseToken(String firebaseToken) {
        FirebaseToken decodedToken;
        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
        } catch (Exception e) {
            log.warn("Falha na verificação do token Firebase: {}", e.getMessage());
            throw new BadCredentialsException("Token do Firebase inválido ou expirado.");
        }

        String uid = decodedToken.getUid();
        String email = decodedToken.getEmail();
        String name = decodedToken.getName();

        // O e-mail é o "username" do sistema (vai no subject do JWT). Login por telefone ou
        // anônimo no Firebase não traz e-mail e criaria um usuário que não consegue autenticar.
        if (email == null || email.isBlank()) {
            throw new BadCredentialsException("A conta do Firebase não possui e-mail.");
        }

        Users user = userRepository.findByFirebaseUid(uid)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(existingUser -> {
                            existingUser.setFirebaseUid(uid);
                            return userRepository.save(existingUser);
                        })
                        .orElseGet(() -> userRepository.save(Users.builder()
                                .name(name != null ? name : "Usuário Firebase")
                                .email(email)
                                // Senha aleatória (igual ao login Google): nunca deixar senha vazia
                                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                .firebaseUid(uid)
                                .build())));

        return new TokenResponseDTO(jwtUtil.generateToken(user));
    }

    @Override
    public UserResponseDTO atualizarRole(UUID id, UserRole role) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com ID: " + id));
        UserRole antiga = user.getRole();
        user.setRole(role);
        Users salvo = userRepository.save(user);

        auditService.registrar("ALTERAR_ROLE",
                "Alterou role de " + salvo.getName() + ": " + antiga + " → " + role,
                getUsuarioLogado());

        return UserResponseDTO.fromEntity(salvo);
    }

    @Override
    public void atualizarFcmToken(String email, String fcmToken) {
        // Um aparelho só pode estar ligado a um usuário: tira o token de quem usava antes
        List<Users> usuariosComToken = userRepository.findAllByFcmToken(fcmToken);
        for (Users u : usuariosComToken) {
            if (!u.getEmail().equals(email)) {
                u.setFcmToken(null);
                userRepository.save(u);
            }
        }

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }

    private Users getUsuarioLogado() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof Users u ? u : null;
    }
}

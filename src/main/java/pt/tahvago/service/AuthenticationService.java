package pt.tahvago.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import pt.tahvago.dto.LoginUserDto;
import pt.tahvago.dto.RegisterUserDto;
import pt.tahvago.dto.VerifyUserDto;
import pt.tahvago.exceptions.RegistrationException;
import pt.tahvago.model.AppUser;
import pt.tahvago.repository.UserRepository;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    private final Map<String, LockoutInfo> lockoutCache = new ConcurrentHashMap<>();
    private final int MAX_ATTEMPTS = 2;
    private final int LOCKOUT_DURATION_MINUTES = 1;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public AppUser signup(RegisterUserDto input) {
        if (userRepository.findByUsername(input.getUsername()).isPresent()) {
            throw new RegistrationException("Username already exists");
        }
        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            throw new RegistrationException("Email already exists");
        }

        AppUser user = new AppUser(input.getUsername(), input.getEmail(), passwordEncoder.encode(input.getPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);
        sendVerificationEmail(user);
        return userRepository.save(user);
    }

    public AppUser authenticate(LoginUserDto input, String clientIp) {
        if (isBlocked(clientIp)) {
            throw new RuntimeException("Too many attempts. Please wait 1 minute.");
        }

        try {
            AppUser user = userRepository.findByEmail(input.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.isEnabled()) {
                throw new RuntimeException("Account not verified. Please verify your account.");
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.getEmail(),
                            input.getPassword()
                    )
            );

            lockoutCache.remove(clientIp);
            return user;

        } catch (Exception e) {
            registerFailedAttempt(clientIp);
            throw e;
        }
    }

    private boolean isBlocked(String ip) {
        LockoutInfo info = lockoutCache.get(ip);
        if (info == null) return false;

        if (info.attempts >= MAX_ATTEMPTS) {
            if (LocalDateTime.now().isBefore(info.lockoutEndTime)) {
                return true;
            }
            lockoutCache.remove(ip);
        }
        return false;
    }

    private void registerFailedAttempt(String ip) {
        LockoutInfo info = lockoutCache.getOrDefault(ip, new LockoutInfo());
        info.attempts++;
        if (info.attempts >= MAX_ATTEMPTS) {
            info.lockoutEndTime = LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES);
        }
        lockoutCache.put(ip, info);
    }

    private static class LockoutInfo {
        int attempts = 0;
        LocalDateTime lockoutEndTime = LocalDateTime.now();
    }

    public void verifyUser(VerifyUserDto input) {
        Optional<AppUser> optionalUser = userRepository.findByEmail(input.getEmail());
        if (optionalUser.isPresent()) {
            AppUser user = optionalUser.get();
            if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification code has expired");
            }
            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                userRepository.save(user);
            } else {
                throw new RuntimeException("Invalid verification code");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public void resendVerificationCode(String email) {
        Optional<AppUser> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            AppUser user = optionalUser.get();
            if (user.isEnabled()) {
                throw new RuntimeException("Account is already verified");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
            sendVerificationEmail(user);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    private void sendVerificationEmail(AppUser user) {
        String subject = "Account Verification";
        String verificationCode = "VERIFICATION CODE " + user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
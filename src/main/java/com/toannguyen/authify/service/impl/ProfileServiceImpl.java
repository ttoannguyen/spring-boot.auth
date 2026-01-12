package com.toannguyen.authify.service.impl;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.toannguyen.authify.dto.ProfileRequest;
import com.toannguyen.authify.dto.ProfileResponse;
import com.toannguyen.authify.entity.UserEntity;
import com.toannguyen.authify.repository.UserRepository;
import com.toannguyen.authify.service.EmailService;
import com.toannguyen.authify.service.ProfileService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    EmailService emailService;

    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        UserEntity newProfile = convertToUserEntity(request);
        if (!userRepository.existsByEmail(request.getEmail())) {
            newProfile = userRepository.save(newProfile);
            return convertToProfileResponse(newProfile);
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found with email: " + email));
        return convertToProfileResponse(existingUser);

    }

    @Override
    public void sendRestOtp(String email) {
        UserEntity existingEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found with email: " + email));
        // Generate 6 digit OTP

        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        // calculate expiry time (current time + 15 minutes in milliseconds)
        long expiryTime = System.currentTimeMillis() + 15 * 60 * 1000;

        // update the profile/user
        existingEntity.setResetOtp(otp);
        existingEntity.setResetOtpExpiryAt(expiryTime);

        // save into the database
        userRepository.save(existingEntity);
        try {
            emailService.sendResetOtpEmail(email, otp);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to send email");
        }

        // return;
    }

    @Override
    public void resetPassword(String email, String otp, String resetPassword) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found with email: " + email));
        if (existingUser.getResetOtp() == null || !existingUser.getResetOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
        if (existingUser.getResetOtpExpiryAt() < System.currentTimeMillis()) {
            throw new RuntimeException("OTP Expired");
        }

        existingUser.setPassword(passwordEncoder.encode(resetPassword));
        existingUser.setResetOtp(null);
        existingUser.setResetOtpExpiryAt(0L);

        userRepository.save(existingUser);
    }

    private ProfileResponse convertToProfileResponse(UserEntity newProfile) {
        return ProfileResponse.builder()
                .userId(newProfile.getUserId())
                .name(newProfile.getName())
                .email(newProfile.getEmail())
                .isAccountVerified(newProfile.getIsAccountVerified())
                .build();
    }

    private UserEntity convertToUserEntity(ProfileRequest request) {
        return UserEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userId(UUID.randomUUID().toString())
                .isAccountVerified(false)
                .resetOtpExpiryAt(0L)
                .verifyOtp(null)
                .verifyOtpExpiryAt(0L)
                .build();
    }

}
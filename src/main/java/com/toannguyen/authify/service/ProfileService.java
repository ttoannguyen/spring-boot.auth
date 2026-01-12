package com.toannguyen.authify.service;

import com.toannguyen.authify.dto.ProfileRequest;
import com.toannguyen.authify.dto.ProfileResponse;

public interface ProfileService {
    ProfileResponse createProfile(ProfileRequest request);

    ProfileResponse getProfile(String email);

    void sendRestOtp(String email);

    void resetPassword(String email, String otp, String resetPassword);
}

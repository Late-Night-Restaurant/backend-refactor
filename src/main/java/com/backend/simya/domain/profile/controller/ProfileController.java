package com.backend.simya.domain.profile.controller;

import com.backend.simya.domain.profile.dto.request.ProfileRequestDto;
import com.backend.simya.domain.profile.dto.request.ProfileUpdateDto;
import com.backend.simya.domain.profile.dto.response.ProfileResponseDto;
import com.backend.simya.domain.profile.entity.Profile;
import com.backend.simya.domain.profile.service.ProfileService;
import com.backend.simya.domain.user.entity.User;
import com.backend.simya.domain.user.service.UserService;
import com.backend.simya.global.common.BaseException;
import com.backend.simya.global.common.BaseResponse;
import com.backend.simya.global.common.BaseResponseStatus;
import com.backend.simya.global.common.ValidErrorDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;

import static com.backend.simya.global.common.BaseResponseStatus.*;

@Slf4j
@RestController
@RequestMapping("/simya/users/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;

    @PostMapping("")
    public BaseResponse createProfile(@Valid @RequestBody ProfileRequestDto profileRequestDto, Errors errors) {

        if (errors.hasErrors()) {
            ValidErrorDetails errorDetails = new ValidErrorDetails();
            return new BaseResponse<>(REQUEST_ERROR, errorDetails.validateHandling(errors));
        }

        try {
//        User user = authService.authenticateUser();  // 현재 접속한 유저
            User user = userService.getMyUserWithAuthorities();
            log.info("ProfileController - user: {}", user);

            ProfileResponseDto profileResponseDto = profileService.createProfile(profileRequestDto, user);
            return new BaseResponse<>(profileResponseDto);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    @GetMapping("")
    public BaseResponse<List<ProfileResponseDto>> getMyProfiles() {
        try {
            User currentUser = userService.getMyUserWithAuthorities();
            return new BaseResponse<>(profileService.findMyAllProfile(currentUser));
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }


    @PatchMapping("/{profileId}")
    public BaseResponse<String> updateProfile(@PathVariable("profileId") Long profileId, @RequestBody ProfileUpdateDto profileUpdateDto) {

        try {
            profileUpdateDto.setProfileId(profileId);
            profileService.updateProfile(profileUpdateDto);

            return new BaseResponse<>(SUCCESS_TO_UPDATE_PROFILE);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    @DeleteMapping("/{profileId}")
    public BaseResponse<String> deleteProfile(@PathVariable("profileId") Long profileId) {

        try {
            profileService.deleteProfile(profileId);
            return new BaseResponse<>(SUCCESS_TO_DELETE_PROFILE);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    @PatchMapping("/{profileId}/main")
    public BaseResponse<String> setMainProfile(@PathVariable("profileId") Long profileId) {
        try {
            profileService.setMainProfile(profileId);
            return new BaseResponse<>(SUCCESS_TO_CHANGE_MAIN_PROFILE);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    @GetMapping("/{profileId}")
    public BaseResponse<ProfileResponseDto> getProfileInfo(@PathVariable("profileId") Long profileId) {
        try {
            Profile profile = profileService.findProfile(profileId);
            return new BaseResponse<>(ProfileResponseDto.from(profile));
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

}

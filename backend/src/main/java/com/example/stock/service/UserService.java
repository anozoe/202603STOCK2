package com.example.stock.service;

import com.example.stock.constants.RoleCode;
import com.example.stock.dto.UserInfoResponse;
import com.example.stock.dto.UserLoginRequest;
import com.example.stock.dto.UserRegisterRequest;
import com.example.stock.dto.UserUpdateRequest;
import com.example.stock.entity.AssetsTotal;
import com.example.stock.entity.User;
import com.example.stock.exception.BusinessException;
import com.example.stock.repository.AssetsTotalRepository;
import com.example.stock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AssetsTotalRepository assetsTotalRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo() {
        User user = currentUserService.getCurrentUser();
        return toUserInfoResponse(user);
    }

    @Transactional
    public UserInfoResponse updateMyInfo(UserUpdateRequest request) {
        User user = currentUserService.getCurrentUser();

        String normalizedUserName = request.getUserName().replaceAll("[\\s　]+", "");
        String trimmedEmail = request.getEmail().trim();

        userRepository.findByEmailAndIdNot(trimmedEmail, user.getId())
                .ifPresent(u -> {
                    throw new BusinessException("E005", "メールアドレス");
                });

        user.setName(normalizedUserName);
        user.setEmail(trimmedEmail);
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy("system");

        try {
            userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("E006");
        }

        return toUserInfoResponse(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterRequest request) {
        String name = request.getName() == null ? "" : request.getName().trim();
        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        String password = request.getPassword();

        userRepository.findByEmail(email)
                .ifPresent(u -> {
                    throw new BusinessException("E005", "メールアドレス");
                });

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(RoleCode.GENERAL_USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy("system");
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy("system");

        
        try {
            User registeredUser = userRepository.save(user);
            BigDecimal defaultBuyingPower = BigDecimal.valueOf(100000);
            AssetsTotal assetsTotal = new AssetsTotal();
            assetsTotal.setUserId(registeredUser.getId().intValue());
            assetsTotal.setBuyingPower(defaultBuyingPower);
            assetsTotal.setHoldingsValue(BigDecimal.ZERO);
            assetsTotal.setTotalAssets(defaultBuyingPower);
            assetsTotal.setUnrealizedPnl(BigDecimal.ZERO);
            assetsTotal.setUnrealizedPnlRatio(BigDecimal.ZERO);
            assetsTotal.setCreatedAt(LocalDateTime.now());
            assetsTotalRepository.save(assetsTotal);
        } catch (Exception e) {
            e.printStackTrace();   
            throw new BusinessException("E006");
        }
    }

    @Transactional(readOnly = true)
    public User login(UserLoginRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        String rawPassword = request.getPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("E010", "ユーザ"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BusinessException("E002", "パスワード");
        }

        return user;
    }

    @Transactional(readOnly = true)
    public UserInfoResponse loginUserInfo(UserLoginRequest request) {
        User user = login(request);
        return toUserInfoResponse(user);
    }

    private UserInfoResponse toUserInfoResponse(User user) {
        String roleName = user.getRole() != null && user.getRole() == RoleCode.ADMIN
                ? "管理者"
                : "一般ユーザ";

        return new UserInfoResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                roleName,
                user.getUpdatedAt() == null ? null : user.getUpdatedAt().toString()
        );
    }
}
package com.pranit.github.security.service;

import com.pranit.github.entities.model.UserDetail;

public interface GenerateToken {

    String generateAccessToken(UserDetail userDetail);

    String generateRefreshToken(UserDetail userDetail, String jti);
}

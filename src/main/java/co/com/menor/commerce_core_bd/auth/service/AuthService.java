package co.com.menor.commerce_core_bd.auth.service;

import co.com.menor.comun_dto.auth.request.AuthRequest;
import co.com.menor.comun_dto.auth.response.AuthResponse;

public interface AuthService {

    AuthResponse login(AuthRequest request);
}

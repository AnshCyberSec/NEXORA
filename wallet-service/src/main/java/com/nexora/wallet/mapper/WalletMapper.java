package com.nexora.wallet.mapper;

import com.nexora.wallet.dto.request.CreateWalletRequest;
import com.nexora.wallet.dto.response.WalletResponse;
import com.nexora.wallet.entity.Wallet;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {
    public Wallet toEntity(CreateWalletRequest request){
        return Wallet.builder()
                .userId(request.getUserId())
                .build();
    }

    public WalletResponse toResponse(Wallet wallet){
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .status(wallet.getStatus())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
}

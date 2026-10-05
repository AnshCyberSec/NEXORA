package com.nexora.wallet.exception;

public class WalletAlreadyExistsException extends BusinessException{
    public WalletAlreadyExistsException(String userId){
        super(
                String.format("Wallet already exists for user: %s",userId),
                "WALLET_ALREADY_EXISTS",
                409
        );
    }
}

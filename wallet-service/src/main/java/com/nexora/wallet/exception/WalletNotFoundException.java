package com.nexora.wallet.exception;

public class WalletNotFoundException extends BusinessException{

    public WalletNotFoundException(String userId){
        super(
                String.format("Wallet not found for user: %s",userId),
                "WALLET_NOT_FOUND",
                404
        );
    }
    public WalletNotFoundException(String field,String value){
        super(
                String.format("Wallet with %s '%s' not found",field,value),
                "WALLET_NOT_FOUND",
                404
        );
    }
}

package com.giova.service.moneystats.app.wallet;

import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Logged
@RestController
@RequestMapping("/v1/wallet")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping(value = "/insert-update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Wallet", description = "API to insert a wallet")
    @Operation(description = "API to insert a wallet", tags = "Wallet")
    @LogInterceptor(type = LogTimeTracker.ActionType.APP_CONTROLLER)
    public ResponseEntity<Response> insertOrUpdateWallet(@RequestBody @Valid Wallet wallet, @RequestHeader("authToken") String authToken) throws UtilsException {
        return walletService.insertOrUpdateWallet(wallet, authToken);
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Wallet", description = "API to get all wallet")
    @Operation(description = "API to get all wallet", tags = "Wallet")
    @LogInterceptor(type = LogTimeTracker.ActionType.APP_CONTROLLER)
    public ResponseEntity<Response> listWallet(@RequestHeader("authToken") String authToken) throws UtilsException {
        return walletService.getWallets(authToken);
    }
}

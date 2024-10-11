package com.giova.service.moneystats.app.wallet.database;

import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public class WalletDAOAdapter implements WalletRepository {
  @Autowired private IWalletDAO iWalletDAO;

  /**
   * Obtain Wallet without Stats and Assets, You Just Got the last Stats as Default
   *
   * @param userId User of the Wallet
   * @return Wallet with only the last Stats
   */
  @Override
  public List<WalletEntity> findAllByUserIdWithoutAssetsAndHistory(Long userId) {
    return iWalletDAO.findAllByUserIdWithoutAssetsAndHistory(userId);
  }
}

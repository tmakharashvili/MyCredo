package models.cardModule;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AccountDetails {
    private String accountNumber;
    private String currency;
    private double availableBalance;
    private double buyRate;

    public AccountDetails() {
        // Default კონსტრუქტორი
    }

    // შევსებული კონსტრუქტორი
    public AccountDetails(String accountNumber, String currency, double availableBalance, double buyRate) {
        this.accountNumber = accountNumber;
        this.currency = currency;
        this.availableBalance = availableBalance;
        this.buyRate = buyRate;
    }
}

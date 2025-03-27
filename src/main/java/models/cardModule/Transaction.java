package models.cardModule;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private String accountNumberApi;
    private String contragentAccount;
    private Double amountApi;
    private String currencyApi;
    private String operationTypeApi;
    private Double debit;
    private Double credit;

    public String getFormattedAmountWithCurrency() {
        if (amountApi == null || currencyApi == null) {
            return "";
        }

        String formattedAmount;
        if (debit != null && debit > 0) {
            formattedAmount = String.format("-%.2f", amountApi);
        } else if (credit != null && credit > 0) {
            formattedAmount = String.format("%.2f", amountApi);
        } else {
            formattedAmount = String.format("%.2f", amountApi);
        }

        String currencySymbol = currencyApi.equalsIgnoreCase("GEL") ? "₾" : currencyApi;
        return formattedAmount + currencySymbol;
    }

    public boolean isDebitTransaction() {
        return debit != null && debit > 0;
    }

    public boolean isCreditTransaction() {
        return credit != null && credit > 0;
    }
}
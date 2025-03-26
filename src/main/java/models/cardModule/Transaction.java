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
        // დებეტის ტრანზაქციისთვის (გადარიცხვა) UI-ზეა უარყოფითი, დადებითი მოდის API-დან
        if (debit != null && debit > 0) {
            // ამოვაბრუნოთ უარყოფითი თანხა
            formattedAmount = String.format("-%.2f", amountApi);
        }
        // კრედიტის ტრანზაქციისთვის (ჩარიცხვა) UI-ზეა დადებითი, API-დანაც დადებითი მოდის
        else if (credit != null && credit > 0) {
            // ამოვაბრუნოთ დადებითი თანხა
            formattedAmount = String.format("%.2f", amountApi);
        }
        else {
            formattedAmount = String.format("%.2f", amountApi);
        }

        String currencySymbol = currencyApi.equalsIgnoreCase("GEL") ? "₾" : currencyApi;
        return formattedAmount + currencySymbol;
    }
}
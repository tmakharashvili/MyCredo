package models.cardModule;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

public class MoneyTransfer {
    private String senderAccountNumberApi;
    private String receiverAccountNumberApi;
    private Double amountApi;
    private String currencyApi;

    public String getFormattedAmountWithCurrency() {
        if (amountApi == null || currencyApi == null) {
            return "";
        }

        // თანხისა და ვალუტის ფორმატირება UI-ს ფორმატთან შესაბამისად
        return String.format("%.2f %s", amountApi, currencyApi);
    }

    public MoneyTransfer() {
    }

    public MoneyTransfer(String senderAccountNumberApi, String receiverAccountNumberApi, Double amountApi, String currencyApi) {
        this.senderAccountNumberApi = senderAccountNumberApi;
        this.receiverAccountNumberApi = receiverAccountNumberApi;
        this.amountApi = amountApi;
        this.currencyApi = currencyApi;
    }
}

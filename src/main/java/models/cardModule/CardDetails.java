package models.cardModule;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CardDetails {
    private String cardNickName;
    private String accountNumber;

    public CardDetails(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    public CardDetails() {

    }
}
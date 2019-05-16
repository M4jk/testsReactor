package edu.iis.mto.testreactor.exc3;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

public class AtmMachineTest {

    private CardProviderService cardService;
    private BankService bankService;
    private MoneyDepot moneyDepot;

    private AtmMachine atmMachine;

    private Payment payment;

    private Money money;
    private Card card;
    private AuthenticationToken authenticationToken;

    @Before
    public void setUp() {
        bankService = mock(BankService.class);
        cardService = mock(CardProviderService.class);
        moneyDepot = mock(MoneyDepot.class);

        atmMachine = new AtmMachine(cardService, bankService, moneyDepot);
    }

    @Test
    public void itCompiles() {
        assertThat(true, equalTo(true));
    }

    @Test(expected = WrongMoneyAmountException.class)
    public void shouldThrowWrongMoneyAmountExceptionWhenMoneyIsZero() {
        money = Money.builder()
                .withAmount(0)
                .withCurrency(Currency.PL)
                .build();

        card = Card.builder()
                .withCardNumber("numer")
                .withPinNumber(1)
                .build();

        atmMachine.withdraw(money, card);
    }


}

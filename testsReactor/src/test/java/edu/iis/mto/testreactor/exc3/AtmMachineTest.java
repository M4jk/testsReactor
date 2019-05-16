package edu.iis.mto.testreactor.exc3;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    @Test(expected = WrongMoneyAmountException.class)
    public void shouldThrowWrongMoneyAmountExceptionWhenMoneyCannotBePayedWithBanknotes() {
        money = Money.builder()
                     .withAmount(1)
                     .withCurrency(Currency.PL)
                     .build();

        card = Card.builder()
                   .withCardNumber("numer")
                   .withPinNumber(1)
                   .build();

        atmMachine.withdraw(money, card);
    }

    @Test(expected = CardAuthorizationException.class)
    public void shouldThrowCardAuthorizationExceptionWhenAuthorizationCodeIsWrong() {
        money = Money.builder()
                     .withAmount(10)
                     .withCurrency(Currency.PL)
                     .build();

        card = Card.builder()
                   .withCardNumber("numer")
                   .withPinNumber(1)
                   .build();

        when(cardService.authorize(card)).thenReturn(Optional.empty());

        atmMachine.withdraw(money, card);
    }

    @Test
    public void shouldReturn100PLBanknote() {
        money = Money.builder()
                     .withAmount(100)
                     .withCurrency(Currency.PL)
                     .build();

        card = Card.builder()
                   .withCardNumber("numer")
                   .withPinNumber(1)
                   .build();

        authenticationToken = AuthenticationToken.builder()
                                                 .withUserId("numer")
                                                 .withAuthorizationCode(1)
                                                 .build();

        when(cardService.authorize(card)).thenReturn(Optional.ofNullable(authenticationToken));
        when(bankService.charge(authenticationToken, money)).thenReturn(true);
        when(moneyDepot.releaseBanknotes(Matchers.anyList())).thenReturn(true);

        Payment payment = atmMachine.withdraw(money, card);

        List<Banknote> list = new ArrayList<>();
        list.add(Banknote.PL100);

        assertThat(payment.getValue(), is(list));
    }

    @Test
    public void shouldCallCommitOnce() {
        money = Money.builder()
                     .withAmount(100)
                     .withCurrency(Currency.PL)
                     .build();

        card = Card.builder()
                   .withCardNumber("numer")
                   .withPinNumber(1)
                   .build();

        authenticationToken = AuthenticationToken.builder()
                                                 .withUserId("numer")
                                                 .withAuthorizationCode(11)
                                                 .build();

        when(cardService.authorize(card)).thenReturn(Optional.ofNullable(authenticationToken));
        when(bankService.charge(authenticationToken, money)).thenReturn(true);
        when(moneyDepot.releaseBanknotes(Matchers.anyList())).thenReturn(true);

        atmMachine.withdraw(money, card);

        verify(bankService, times(1)).commit(authenticationToken);
    }


}

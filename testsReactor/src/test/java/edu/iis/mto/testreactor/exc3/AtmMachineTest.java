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

        card = Card.builder()
                   .withCardNumber("numer")
                   .withPinNumber(1)
                   .build();

        authenticationToken = AuthenticationToken.builder()
                                                 .withUserId("numer")
                                                 .withAuthorizationCode(1)
                                                 .build();

        money = Money.builder()
                     .withAmount(100)
                     .withCurrency(Currency.PL)
                     .build();

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

        atmMachine.withdraw(money, card);
    }

    @Test(expected = WrongMoneyAmountException.class)
    public void shouldThrowWrongMoneyAmountExceptionWhenMoneyCannotBePayedWithBanknotes() {
        money = Money.builder()
                     .withAmount(1)
                     .withCurrency(Currency.PL)
                     .build();

        atmMachine.withdraw(money, card);
    }

    @Test(expected = CardAuthorizationException.class)
    public void shouldThrowCardAuthorizationExceptionWhenAuthorizationCodeIsWrong() {
        money = Money.builder()
                     .withAmount(10)
                     .withCurrency(Currency.PL)
                     .build();

        when(cardService.authorize(card)).thenReturn(Optional.empty());

        atmMachine.withdraw(money, card);
    }

    @Test
    public void shouldReturn100PLBanknote() {
        when(cardService.authorize(card)).thenReturn(Optional.ofNullable(authenticationToken));
        when(bankService.charge(authenticationToken, money)).thenReturn(true);
        when(moneyDepot.releaseBanknotes(Matchers.anyList())).thenReturn(true);

        payment = atmMachine.withdraw(money, card);

        List<Banknote> expectedBanknotesList = new ArrayList<>();
        expectedBanknotesList.add(Banknote.PL100);

        assertThat(payment.getValue(), is(expectedBanknotesList));
    }

    @Test
    public void shouldCallCommitOnce() {
        when(cardService.authorize(card)).thenReturn(Optional.ofNullable(authenticationToken));
        when(bankService.charge(authenticationToken, money)).thenReturn(true);
        when(moneyDepot.releaseBanknotes(Matchers.anyList())).thenReturn(true);

        atmMachine.withdraw(money, card);

        verify(bankService, times(1)).commit(authenticationToken);
    }

    @Test
    public void shouldCallStartOnce() {
        when(cardService.authorize(card)).thenReturn(Optional.ofNullable(authenticationToken));
        when(bankService.charge(authenticationToken, money)).thenReturn(true);
        when(moneyDepot.releaseBanknotes(Matchers.anyList())).thenReturn(true);

        atmMachine.withdraw(money, card);

        verify(bankService, times(1)).startTransaction(authenticationToken);
    }

    @Test(expected = InsufficientFundsException.class)
    public void shouldThrowInsufficientFundsExceptionIfMoneyToChargeIsBiggerThenOnAccount() {
        Money moneyToCharge = Money.builder()
                                   .withAmount(110)
                                   .withCurrency(Currency.PL)
                                   .build();

        when(cardService.authorize(card)).thenReturn(Optional.ofNullable(authenticationToken));
        when(bankService.charge(authenticationToken, moneyToCharge)).thenReturn(false);

        atmMachine.withdraw(money, card);
    }

    @Test
    public void shouldReturn110EUInBanknotes() {
        money = Money.builder()
                     .withAmount(110)
                     .withCurrency(Currency.EU)
                     .build();

        when(cardService.authorize(card)).thenReturn(Optional.ofNullable(authenticationToken));
        when(bankService.charge(authenticationToken, money)).thenReturn(true);
        when(moneyDepot.releaseBanknotes(Matchers.anyList())).thenReturn(true);

        payment = atmMachine.withdraw(money, card);

        List<Banknote> expectedBanknotesList = new ArrayList<>();
        expectedBanknotesList.add(Banknote.EU10);
        expectedBanknotesList.add(Banknote.EU100);

        assertThat(payment.getValue(), is(expectedBanknotesList));
    }

    @Test(expected = MoneyDepotException.class)
    public void shouldThrowMoneyDepotException() {
        when(cardService.authorize(card)).thenReturn(Optional.ofNullable(authenticationToken));
        when(bankService.charge(authenticationToken, money)).thenReturn(true);

        payment = atmMachine.withdraw(money, card);
    }

    @Test
    public void shouldCallChargeThreeTimes() {
        when(cardService.authorize(card)).thenReturn(Optional.ofNullable(authenticationToken));
        when(bankService.charge(authenticationToken, money)).thenReturn(true);
        when(moneyDepot.releaseBanknotes(Matchers.anyList())).thenReturn(true);

        atmMachine.withdraw(money, card);
        atmMachine.withdraw(money, card);
        atmMachine.withdraw(money, card);

        verify(bankService, times(3)).charge(authenticationToken, money);
    }

}

package bg.uni.sofia.fmi.mjt.wallet.client.command;

import bg.uni.sofia.fmi.mjt.wallet.client.exception.InvalidUserCommandException;
import bg.uni.sofia.fmi.mjt.wallet.client.ui.UI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

public class CommandCreatorTest {
    @Mock
    private UI ui;

    @InjectMocks
    private CommandCreator commandCreator;

    @Test
    void testReadCommandWithValidInput() throws InvalidUserCommandException {
        when(ui.read()).thenReturn("register username password");
        Command result = commandCreator.readCommand();
        assertEquals(CommandLabel.REGISTER, result.getCommandLabel(), "Command label should be parsed correctly");
        assertArrayEquals(new String[]{"username", "password"}, result.getArguments(), "Arguments should be parsed correctly");
        verify(ui).read();
    }

    @Test
    void testReadCommandWithInvalidUserCommand() {
        when(ui.read()).thenReturn("invalidCommand arg1 arg2");
        assertThrows(InvalidUserCommandException.class, () -> commandCreator.readCommand(),
            "Read Command should throw InvalidUserCommandException for invalid command");
        verify(ui).read();
    }

    @Test
    void testReadCommandWithNoArguments() throws InvalidUserCommandException {
        when(ui.read()).thenReturn("get-wallet-summary");
        Command result = commandCreator.readCommand();
        assertEquals(CommandLabel.WALLET_SUMMARY, result.getCommandLabel(), "Command label should be parsed correctly");
        assertArrayEquals(new String[]{}, result.getArguments(), "Arguments should be an empty array");
        verify(ui).read();
    }
}

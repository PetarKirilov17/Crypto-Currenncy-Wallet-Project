package bg.uni.sofia.fmi.mjt.wallet.client.command;

import bg.uni.sofia.fmi.mjt.wallet.client.ui.UI;
import bg.uni.sofia.fmi.mjt.wallet.client.validation.StringValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandValidator {
    private static final String RES_DIRECTORY = "res";
    private static final String ASSET_IDS_FILE_PATH = "assetIds.txt";
    private Path assetIdsPath = Path.of(RES_DIRECTORY,ASSET_IDS_FILE_PATH).toAbsolutePath();
    private static final String DOUBLE_REGEX = "-?\\d+(\\.\\d+)?";
    private static final int ONE_DESIRED_ARG = 1;
    private static final int TWO_DESIRED_ARGS = 2;
    private static final int ZERO_DESIRED_ARGS = 0;
    private static final String INVALID_SIZE_OF_ARGS = "You passed invalid count of arguments! ";
    private static final String WRONG_USERNAME_MESSAGE = "Username has to be between " + StringValidator.MIN_USERNAME_LENGTH + " and " + StringValidator.MAX_USERNAME_LENGTH + " characters long! It must contain only alphanumeric characters!";
    private static final String WRONG_PASSWORD_MESSAGE = "Password has to be between " + StringValidator.MIN_PASSWORD_LENGTH + " and " + StringValidator.MAX_PASSWORD_LENGTH;
    private static final String WRONG_DOUBLE_NUMBER_MESSAGE = "Double number is not in correct format! Please insert a valid number!";
    private static final String WRONG_ASSET_ID_MESSAGE = "There is no asset with this asset ID!";
    private UI ui;
    private final Set<String> assetIds;
    public CommandValidator(UI ui){
        this.ui = ui;
        assetIds = new HashSet<>(loadAssetIdsFromFile());
    }

    public boolean validateRegisterAndSignUp(Command command){
        if(!checkSizeOfPassedArguments(command.getArguments().length, TWO_DESIRED_ARGS)){
            return false;
        }

        if(!StringValidator.isValidUsername(command.getArguments()[0])){
            ui.writeError(WRONG_USERNAME_MESSAGE);
            return false;
        }
        if(!StringValidator.isValidPassword(command.getArguments()[1])){
            ui.writeError(WRONG_PASSWORD_MESSAGE);
            return false;
        }
        return true;
    }


    public boolean validateDepositMoney(Command command){
        if(!checkSizeOfPassedArguments(command.getArguments().length, ONE_DESIRED_ARG)){
            return false;
        }
        if(!isValidDouble(command.getArguments()[0])){
            ui.writeError(WRONG_DOUBLE_NUMBER_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean validateBuyAsset(Command command){
        if(!checkSizeOfPassedArguments(command.getArguments().length, TWO_DESIRED_ARGS)){
            return false;
        }
        if(!isValidAssetId(command.getArguments()[0].toUpperCase())){
            ui.writeError(WRONG_ASSET_ID_MESSAGE);
            return false;
        }
        if(!isValidDouble(command.getArguments()[1])){
            ui.writeError(WRONG_DOUBLE_NUMBER_MESSAGE);
            return false;
        }
        return true;
    }
    public boolean validateSellAsset(Command command){
        if(!checkSizeOfPassedArguments(command.getArguments().length, ONE_DESIRED_ARG)){
            return false;
        }
        if(!isValidAssetId(command.getArguments()[0].toUpperCase())){
            ui.writeError(WRONG_ASSET_ID_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean validateCommandsWithNoArguments(Command command){
        return checkSizeOfPassedArguments(command.getArguments().length, ZERO_DESIRED_ARGS);
    }

    private boolean isValidDouble(String str){
        return str.matches(DOUBLE_REGEX);
    }

    private boolean checkSizeOfPassedArguments(int argsCount, int desiredCount){
        if(argsCount != desiredCount){
            ui.writeError(INVALID_SIZE_OF_ARGS + " Desired count: " + desiredCount + " | Count of passed arguments: " + argsCount);
            return false;
        }
        return true;
    }
    private  List<String> loadAssetIdsFromFile() {
        try (BufferedReader reader = Files.newBufferedReader(assetIdsPath)) {
            return reader.lines().toList();
        } catch (IOException e) {
           throw new UncheckedIOException("Error occurred while loading the file", e);
        }
    }

    private boolean isValidAssetId(String userInput) {
        return assetIds.contains(userInput);
    }
}

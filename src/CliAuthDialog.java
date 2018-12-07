import java.util.Scanner;

public class CliAuthDialog {

    public CliAuthDialog() {
    }

    /**
     * This function will prompt the user to authenticate.
     *
     * @param link the authentication link the user should use.
     * @return the authentication code from the user.
     */
    public static String promptForCode(String link) {
        //User prompt stuff
        System.out.println("Please follow this link, and then copy and paste the code below.");
        System.out.println(link);
        Scanner scanner = new Scanner(System.in);
        System.out.print("Code: ");
        String code = scanner.nextLine();
        return code;
    }
}
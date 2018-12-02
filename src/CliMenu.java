import java.util.ArrayList;
import java.util.Scanner;

public class CliMenu {
    private ArrayList<String> options;
    private String prompt;
    private boolean isMultipleChoice;
    private Scanner scanner;

    public CliMenu() {
        scanner = new Scanner(System.in);
    }

    public CliMenu(ArrayList<String> options, String prompt, boolean isMultipleChoice) {
        this.options = options;
        this.prompt = prompt;
        this.isMultipleChoice = isMultipleChoice;
        scanner = new Scanner(System.in);
    }

    public int[] run() {
        int[] ret = new int[0];
        boolean error = true;
        while (error) {
            error = false;
            System.out.println((!isMultipleChoice) ? prompt : (prompt + " (Delimit multiple options with commas e.g. 1,3,4)"));
            for (int i = 0; i < options.size(); i++) {
                System.out.printf("[%d] %s\n", i + 1, options.get(i));
            }
            System.out.print((isMultipleChoice) ? "Options: " : "Option: ");
            if (isMultipleChoice) {
                String input = scanner.nextLine();
                //Separate options
                String[] optionStrs = input.split(",");
                ret = new int[optionStrs.length];
                for (int i = 0; i < optionStrs.length; i++) {
                    optionStrs[i] = optionStrs[i].trim();
                    try {
                        ret[i] = Integer.parseInt(optionStrs[i]);
                    } catch (NumberFormatException e) {
                        System.out.printf("\n'%s' is not a number, please try again.\n\n", optionStrs[i]);
                        error = true;
                        continue;
                    }
                    //Not in range
                    if (ret[i] < 1 || ret[i] > options.size()) {
                        System.out.printf("\n'%s' is not in the range of %d-%d, please try again.\n\n", optionStrs[i], 1, options.size());
                        error = true;
                        continue;
                    }
                }
            } else {
                String input = scanner.nextLine();
                ret = new int[1];
                if (input.contains(",")) {
                    error = true;
                    System.out.println("\nOnly one choice allowed, please try again.\n");
                    continue;
                }
                try {
                    ret[0] = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.printf("\n'%s' is not a number, please try again\n\n", input);
                    continue;
                }
                if (ret[0] < 1 || ret[0] > options.size()) {
                    System.out.printf("\n'%s' is not in the range of %d-%d, please try again.\n\n", input, 1, options.size());
                    error = true;
                    continue;
                }
            }
        }
        for (int i = 0; i < ret.length; i++) {
            ret[i]--;
        }
        return ret;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = options;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}

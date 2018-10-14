/*
 * Visa: 13 or 16 digits, starting with 4.
 * MasterCard: 16 digits, starting with 51 through 55.
 * Discover: 16 digits, starting with 6011 or 65.
 * American Express: 15 digits, starting with 34 or 37.
 * Diners Club: 14 digits, starting with 300 through 305, 36, or 38.
 * JCB: 15 digits, starting with 2131 or 1800, or 16 digits starting with 35.
 * Maestro: Between 12 and 19 digits, starting with 50, 56, 57, 58, 6304, 6390 or 67.
 */
package PerDa;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ruimfsantos
 */
public class ValidateCCNumbers {

    public static void main(String[] args) {
        List<String> cards = new ArrayList<String>();

        //Valid Credit Cards
        cards.add("xxxx-xxxx-xxxx-xxxx"); //Masked to avoid any inconvenience unknowingly

        //Invalid Credit Card
        cards.add("xxxx-xxxx-xxxx-xxxx"); //Masked to avoid any inconvenience unknowingly

        String regex = "^(?:(?<visa>4[0-9]{12}(?:[0-9]{3})?)|"
                + "(?<mastercard>5[1-5][0-9]{14})|"
                + "(?<discover>6(?:011|5[0-9]{2})[0-9]{12})|"
                + "(?<amex>3[47][0-9]{13})|"
                + "(?<diners>3(?:0[0-5]|[68][0-9])?[0-9]{11})|"
                + "(?<jcb>(?:2131|1800|35[0-9]{3})[0-9]{11}))|"
                + "(?<maestro>(?:5[0678]\\d\\d|6304|6390|67\\d\\d)\\d{8,15}$";

        Pattern pattern = Pattern.compile(regex);

        for (String card : cards) {
            //Strip all hyphens
            card = card.replaceAll("-", "");

            //Match the card
            Matcher matcher = pattern.matcher(card);

            System.out.println(matcher.matches());

            if (matcher.matches()) {
                //If card is valid then verify which group it belong
                System.out.println(matcher.group("mastercard"));
            }
        }

    }

    public static boolean CheckLuhn(String ccNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = ccNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(ccNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }
    
}

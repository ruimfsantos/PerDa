/**
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PerDa;

/**
 * @author ruimfsantos
 */
public class Validador {

    /**
     * Regras para validação do NIF. - Tem de ter 9 dígitos - O primeiro dígito
     * tem de ser 1, 2, 5, 6, 8 ou 9 (Na realidade o 3 está reservado para uso
     * de particulares assim que os começados por 2 se esgotarem, o 4 e 7 são
     * utilizados em casos especiais, pelo que, por omissão, a função ignora
     * esta validação) - Para o dígito de controlo (último digíto do NIF): º
     * 9*d1 + 8*d2 + 7*d3 + 6*d4 + 5*d5 + 4*d6 + 3*d7 + 2*d8 + 1*d9 (em que d1 a
     * d9 são os 9 dígitos do NIF); º A soma de controlo tem de ser múltiplo de
     * 11 (quando divídida por 11 dar 0); º Subtraír o resto da divisão da soma
     * por 11 a 11; º Se o resultado for 10, é assumído o algarismo 0;
     */
    public static boolean NIF(String nifNumber) {
        final int max = 9;

        //check if is numeric and has 9 numbers
        if (!nifNumber.matches("[0-9]+") || nifNumber.length() != max) {
            return false;
        }
        int checkSum = 0;

        //calculate checkSum
        for (int i = 0; i < max - 1; i++) {
            checkSum += (nifNumber.charAt(i) - '0') * (max - i);
        }
        int checkDigit = 11 - (checkSum % 11);

        //if checkDigit is higher than TEN set it to zero
        if (checkDigit >= 10) {
            checkDigit = 0;
        }

        //compare checkDigit with the last number of NIF
        return checkDigit == nifNumber.charAt(max - 1) - '0';
    }

    /**
     * Verificação do NISS (Número de Identificação na Segurança Social). - É
     * composto por onze algarismos, sendo o último o check-digit - Pessoa
     * Singular (PS) começa por 1 - Pessoa Colectiva (PC) começa por 2 - Para o
     * dígito de controlo (último digíto do NISS - d11): º
     * d1*29+d2*23+d3*19+d4+17+d5*13+d6*11+d7*7+d8*5+d9*3+d10*2 º O resultado da
     * operação anterior, dividi-se por 10 º Substrai-se a 9 o valor do resto
     * obtido na divisão º O valor obtido pela diferença anterior é o d11
     * (digito de controlo)
     */
    public static boolean NISS(String niss) {
        return hasValidLength(niss)
                && isNumeric(niss)
                && hasValidFirstDigit(niss)
                && hasValidControlDigit(niss);
    }

    private static boolean hasValidLength(String niss) {
        return !(niss == null || niss.length() != 11);
    }

    private static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasValidFirstDigit(String niss) {
        return niss.charAt(0) == '1' || niss.charAt(0) == '2';
    }

    private static boolean hasValidControlDigit(String niss) {
        int[] nissArray = convertToIntegerArray(niss);
        final int[] FACTORS = {29, 23, 19, 17, 13, 11, 7, 5, 3, 2};
        int sum = 0;
        for (int i = 0; i < FACTORS.length; i++) {
            sum += nissArray[i] * FACTORS[i];
        }
        return nissArray[nissArray.length - 1] == (9 - (sum % 10));
    }

    private static int[] convertToIntegerArray(String niss) {
        final int[] ints = new int[niss.length()];
        for (int i = 0; i < niss.length(); i++) {
            ints[i] = Integer.parseInt(String.valueOf(niss.charAt(i)));
        }
        return ints;
    }

    /** 
     * Check Luhn...
     * Validação usada no Cartão Cidadão português e nos cartões de crédito
     * Adaptado de https://www.autenticacao.gov.pt/documents/10179/11463/Valida%C3%A7%C3%A3o+de+N%C3%BAmero+de+Documento+do+Cart%C3%A3o+de+Cidad%C3%A3o/0dbc446b-3718-41e5-b982-551a72f8b8a8
     */

    public static boolean CheckLuhn(String ccNumber) {
        int sum = 0;
        boolean secondDigit = false;

        for (int i = ccNumber.length() - 1; i >= 0; i--) {
            int valor = Integer.parseInt(ccNumber.substring(i, i + 1));
            if (secondDigit) {
                valor *= 2;
                if (valor > 9) {
                    valor = (valor % 10) + 1;
                }
            }
            sum += valor;
            secondDigit = !secondDigit;
        }
        return (sum % 10 == 0);
    }

    public int GetNumberFromChar(char letter) {
        switch (letter) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case 'A':
                return 10;
            case 'B':
                return 11;
            case 'C':
                return 12;
            case 'D':
                return 13;
            case 'E':
                return 14;
            case 'F':
                return 15;
            case 'G':
                return 16;
            case 'H':
                return 17;
            case 'I':
                return 18;
            case 'J':
                return 19;
            case 'K':
                return 20;
            case 'L':
                return 21;
            case 'M':
                return 22;
            case 'N':
                return 23;
            case 'O':
                return 24;
            case 'P':
                return 25;
            case 'Q':
                return 26;
            case 'R':
                return 27;
            case 'S':
                return 28;
            case 'T':
                return 29;
            case 'U':
                return 30;
            case 'V':
                return 31;
            case 'W':
                return 32;
            case 'X':
                return 33;
            case 'Y':
                return 34;
            case 'Z':
                return 35;
        }
        return letter;

    }
    
    public static boolean CCisValid(String idCard) {
        if (idCard.length() != 13) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Character.getNumericValue(idCard.charAt(i)) * (13 - i);
        }
        return (11 - sum % 11) % 10 == Character.getNumericValue(idCard.charAt(12));

    }

}

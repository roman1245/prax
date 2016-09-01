package xyz.kandrac.library.billing.util;

/**
 * Created by Jan Kandrac on 16.7.2016.
 */
public class IABKeyEncoder {

    private static String MIDDLE = "BAQEFAAOCAQ8AMIIBCgKCAQEAkx7q73ebYDvhdJPF1xgWoffCoeKv8BShi+U" +
            "B3ogcK8TBQWrACIML8VPtb1Y8nK3FOTfY63fejLs55nftMYOA9P2jrTUS/qp6ZlsxDaL0t49SofgPQXUKLx" +
            "9rCBrHB/Ch82ZTP+HmyPa1IllSsgi2mo/IkOV2OqKM46+Dw1DnFMhmy5QPgAcFtt9DQ0nszWOxnLmdY35kT" +
            "+Rw6vcfEPRd+WkyGGW8yGbFSq1qCN67Q9Lu6bvXFEihtfHtdbb5KJtce4s+r6Y5K/2vdYNMw3z53zFNhX8h" +
            "wJiwZJbg+EfWPyOkGAdcRhqY7LnpMxBP7PofUsC7UfWrKa";

    private static String START = "NJJCJkBOChlriljH:x1";
    private static String END = "BAQADIwkHcpU8Ubv++";

    public static String getKey() {
        return decrementLetters(START) + MIDDLE + reverseString(END);
    }

    private static String decrementLetters(String text) {
        StringBuilder sb = new StringBuilder();
        char[] x = text.toCharArray();
        for (char dec : x) {
            sb.append((char)(dec - 1));
        }
        return sb.toString();
    }

    private static String reverseString(String text) {
        StringBuilder sb = new StringBuilder();
        char[] x = text.toCharArray();
        for (int i = x.length - 1; i >= 0; i--) {
            sb.append(x[i]);
        }
        return sb.toString();
    }
}

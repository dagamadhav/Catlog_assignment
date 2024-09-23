import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SecretSharing {
    public static void main(String[] args) {
        try {
            // Step 1: Read JSON file content
            BufferedReader reader = new BufferedReader(new FileReader("org1.json"));
            
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();

            // Step 2: Extract 'n' and 'k' from JSON (number of roots, minimum roots required)
            int n = extractInt(content.toString(), "\"n\"");
            int k = extractInt(content.toString(), "\"k\"");

            // Step 3: Store each root's (x, y) pairs from JSON
            HashMap<Integer, String> encodedRoots = new HashMap<>();
            HashMap<Integer, BigInteger> decodedRoots = new HashMap<>();
            Pattern pattern = Pattern.compile("\"(\\d+)\":\\s*\\{\\s*\"base\":\\s*\"(\\d+)\",\\s*\"value\":\\s*\"(\\w+)\"\\s*\\}");
            Matcher matcher = pattern.matcher(content.toString());

            while (matcher.find()) {
                int x = Integer.parseInt(matcher.group(1));    // 'x' value
                int base = Integer.parseInt(matcher.group(2)); // base of encoded 'y'
                String encodedY = matcher.group(3);            // encoded 'y' value

                // Store encoded (x, y) as a string
                encodedRoots.put(x, encodedY);
                
                // Decode 'y' using its base and store
                BigInteger decodedY = new BigInteger(encodedY, base);
                decodedRoots.put(x, decodedY);
            }

            // Print the decoded values
            System.out.println("Decoded Values:");
            for (Integer x : decodedRoots.keySet()) {
                System.out.println("Key: " + x + ", Decoded Value: " + decodedRoots.get(x));
            }

            // Step 4: Calculate the constant term 'c' using Lagrange interpolation
            BigInteger constant = calculateConstant(decodedRoots, k);
            System.out.println("The constant term 'c' is: " + constant);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to extract integers like 'n' and 'k' from JSON
    private static int extractInt(String json, String key) {
        Pattern pattern = Pattern.compile(key + ":\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new IllegalArgumentException("Missing key: " + key);
    }

    // Lagrange interpolation to find the constant term 'c'
    public static BigInteger calculateConstant(HashMap<Integer, BigInteger> roots, int k) {
        BigInteger result = BigInteger.ZERO;
        Integer[] xValues = roots.keySet().toArray(new Integer[0]);
        BigInteger[] yValues = new BigInteger[roots.size()];
    
        int index = 0;
        for (Integer x : roots.keySet()) {
            if (index < k) {
                yValues[index] = roots.get(x);
                index++;
            }
        }
    
        // Lagrange Interpolation calculation
        for (int i = 0; i < k; i++) {
            BigInteger term = yValues[i];
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;
    
            for (int j = 0; j < k; j++) {
                if (i != j) {
                    numerator = numerator.multiply(BigInteger.valueOf(-xValues[j]));
                    denominator = denominator.multiply(BigInteger.valueOf(xValues[i] - xValues[j]));
                }
            }
    
            term = term.multiply(numerator).divide(denominator);
            result = result.add(term);
        }
        return result;
    }
    
}

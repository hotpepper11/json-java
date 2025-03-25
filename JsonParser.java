import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class JsonParser {
    private String jsonString;

    public JsonParser(String jsonString) {
        this.jsonString = jsonString.replaceAll("\\s+", "");
    }

    public Object read() {
        if (this.jsonString.length() == 0) return null;

        switch (this.jsonString.charAt(0)) {
            case '{':
                return parseHashMap(this.jsonString);
            
            case '[':
                return parseArray(this.jsonString);
        
            default:
                System.out.println("JSON String is not valid. Does you JSON have an object or an array in it?");
                break;
        }

        return null;
    }

    public HashMap<String, Object> parseHashMap(String jsonString) {
        HashMap<String, Object> hashMap = new HashMap<>();

        char[] charArray = jsonString.toCharArray();

        boolean inValueOrKey = false;
        String currentString = "";
        String currentKey = "";
        boolean onReverseSolidus = false; // For the character '\'
        String valueType = "";
        boolean inString = false;

        int hashMapsStack = 0;
        int startInnerHashMapIdx = 0;

        int arraysStack = 0;
        int startInnerArrayIdx = 0;

        // Start at index 1 to skip '{'
        for (int i = 1; i < charArray.length; i++) {
            if (hashMapsStack > 0) {
                if (charArray[i] == '{') {
                    hashMapsStack += 1;
                }

                if (charArray[i] == '}') {
                    hashMapsStack -= 1;

                    // End the last inner hash map
                    if (hashMapsStack == 0) {
                        HashMap<String, Object> result = parseHashMap(jsonString.substring(startInnerHashMapIdx, i + 1));

                        hashMap.put(currentKey, result);

                        valueType = "";
                    }
                }

                continue;
            }

            if (arraysStack > 0) {
                if (charArray[i] == '[') {
                    arraysStack += 1;
                }

                if (charArray[i] == ']') {
                    arraysStack -= 1;

                    // End the last inner hash map
                    if (arraysStack == 0) {
                        List<Object> result = parseArray(jsonString.substring(startInnerArrayIdx, i + 1));

                        hashMap.put(currentKey, result);

                        valueType = "";
                    }
                }

                continue;
            }

            // Skip whitespace if the value is not currently in a string
            if (!inString && (charArray[i] == ' ' || charArray[i] == '\n' || charArray[i] == '}')) continue;

            if (inValueOrKey) {
                currentString += charArray[i];
            }

            if (charArray[i] == '\\') {
                onReverseSolidus = true;

                continue;
            }

            if (onReverseSolidus) {
                onReverseSolidus = false;

                continue;
            }

            if (charArray[i] == '"') {
                inString = !inString;
                inValueOrKey = inString;
                if (inString) valueType = "string";

                // Remove the last character (it will be an end quote)
                if (currentString.length() > 0) currentString = currentString.substring(0, currentString.length() - 1);

                continue;
            }

            if (charArray[i] == ':') {
                // Reset all values
                currentKey = currentString;
                currentString = "";
                valueType = "";
                inString = false;
                inValueOrKey = true;

                continue;
            }

            if (charArray[i] == ',') {
                // The currentString is the currentValue
                if (valueType == "string") {
                    hashMap.put(currentKey, currentString);
                } 

                if (valueType == "number") {
                    // Remove the last char because it will be ','
                    hashMap.put(currentKey, convertStringToNumber(currentString.substring(0, currentString.length() - 1)));
                }

                if (valueType == "true") {
                    hashMap.put(currentKey, true);
                }

                if (valueType == "false") {
                    hashMap.put(currentKey, false);
                }

                if (valueType == "null") {
                    hashMap.put(currentKey, null);
                }

                currentKey = "";
                currentString = "";
                valueType = "";

                continue;
            }

            // If the current value is not a string
            if (!inString && valueType == "" && currentKey.length() > 0) {
                // If the first character of the value is a number
                if (convertStringToNumber(Character.toString(charArray[i])) != null) {
                    valueType = "number";
                }

                // If the first character is t (leading to true)
                if (charArray[i] == 't') {
                    valueType = "true";
                }

                // If the first character is t (leading to true)
                if (charArray[i] == 'f') {
                    valueType = "false";
                }

                // If the first character is t (leading to true)
                if (charArray[i] == 'n') {
                    valueType = "null";
                }

                // If the first character is { (leading to another object)
                if (charArray[i] == '{') {
                    hashMapsStack += 1;

                    startInnerHashMapIdx = i;
                }

                // If the first character is [ (leading to an array)
                if (charArray[i] == '[') {
                    arraysStack += 1;

                    startInnerArrayIdx = i;
                }
            }
        }

        // The currentString is the currentValue
        if (valueType == "string") {
            hashMap.put(currentKey, currentString);
        } 
        
        if (valueType == "number") {
            hashMap.put(currentKey, convertStringToNumber(currentString));
        }

        if (valueType == "true") {
            hashMap.put(currentKey, true);
        }

        if (valueType == "false") {
            hashMap.put(currentKey, false);
        }

        if (valueType == "null") {
            hashMap.put(currentKey, null);
        }

        return hashMap;
    }

    public List<Object> parseArray(String jsonString) {
        List<Object> result = new ArrayList<>();

        char[] charArray = jsonString.toCharArray();

        int arraysStack = 0;
        int startInnerArrayIdx = 0;

        int hashMapsStack = 0;
        int startInnerHashMapIdx = 0;

        String valueType = "";
        boolean inString = false;
        String currentString = "";
        boolean onReverseSolidus = false;

        for (int i = 1; i < charArray.length; i++) {
            if (hashMapsStack > 0) {
                if (charArray[i] == '{') {
                    hashMapsStack += 1;
                }

                if (charArray[i] == '}') {
                    hashMapsStack -= 1;

                    // End the last inner array
                    if (hashMapsStack == 0) {
                        HashMap<String, Object> innerHashMap = parseHashMap(jsonString.substring(startInnerHashMapIdx, i + 1));

                        result.add(innerHashMap);

                        valueType = "";
                    }
                }

                continue;
            }

            if (arraysStack > 0) {
                if (charArray[i] == '[') {
                    arraysStack += 1;
                }

                if (charArray[i] == ']') {
                    arraysStack -= 1;

                    // End the last inner array
                    if (arraysStack == 0) {
                        List<Object> innerArray = parseArray(jsonString.substring(startInnerArrayIdx, i + 1));

                        result.add(innerArray);

                        valueType = "";
                    }
                }

                continue;
            }

            // Skip whitespace if the value is not currently in a string
            if (!inString && (charArray[i] == ' ' || charArray[i] == '\n' || charArray[i] == '}')) continue;

            currentString += charArray[i];

            if (charArray[i] == '\\') {
                onReverseSolidus = true;

                continue;
            }

            if (onReverseSolidus) {
                onReverseSolidus = false;

                continue;
            }

            if (charArray[i] == '"') {
                inString = !inString;
                if (inString) valueType = "string";

                // Remove the last character (it will be an end quote)
                if (currentString.length() > 0) currentString = currentString.substring(0, currentString.length() - 1);

                continue;
            }

            if (charArray[i] == ',') {
                // The currentString is the currentValue
                if (valueType == "string") {
                    result.add(currentString);
                } 

                if (valueType == "number") {
                    // Remove the last char because it will be ','
                    result.add(convertStringToNumber(currentString.substring(0, currentString.length() - 1)));
                }

                if (valueType == "true") {
                    result.add(true);
                }

                if (valueType == "false") {
                    result.add(false);
                }

                if (valueType == "null") {
                    result.add(null);
                }

                currentString = "";
                valueType = "";

                continue;
            }

            // If the current value is not a string
            if (!inString && valueType == "") {
                // If the first character of the value is a number
                if (convertStringToNumber(Character.toString(charArray[i])) != null) {
                    valueType = "number";
                }

                // If the first character is t (leading to true)
                if (charArray[i] == 't') {
                    valueType = "true";
                }

                // If the first character is t (leading to true)
                if (charArray[i] == 'f') {
                    valueType = "false";
                }

                // If the first character is t (leading to true)
                if (charArray[i] == 'n') {
                    valueType = "null";
                }

                // If the first character is [ (leading to another array)
                if (charArray[i] == '[') {
                    arraysStack += 1;

                    startInnerArrayIdx = i;
                }

                // If the first character is { (leading to a hashmap)
                if (charArray[i] == '{') {
                    hashMapsStack += 1;

                    startInnerHashMapIdx = i;
                }
            }
        }

        // The currentString is the currentValue
        if (valueType == "string") {
            result.add(currentString);
        } 

        if (valueType == "number") {
            // Remove the last char because it will be ','
            result.add(convertStringToNumber(currentString.substring(0, currentString.length() - 1)));
        }

        if (valueType == "true") {
            result.add(true);
        }

        if (valueType == "false") {
            result.add(false);
        }

        if (valueType == "null") {
            result.add(null);
        }

        return result;
    }

    private static Number convertStringToNumber(String str) {
        if (str == null || str.isEmpty()) {
            return null; // Or throw an exception, depending on your needs.
        }

        try {
            // Attempt to parse as an Integer
            return Integer.parseInt(str);
        } catch (NumberFormatException e1) {
            try {
                // Attempt to parse as a Long
                return Long.parseLong(str);
            } catch (NumberFormatException e2) {
                try {
                    // Attempt to parse as a Double
                    return Double.parseDouble(str);
                } catch (NumberFormatException e3) {
                    try {
                        // Attempt to parse as a Float.
                        return Float.parseFloat(str);
                    } catch(NumberFormatException e4){
                        return null;
                    }
                }
            }
        }
    }

    // main method for testing
    public static void main(String[] args) {}
}

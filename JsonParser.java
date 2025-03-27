import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonParser {
    public static Object decode(String jsonString) {
        jsonString = jsonString.trim();

        if (jsonString.length() == 0) return null;

        switch (jsonString.charAt(0)) {
            case '{':
                return decodeHashMap(jsonString);
            
            case '[':
                return decodeArray(jsonString);
        
            default:
                System.out.println("JSON String is not valid. Does your JSON have an object or an array in it?");
                break;
        }

        return null;
    }

    public static String encode(Object object) {
        // TODO: How to get rid of the warning for "Unchecked cast from Object" below even though it is checked in the if statement
        if (object instanceof HashMap) {
            HashMap<Object, Object> hashmap = (HashMap<Object, Object>) object;

            return encodeHashMap(hashmap);
        }

        // TODO: How to get rid of the warning for "Unchecked cast from Object" below even though it is checked in the if statement
        if (object instanceof Object[] || object instanceof ArrayList) {
            Object[] array = (Object[]) object;

            return encodeArray(array);
        }

        return "This object is not of type hashmap or array.";
    }

    private static HashMap<String, Object> decodeHashMap(String jsonString) {
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
                        HashMap<String, Object> result = decodeHashMap(jsonString.substring(startInnerHashMapIdx, i + 1));

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
                        List<Object> result = decodeArray(jsonString.substring(startInnerArrayIdx, i + 1));

                        hashMap.put(currentKey, result);

                        valueType = "";
                    }
                }

                continue;
            }

            // Skip whitespace if the value is not currently in a string
            // if (!inString && (charArray[i] == ' ' || charArray[i] == '\n' || charArray[i] == '}')) continue;

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

    private static String encodeHashMap(HashMap<Object, Object> hashmap) {
        String output = "{";

        for (Map.Entry<Object, Object> entry : hashmap.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            output += '"' + key + '"' + ':';

            if (value instanceof String) {
                output += '"' + value.toString() + '"';
            } else {
                if (value instanceof HashMap || value instanceof Object[] || value instanceof ArrayList) value = encode(value);

                output += value.toString();
            }

            output += ',';
        }

        output = output.substring(0, output.length() - 1) + '}';

        return output;
    }

    private static String encodeArray(Object[] array) {
        String output = "[";

        for (Object element : array) {
            if (element instanceof String) {
                output += '"' + element.toString() + '"';
            } else {
                if (element instanceof HashMap || element instanceof Object[] || element instanceof ArrayList) element = encode(element);

                output += element.toString();
            }

            output += ',';
        }

        output = output.substring(0, output.length() - 1) + ']';

        return output;
    }

    private static List<Object> decodeArray(String jsonString) {
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
                        HashMap<String, Object> innerHashMap = decodeHashMap(jsonString.substring(startInnerHashMapIdx, i + 1));

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
                        List<Object> innerArray = decodeArray(jsonString.substring(startInnerArrayIdx, i + 1));

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
    public static void main(String[] args) {
        Object output = JsonParser.decode("{ \"user\": {\"id\":1, \"name\": \"Thomas Smith\", \"admin\":true } }");
        Object json = JsonParser.encode(output);

        System.out.println(json);

        Object output2 = JsonParser.decode("[1, 2, 3, 4, 5, 6, \"Hi!\",true, false, null]");
        System.out.println(output2);
        Object json2 = JsonParser.encode(output2);

        System.out.println(json2);
    }
}

import java.io.*;
import java.util.*;

public class StrongPasswordChecker {

    private static final int CHAINING_M = 1000; // Table size for separate chaining
    private static final int PROBING_M = 20000; // Table size for linear probing
    private static final String[] DICTIONARY = {
            "a", "aaron", "abandoned", "account", "accountability"
    };

    static class Entry {
        String key;
        int value;

        public Entry(String key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    static class HashTableChaining {
        private final List<Entry>[] table;

        public HashTableChaining(int size) {
            table = new ArrayList[size];
            for (int i = 0; i < size; i++) {
                table[i] = new ArrayList<>();
            }
        }

        public void put(String key, int value, boolean useCurrentHashCode) {
            int hash = computeHash(key, useCurrentHashCode) % table.length;
            if (hash < 0) hash += table.length;
            table[hash].add(new Entry(key, value));
        }

        public int search(String key, boolean useCurrentHashCode) {
            int hash = computeHash(key, useCurrentHashCode) % table.length;
            if (hash < 0) hash += table.length;
            List<Entry> bucket = table[hash];
            int comparisons = 0;
            for (Entry entry : bucket) {
                comparisons++;
                if (entry.key.equals(key)) {
                    return comparisons;
                }
            }
            return comparisons;
        }

        private int computeHash(String key, boolean useCurrentHashCode) {
            if (useCurrentHashCode) {
                return key.hashCode();
            } else {
                int hash = 0;
                int skip = Math.max(1, key.length() / 8);
                for (int i = 0; i < key.length(); i += skip) {
                    hash = (hash * 37) + key.charAt(i);
                }
                return hash;
            }
        }
    }

    static class HashTableProbing {
        private final String[] keys;
        private final int[] values;

        public HashTableProbing(int size) {
            keys = new String[size];
            values = new int[size];
        }

        public void put(String key, int value, boolean useCurrentHashCode) {
            int hash = computeHash(key, useCurrentHashCode) % keys.length;
            if (hash < 0) hash += keys.length;
            while (keys[hash] != null) {
                hash = (hash + 1) % keys.length;
            }
            keys[hash] = key;
            values[hash] = value;
        }

        public int search(String key, boolean useCurrentHashCode) {
            int hash = computeHash(key, useCurrentHashCode) % keys.length;
            if (hash < 0) hash += keys.length;
            int comparisons = 0;
            while (keys[hash] != null) {
                comparisons++;
                if (keys[hash].equals(key)) {
                    return comparisons;
                }
                hash = (hash + 1) % keys.length;
            }
            return comparisons;
        }

        private int computeHash(String key, boolean useCurrentHashCode) {
            if (useCurrentHashCode) {
                return key.hashCode();
            } else {
                int hash = 0;
                int skip = Math.max(1, key.length() / 8);
                for (int i = 0; i < key.length(); i += skip) {
                    hash = (hash * 37) + key.charAt(i);
                }
                return hash;
            }
        }
    }

    public static void main(String[] args) {
        HashTableChaining chainingTable = new HashTableChaining(CHAINING_M);
        HashTableProbing probingTable = new HashTableProbing(PROBING_M);

        // Populate hash tables with dictionary
        for (int i = 0; i < DICTIONARY.length; i++) {
            chainingTable.put(DICTIONARY[i], i + 1, false);
            probingTable.put(DICTIONARY[i], i + 1, false);
        }

        // Read passwords from file
        String filename = "C:/Users/legit/Downloads/passwords.txt"; // The file containing the passwords to check
        List<String> passwords = readPasswordsFromFile(filename);

        // Check each password
        for (String password : passwords) {
            System.out.println("Testing password: " + password);
            boolean isStrong = isStrongPassword(password, chainingTable, probingTable);
            System.out.println("Strong: " + isStrong);

            // Display the number of comparisons for both hashCode implementations
            int chainingComparisonsOld = chainingTable.search(password, false);
            int chainingComparisonsNew = chainingTable.search(password, true);
            int probingComparisonsOld = probingTable.search(password, false);
            int probingComparisonsNew = probingTable.search(password, true);

            System.out.println("Chaining Comparisons (Old hashCode): " + chainingComparisonsOld);
            System.out.println("Chaining Comparisons (New hashCode): " + chainingComparisonsNew);
            System.out.println("Probing Comparisons (Old hashCode): " + probingComparisonsOld);
            System.out.println("Probing Comparisons (New hashCode): " + probingComparisonsNew);
            System.out.println();
        }
    }

    private static List<String> readPasswordsFromFile(String filename) {
        List<String> passwords = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                passwords.add(line.trim()); // Add password from each line
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return passwords;
    }

    private static boolean isStrongPassword(String password, HashTableChaining chainingTable, HashTableProbing probingTable) {
        if (password.length() < 8) return false;

        // Check against dictionary
        for (String word : DICTIONARY) {
            if (password.equals(word)) return false;
            if (password.startsWith(word) && password.substring(word.length()).matches("\\d")) return false;
        }

        return true;
    }
}

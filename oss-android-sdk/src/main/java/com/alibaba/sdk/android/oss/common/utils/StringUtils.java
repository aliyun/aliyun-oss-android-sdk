package com.alibaba.sdk.android.oss.common.utils;

import static com.alibaba.sdk.android.oss.common.utils.ServiceConstants.DEFAULT_ENCODING;

import java.nio.charset.Charset;
import java.util.Collection;

public class StringUtils {

    public static final Charset UTF8 = Charset.forName(DEFAULT_ENCODING);

    /**
     * @return true if the given value is either null or the empty string
     */
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    /**
     * A null-safe trim method. If the input string is null, returns null;
     * otherwise returns a trimmed version of the input.
     */
    public static String trim(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    /**
     * Joins the strings in parts with joiner between each string
     * @param joiner the string to insert between the strings in parts
     * @param parts the parts to join
     */
    public static String join(String joiner, String... parts) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            builder.append(parts[i]);
            if (i < parts.length - 1) {
                builder.append(joiner);
            }
        }
        return builder.toString();
    }

    /**
     * Joins the strings in collection with joiner between each string
     * @param joiner the string to insert between the strings in collection
     * @param collection the collection to join
     */
    public static String join(String joiner, Collection<String> collection) {
        StringBuilder builder = new StringBuilder();
        int i = 0;

        for (String part : collection) {
            builder.append(part);
            if (i < collection.size() - 1) {
                builder.append(joiner);
            }
            i++;
        }
        return builder.toString();
    }

    public static String replaceEach(final String text, final String[] searchList, final String[] replacementList) {
        int searchLength = searchList.length;
        int replacementLength = replacementList.length;

        if (isNullOrEmpty(text) || (searchLength == 0 && replacementLength == 0)) {
            return text;
        }

        // make sure lengths are ok, these need to be equal
        if (searchLength != replacementLength) {
            throw new IllegalArgumentException("Search and Replace array lengths don't match: "
                    + searchLength
                    + " vs "
                    + replacementLength);
        }

        // keep track of which still have matches
        boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];

        // index on index that the match was found
        int textIndex = -1;
        int replaceIndex = -1;
        int tempIndex = -1;

        // index of replace array that will replace the search string found
        // NOTE: logic duplicated below START
        for (int i = 0; i < searchLength; i++) {
            if (noMoreMatchesForReplIndex[i] || isNullOrEmpty(searchList[i]) || replacementList[i] == null) {
                continue;
            }
            tempIndex = text.indexOf(searchList[i]);

            // see if we need to keep searching for this
            if (tempIndex == -1) {
                noMoreMatchesForReplIndex[i] = true;
            } else {
                if (textIndex == -1 || tempIndex < textIndex) {
                    textIndex = tempIndex;
                    replaceIndex = i;
                }
            }
        }
        // NOTE: logic mostly below END

        // no search strings found, we are done
        if (textIndex == -1) {
            return text;
        }

        int start = 0;

        // get a good guess on the size of the result buffer so it doesn't have to double if it goes over a bit
        int increase = 0;

        // count the replacement text elements that are larger than their corresponding text being replaced
        for (int i = 0; i < searchList.length; i++) {
            if (searchList[i] == null || replacementList[i] == null) {
                continue;
            }
            int greater = replacementList[i].length() - searchList[i].length();
            if (greater > 0) {
                increase += 3 * greater; // assume 3 matches
            }
        }
        // have upper-bound at 20% increase, then let Java take over
        increase = Math.min(increase, text.length() / 5);

        StringBuilder buf = new StringBuilder(text.length() + increase);

        while (textIndex != -1) {

            for (int i = start; i < textIndex; i++) {
                buf.append(text.charAt(i));
            }
            buf.append(replacementList[replaceIndex]);

            start = textIndex + searchList[replaceIndex].length();

            textIndex = -1;
            replaceIndex = -1;
            tempIndex = -1;
            // find the next earliest match
            // NOTE: logic mostly duplicated above START
            for (int i = 0; i < searchLength; i++) {
                if (noMoreMatchesForReplIndex[i] || searchList[i] == null ||
                        searchList[i].isEmpty() || replacementList[i] == null) {
                    continue;
                }
                tempIndex = text.indexOf(searchList[i], start);

                // see if we need to keep searching for this
                if (tempIndex == -1) {
                    noMoreMatchesForReplIndex[i] = true;
                } else {
                    if (textIndex == -1 || tempIndex < textIndex) {
                        textIndex = tempIndex;
                        replaceIndex = i;
                    }
                }
            }
            // NOTE: logic duplicated above END

        }

        int textLength = text.length();
        for (int i = start; i < textLength; i++) {
            buf.append(text.charAt(i));
        }
        return buf.toString();
    }
}

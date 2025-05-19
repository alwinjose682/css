package io.alw.datagen.tokengenerator;

import java.util.Arrays;

public final class AlphaNumericTokenGenerator implements TokenGenerator<char[]> {
    private static final char[] constCharArr;
    private static final int constCharArr_largestNumIdx;
    private static final int constCharArr_smallestAlphabetIdx;
    private static final char constCharArr_lastElem;
    private final char[] tokenArr;
    private final int[] tokenArrSideTable; //tokenArrSideTable[i] carries the index of the constCharArr to which tokenArr[i] elem belongs
    private int tokenArrPointer;
    private int tokenArrPointer2;
    private int tokenArrLastElemPointer;
    //    private String tokenPartLeft;
//    private String tokenPartRight;
    private boolean addedNewElemToTokenArr;

    static {
        int idx = 0;
        constCharArr = new char[('9' - '0') + ('z' - 'a') + 2];
        constCharArr_lastElem = 'z';
        for (char ch = '0'; ch <= '9'; ++ch) {
            constCharArr[idx++] = ch;
        }
        constCharArr_largestNumIdx = idx - 1;
        constCharArr_smallestAlphabetIdx = idx;
        for (char ch = 'a'; ch <= 'z'; ++ch) {
            constCharArr[idx++] = ch;
        }
    }

    public AlphaNumericTokenGenerator(int maxLength) {
        this(2, maxLength, null);
    }

    /// minLength must be greater than 1
    public AlphaNumericTokenGenerator(int minLength, int maxLength) {
        this(minLength, maxLength, null);
    }

    /// if startWithChars is null, then defaults is applied
    public AlphaNumericTokenGenerator(int maxLength, char[] startWithChars) {
        this(2, maxLength, startWithChars);
    }

    private AlphaNumericTokenGenerator(int minLength, int maxLength, char[] startWithChars) {
        if (minLength < 2 || maxLength < minLength) {
            throw new RuntimeException("'minLength' must be at least 2 and 'maxLength' should not be less than 'minLength'");
        } else if (startWithChars != null && maxLength < startWithChars.length) {
            throw new RuntimeException("'maxLength' should not be less than the length of 'startWithChars[]'");
        }
        this.tokenArr = new char[maxLength];
        this.tokenArrSideTable = new int[maxLength];
        this.tokenArrPointer = this.tokenArrPointer2 = this.tokenArrLastElemPointer = populateTokenArray(minLength, maxLength, startWithChars);
        this.addedNewElemToTokenArr = false;
    }

    private void nextToken() {
        if (isTokenArrPointer2AtLastChar()) {
            // tokenArrPointer2 is shifted to the left till the char on the left is not lastChar
            while (tokenArrPointer2 > tokenArrPointer && isTokenArrPointer2AtLastChar()) {
                --tokenArrPointer2;
            }
            if (tokenArrPointer2 <= tokenArrPointer) {
                // tokenArrPointer is shifted to left and the char is advanced
                // if tokenArrPointer cannot be shifted to left, a new elem is added to tokenArr, tokenArr is reset and tokenArrPointer and tokenArrPointer2 are also reset
                // In both of the above cases tokenArrPointer2 is shifted to the right to the last elem of the array
                // while shifting tokenArrPointer to the left, if the char at left is already the lastChar, then tokenArrPointer is shifted left further
                while (tokenArrPointer >= 0 && isTokenArrPointer1AtLastChar()) {
                    --tokenArrPointer;
                }
                if (tokenArrPointer >= 0) {
                    //advance the char at tokenArr[tokenArrPointer] and invoke shiftTokenArrPointer2ToLastElem()
                    advanceTheCharAt(tokenArrPointer);
                    shiftTokenArrPointer2AndResetTokenArrPosToTheRightOf(tokenArrPointer);
                } else {
                    // If all positions in tokenArr are rotated, add a new elem to tokenArr if tokenArrCurrMaxIdx+1 < tokenArr.length and reset the tokenArr elems
                    if (++tokenArrLastElemPointer < tokenArr.length) {
                        populateTokenArray(tokenArrLastElemPointer + 1, tokenArrLastElemPointer + 1, new char[]{constCharArr[constCharArr_smallestAlphabetIdx]});
                    } else {
                        throw new RuntimeException("AlphanumericTokenGenerator reached maximum tokens it can provide");
                    }
                }
            } else {
                // case when tokenArrPointer2 > tokenArrPointer and tokenArr[tokenArrPointer2] is not the lastChar
                // Note: tokenArrPointer2 is already shifted to the left at this point
                // advance the char at tokenArr[tokenArrPointer2] and invoke shiftTokenArrPointer2ToLastElem()
                advanceTheCharAt(tokenArrPointer2);
                shiftTokenArrPointer2AndResetTokenArrPosToTheRightOf(tokenArrPointer2);
            }
        } else {
            // case when tokenArr[tokenArrPointer2] is not the lastChar
            // Note: tokenArrPointer2 is NOT shifted in this case
            // advance the char at tokenArr[tokenArrPointer2]
            advanceTheCharAt(tokenArrPointer2);
        }
    }

    // assigns the position in tokenArr identified by idx to the next char in constCharArr
    // updates the tokenArrSideTable also to keep it in sync with tokenArr
    private void advanceTheCharAt(final int idx) {
        tokenArrSideTable[idx] = tokenArrSideTable[idx] + 1;
        tokenArr[idx] = constCharArr[tokenArrSideTable[idx]];
    }

    // Shift tokenArrPointer2 to the last elem of tokenArr
    // Populates lowest elem in all the positions to the right of provided idx
    // tokenArrSideTable is also updated to keep it in sync with tokenArr
    private void shiftTokenArrPointer2AndResetTokenArrPosToTheRightOf(int idx) {
        for (idx = idx + 1; idx <= tokenArrLastElemPointer; ++idx) {
            tokenArr[idx] = constCharArr[0];
            tokenArrSideTable[idx] = 0;
        }
        tokenArrPointer2 = tokenArrLastElemPointer;
    }

    private boolean isTokenArrPointer1AtLastChar() {
        return isLastChar(tokenArrSideTable[tokenArrPointer]);
    }

    private boolean isTokenArrPointer2AtLastChar() {
        return isLastChar(tokenArrSideTable[tokenArrPointer2]);
    }

    private boolean isLastChar(int idx) {
        return idx + 1 == constCharArr.length;

        /*
        isLastChar logic of old implementation to avoid duplicate token values

//        if (tokenArrPointer + 1 <= tokenArrLastElemPointer) {
//            char nextElemInConstCharArr = constCharArr[tokenArrSideTable[tokenArrPointer] + 1];
//            return nextElemInConstCharArr == tokenArr[tokenArrPointer + 1] // IMP: This condition is important, because it avoids duplicate tokens being generated. Duplicate tokens such as zzz,zyz,yyz etc
//                    || tokenArrSideTable[tokenArrPointer] + 1 == constCharArr.length;
//        } else {
//            return tokenArrSideTable[tokenArrPointer] + 1 == constCharArr.length;
//        }

         */
    }

//    private void reComputeTokenPartLeftAndRight() {
//        tokenPartLeft = getTokenPartLeft();
//        tokenPartRight = getTokenPartRight();
//    }

//    private String getTokenPartLeft() {
//        StringBuilder sb = new StringBuilder();
//        for (int idx = 0; idx < tokenArrPointer2; ++idx) {
//            sb.append(tokenArr[idx]);
//        }
//        return sb.toString(); // returns an empty string and not null if nothing is appended, which is what is needed
//    }
//
//    private String getTokenPartRight() {
//        StringBuilder sb = new StringBuilder();
//        for (int idx = tokenArrPointer2 + 1; idx <= tokenArrLastElemPointer; ++idx) {
//            sb.append(tokenArr[idx]);
//        }
//        return sb.toString(); // returns an empty string and not null if nothing is appended, which is what is needed
//    }

    /// Populates the tokArr and tokArrSideTable
    /// if startWithChars is null, populates the first char as constCharArr\[constCharArr_smallestAlphabetIdx\]
    /// all positions of tokArr till minLength are populated with the constCharArr[0]
    /// this.tokenArrPointer and this.tokenArrLastElemPointer are set as idx, which is the last position of tokArr that has elements. Note: tokArr.length is greater than this.tokenArrLastElemPointer, except for the last cycle
    private int populateTokenArray(int minLength, int maxLength, char[] startWithChars) {
        int idx = 0;

        if (startWithChars != null) {
            // fill the token array with the chars by which the token needs start
            // tokArrSideTable is also populated
            for (; idx < startWithChars.length; ++idx) {
                tokenArr[idx] = startWithChars[idx];
                tokenArrSideTable[idx] = findIdxOfCharInConstCharArr(startWithChars[idx]);
            }
        }
        // Add 'constCharArr[constCharArr_smallestNumIdx]' till 'minlength'
        // if no char is populated in 'tokArr', then populate the first char as constCharArr[constCharArr_smallestAlphabetIdx]
        // tokArrSideTable is also populated
        if ((minLength > 0 && idx < minLength) || (idx == 0 && 0 < maxLength)) {
            if (idx == 0) {
                tokenArr[idx] = constCharArr[constCharArr_smallestAlphabetIdx];
                tokenArrSideTable[idx++] = constCharArr_smallestAlphabetIdx;
            }

            while (idx < minLength) {
                tokenArr[idx] = constCharArr[0];
                tokenArrSideTable[idx++] = 0;
            }
        }

        if (idx == 0) {
            throw new RuntimeException("Incorrect size to generate token. The parameters provided to initialize this class are wrong");
        }

        --idx;
        this.tokenArrPointer = this.tokenArrPointer2 = this.tokenArrLastElemPointer = idx;
        return idx;
    }

    private int findIdxOfCharInConstCharArr(char ch) {
        for (int idx = 0; idx < constCharArr.length; idx++) {
            if (ch == constCharArr[idx]) {
                return idx;
            }
        }
        throw new RuntimeException("Provided char: " + ch + ", is not present in constCharArr[]");
    }

    @Override
    public char[] next() {
        nextToken();
        return current();
    }

    @Override
    public char[] current() {
        return Arrays.copyOfRange(tokenArr, 0, tokenArrLastElemPointer + 1);
    }

    @Override
    public String nextAsString() {
        return String.valueOf(next());
    }

    @Override
    public String currentAsString() {
        return String.valueOf(current());
    }
}

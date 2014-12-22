package org.mycompany.bankocr


class OcrFileReader {
	static final int ROWS_PER_ACCOUNT = 3
	static final int CHARS_PER_DIGIT = 3

	List<String> createAccountNumberReportFromFile(String file) {
		collectAccountNumbersFromFile(file).collect { acctNum ->
	 		def finding = accountNumberFinding(acctNum)
			finding ? "$acctNum $finding" : acctNum
		}
	}

	String accountNumberFinding(String accountNumber) {
		if (accountNumber.contains('?')) {
			'ILL'
		} else if (validateAccountNumber(accountNumber)) {
		 	''
		} else {
			'ERR'
		}
	}


	List<String> collectAccountNumbersFromFile(String file) {
		collectAccountNumbers([], new File(file).readLines())
	}

	/**
	*  Note we drop one extra line than we take because of the trailing blank line
	*  between the OCR bank account numbers.
	*/
	List<String> collectAccountNumbers(List<String> acc, List<String> lines) {
		lines ? collectAccountNumbers(acc << convertOcrDigitsToAccountNumber(lines.take(ROWS_PER_ACCOUNT)), 
																			 lines.drop(ROWS_PER_ACCOUNT +1))
              : acc
	}

	/**
	 * Returns collection of size 3, each element contains list of 9 three-char segments.
	 *
	 * .transpose() acts like a zipper, creating a list of lists based on the corresponding
	 * positions in each of the three collections.
	 *
	 * .join() will create one OCR Digit string from the 1st, 2nd and 3rd part of each OCR Digit
	 *  OcrDigit.ocrToDecimal(it)
	 *
 	 */
	String convertOcrDigitsToAccountNumber(List<String> lines) {
		collectSplitOcrDigitsLines([] ,lines)
			.transpose()
			*.join()
			.collect { OcrDigit.ocrToDecimal(it) }
			.join()
	}

	/**
	 * Returns a collection of 3 lists, each list have the 9 distinct parts from reading one of the
	 * three rows in a Bank OCR Account number.
	 *
	 * @param lines - List of raw OCR account number lines to process
	 * @return list of lists of distinct OCR digit segments
	 *
	 */
	List<List<String>> collectSplitOcrDigitsLines(List<String> lines) {
		collectSplitOcrDigitsLines([] ,lines)
	}

	/**
	 * Recursive helper for collectSplitOcrDigitsLines(),
	 * processing each of the three lines that makeup a Bank OCR Account Number.
	 *
	 * @param acc - accumulator for the collections of 3 character segments
	 * @param l - list of raw OCR account number lines to process
	 * @return list of lists of distinct OCR digit segments
	 */
	List<List<String>> collectSplitOcrDigitsLines(List<List<String>> acc, List<String> l) {
		l ? collectSplitOcrDigitsLines(acc << splitOctDigitLine([],l.first()), l.tail())
	      : acc
	}

	/** 
	 * Returns collection of size 9, each list element contains three character string
	 * representing part of an OCR Digit
	 * 
	 * @param s - the string containing one line from an OCR file
	 * @return the list of 9 OCR digit segments
	 */
	List<String> splitOcrDigitLine(String s) {
		splitOctDigitLine([], s)
	}

	/**
	 * Returns collection of size 9, each list element contains three character string
	 * representing part of an OCR Digit. Recursive works through the OCR Digit line.
	 *
	 * For example, if the first part of an unprocessed row OCR Number row is " _ |_|",
	 * 	this method will return [' _ ','|_|']
	 *
	 * @param acc - accumulator for each 3 character segment
	 * @param s - the string containing OCR Digit segments to be read
	 * @return the list of distinct OCR digit segments
	 */

	List<String> splitOctDigitLine(List<String> acc, String s) {
		s ? splitOctDigitLine(acc + s.take(CHARS_PER_DIGIT), s.drop(CHARS_PER_DIGIT))
          : acc 
	}


	/*
	  The forumla for computing a check sum to validate an account is the following:
		account number:  3  4  5  8  8  2  8  6  5
		position names:  d9 d8 d7 d6 d5 d4 d3 d2 d1

		checksum calculation:

		((1*d1) + (2*d2) + (3*d3) + ... + (9*d9)) mod 11 == 0
	*/
	boolean validateAccountNumber(String accountNumber) {
		computeCheckSum(accountNumber) == 0
	}
	
	int computeCheckSum(String accountNumber) {
		computeCheckSumHelper(0, 1, accountNumber.reverse()) % 11
	}

	int computeCheckSumHelper(int acc, int idx, String accountNumber) {
		accountNumber ? computeCheckSumHelper(acc + (idx * accountNumber.take(1).toInteger()), idx +1, accountNumber.drop(1))
			          : acc
	}
}

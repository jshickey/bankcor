package org.mycompany.bankocr

/**
 * This class is a utility for reading file of OCR bank accounts and
 * converting them into a file of actual account numbers. The numbers
 * will be validated with checksum. If the validation fails, the account
 * will be followed by ERR. If the program is unable to recognize the
 * OCR digits, the digit will be replaced with a ? and marked with ILL.
 *
*/

class OcrFileReader {
	static final int ROWS_PER_ACCOUNT = 3
	static final int CHARS_PER_DIGIT = 3

	/**
	 * This method is main public entry point into this class. It will
	 * process a file and return a report.
	 *
	 * @param file - contains the OCR version of the bank account numbers
	 * @return a collection of account numbers with checksum/legibility indicators
	 */
	List<String> createAccountNumberReportFromFile(String file) {
		collectAccountNumbersFromFile(file).collect { acctNum ->
	 		def finding = accountNumberFinding(acctNum)
			finding ? "$acctNum $finding" : acctNum
		}
	}

	/**
	 * Determine if an account number is valid
	 *
	 * @param accountNumber - actual account number
	 * @return string indicating checksum or legibility error
	 */
	String accountNumberFinding(String accountNumber) {
		if (accountNumber.contains('?')) {
			'ILL'
		} else if (validateAccountNumber(accountNumber)) {
		 	''
		} else {
			'ERR'
		}
	}

	/**
	 * Top level method for reading a file of OCR account numbers
	 * and returning a list of actual account numbers.
	 *
	 * @param file contains OCR account numbers
	 * @return list of actual account numbers
	 */
	List<String> collectAccountNumbersFromFile(String file) {
		collectAccountNumbers([], new File(file).readLines())
	}

	/**
	 * This method is responsible for grabbing a collection of lines that make up one OCR
	 * bank account number and calling a method to convert those lines into
	 * an actual bank account number.
	 *
	 * Note: we drop one extra line than we take because of the trailing blank line
	 *  between the OCR bank account numbers.
	 *
	 * @param acc
	 * @param lines
	 * @return
	 */
	List<String> collectAccountNumbers(List<String> acc, List<String> lines) {
		lines ? collectAccountNumbers(acc << convertOcrDigitsToAccountNumber(lines.take(ROWS_PER_ACCOUNT)), 
																			 lines.drop(ROWS_PER_ACCOUNT +1))
              : acc
	}

	/**
	 * This method is responsible for orchestrating the conversion of one OCR account number
	 * into an actual account number.
	 *
	 * First, it calls a method returns a list of lists which contain 9 three-char segments.
	 *
	 * .transpose() acts like a zipper, creating a new list of lists that aggregates the corresponding
	 * positions in each of the three collections so that the three parts of one OCR digit
	 * are grouped together.
	 *
	 * .join() will create one OCR Digit string from the 1st, 2nd and 3rd part of each OCR Digit
	 *
	 *  OcrDigit.ocrToDecimal() will take the complete OCR string for one digit and return an actual number.
	 *
 	 */
	String convertOcrDigitsToAccountNumber(List<String> lines) {
		collectSplitOcrDigitsLines(lines)
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
	 * Recursive helper for collectSplitOcrDigitsLines()
	 *
	 * @param acc - accumulator for the collections of 3 character segments
	 * @param l - list of raw OCR account number lines to process
	 * @return list of lists of distinct OCR digit segments
	 */
	List<List<String>> collectSplitOcrDigitsLines(List<List<String>> acc, List<String> l) {
		l ? collectSplitOcrDigitsLines(acc << splitOcrDigitLine(l.first()), l.tail())
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
		splitOcrDigitLine([], s)
	}

	/**
	 * Recursive helper for splitOcrDigitLine()
	 *
	 * For example, if the first part of an unprocessed row OCR Number row is " _ |_|",
	 * 	this method will return [' _ ','|_|',etc...]
	 *
	 * @param acc - accumulator for each 3 character segment
	 * @param s - the string containing OCR Digit segments to be read
	 * @return the list of distinct OCR digit segments
	 */

	List<String> splitOcrDigitLine(List<String> acc, String s) {
		s ? splitOcrDigitLine(acc + s.take(CHARS_PER_DIGIT), s.drop(CHARS_PER_DIGIT))
          : acc 
	}


	/*
	  The formula for computing a check sum to validate an account is the following:
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

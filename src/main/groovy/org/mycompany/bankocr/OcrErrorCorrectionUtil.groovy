package org.mycompany.bankocr

// TODO: '?' probably should this secret value for unknown char everywhere

class OcrErrorCorrectionUtil {
	List<Character> ocrChars = [' ', '|', '_']

	// TODO: Refactor valid stuff from OcrFileReader into this class, rename this class OcrValidationUtil
	OcrFileReader ocrFileReader = new OcrFileReader()
	
	/**
	 * collect all possibilities at each position, filtering out the nulls or empty strings
	 * @param invalidAccountNumber
	 * @return
	 */
	List<String> findAlternateValidNumber(String invalidAccountNumber) {
		(0..invalidAccountNumber.size() -1).iterator().collect {int index ->
			findAlternateValidNumberAtIndex(index, invalidAccountNumber)
		}.findAll{it}
	}
	
	/**
	 * Try to find all possible alternatives to an account number for a given
	 * and see if one of them passes a checksum. This assume only one value
	 * in a given position of an account number would create a valid checksum.
	 * 
	 * @param index
	 * @param invalidAccountNumber
	 * @return
	 */
	String findAlternateValidNumberAtIndex(int index, String invalidAccountNumber) {
		collectUsingAlternateCharacters(OcrDigit.decimalToOcr(invalidAccountNumber[index]))
			.findAll{it != '?'}
			.collect { replaceChar(index, it, invalidAccountNumber)}
			.find {ocrFileReader.validateAccountNumber(it)}
	}
	
	/**
	 * 
	 * @param illegibleNumber
	 * @return
	 */
	def collectUsingAlternateCharacters(String illegibleNumber) {
		(0..illegibleNumber.size() -1).iterator().collectMany { index ->
			collectUsingAlternateCharactersAtIndex(index, illegibleNumber)
		}
	}

	/**
	 * 
	 * @param index
	 * @param illegibleNumber
	 * @return
	 */
	def collectUsingAlternateCharactersAtIndex(int index, String illegibleNumber) {
		collectUsingAlternateCharactersAtIndexHelper(index, illegibleNumber, ocrChars - illegibleNumber[index], [])
	}

	/**
	 * Substitute alternate characters at a given index in the OCR Digit and collect the results of trying to convert to a number
	 * 
	 * @param index - position in the OCR String to substitute a new character
	 * @param illegibleNumber - the original illegible OCR Digit
	 * @param altCharList - the alternate characters to try (i.g. if char at index is a pipe, try underscore and space)
	 * @param acc - accumulate the results of trying to convert with alternate characters
	 * @return final value of the accumulator
	 */
	def collectUsingAlternateCharactersAtIndexHelper(int index, String illegibleNumber, List<Character> altCharList, List<String> acc) {
		altCharList ? collectUsingAlternateCharactersAtIndexHelper(index, illegibleNumber, altCharList.tail(),
			                                                  acc + replaceCharAndConvertToNumber(index, altCharList.first() as Character, illegibleNumber))
		            : acc
	}

	def replaceCharAndConvertToNumber(int index, Character c, String illegibleNumber) {
		def s = illegibleNumber.getChars()
      	s[index] = c
      	OcrDigit.ocrToDecimal( s as String )
	}
	
	String replaceChar(int index, String c, String illegibleNumber) {
		def s = illegibleNumber.getChars()
		s[index] = c[0]
		return s as String
	}
}
package org.mycompany.bankocr

enum OcrDigit {
	
	ZERO('0', ' _ ' +
	          '| |' +
	          '|_|'),
		
	ONE('1', '   ' +
	         '  |' +
	         '  |'),
		
	TWO('2', ' _ ' +
	         ' _|' +
	         '|_ '),
		 
	THREE('3',' _ ' +
			  ' _|' +
			  ' _|'),
		  
	FOUR('4','   ' +
		     '|_|' +
			 '  |'),
		 
    FIVE('5',' _ ' +
		     '|_ ' +
			 ' _|'),
		 
	SIX('6', ' _ ' +
		     '|_ ' +
			 '|_|'),
	
	SEVEN('7', ' _ ' +
		       '  |' +
			   '  |'),
		   
	EIGHT('8', ' _ ' +
		       '|_|' + 
			   '|_|'),
		
	NINE('9', ' _ ' +
		      '|_|' +
			  ' _|')
		
	String decimalValue
	String value
	
	OcrDigit(String decimalValue, String value) {
		this.value = value
		this.decimalValue = decimalValue
	}
	
	/**
	 * Given an OCR Digit, return a string that contains decimal value.
	 * If the string doesn't match any of the OCR numbers, return the question mark character.
	 * 
	 * @param s - the 9 character string representing an OCR Digit
	 * @return a string containing the decimal value of the OCR Digit
	 */
	static String ocrToDecimal( String s ) {
		values().find { it.value == s }?.decimalValue ?: '?'
	}
	
	static OcrDigit findByOcrValue( String s ) {
		values().find { it.value == s }
	}
}

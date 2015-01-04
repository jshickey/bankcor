package org.mycompany.bankocr;

import org.mycompany.bankocr.OcrDigit;

import spock.lang.*


public class OcrDigitTest extends Specification {
	
	void 'Find OCR String given a number'() {
		given:
		String num = '3'
		
		when:
		def ocrString = OcrDigit.decimalToOcr(num)
		
		then:
		assert ' _ ' +
			   ' _|' +
			   ' _|' == ocrString
	}
	
	void 'Find OCR Digit given a number'() {
		given:
		String num = '3'
		
		when:
		def ocrNumber = OcrDigit.findByDecimalNumber(num)
		
		then:
		assert OcrDigit.THREE ==  ocrNumber
	}
	
	void 'Convert OCR Number to a Decimal Number'() {
		given: 'an Bank OCR String for the number zero'
		def ocrString = ' _ ' +
	                    '| |' +
	                    '|_|'
						
		when: 'invoking the static ocrToDecimal() method '
		def decimalValue = OcrDigit.ocrToDecimal(ocrString)
		
		then: 'return the ZERO enum'
		assert  OcrDigit.ZERO.decimalValue == decimalValue
	}

	void 'Convert unrecognizable OCR Number to a Question Mark'() {
		given: 'an unrecognizable Bank OCR string'
		def ocrString = '|_ ' +
						'| |' +
						'|_|'
						
		when: 'invoking the static ocrToDecimal() method '
		def decimalValue = OcrDigit.ocrToDecimal(ocrString)
		
		then: 'return the question mark'
		assert  '?' == decimalValue
	}

	void 'Find OCR Digit Enum from a string of nine characters'() {
		given: 'an Bank OCR String for the number zero'
		def ocrString = ' _ ' +
	                    '| |' +
	                    '|_|'
						
		when: 'invoking the static findByOcrValue() method '
		def e = OcrDigit.findByOcrValue(ocrString)
		
		then: 'return the question mark'
		assert  OcrDigit.ZERO == e
	}

	void 'Return null trying to find OCR Digit Enum from a string that does not match any OCR number'() {
		given: 'an invalid Bank OCR String for the number zero'
		def ocrString = '|_ ' +
						'| |' +
						'|_|'
						
		when: 'invoking the static findByOcrValue() method '
		def e = OcrDigit.findByOcrValue(ocrString)
		
		then: 'return the question mark'
		assert null == e
	}

}

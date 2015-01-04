package org.mycompany.bankocr;

import org.mycompany.bankocr.OcrDigit;

import spock.lang.*

class OcrErrorCorrectionUtilTest extends Specification {
	
	void "Test converting account number 888888888 to something that is valid"() {
		given:
		def invalidAccountNumber = '888888888'
		
		when:
		def result = errUtil.findAlternateValidNumber(invalidAccountNumber)
		
	   then:
	   assert 3 == result.size()
	   assert result.contains('888886888')
	   assert result.contains('888888880')
	   assert result.contains('888888988')
	}
	
	void "Test converting account number 777777777 to something that is valid"() {
		given:
		def invalidAccountNumber = '777777777'
		
		when:
		def result = errUtil.findAlternateValidNumber(invalidAccountNumber)
		
	   then:
	   assert '777777177' == result[0]

	}
	
	// class under test
	OcrErrorCorrectionUtil errUtil = new OcrErrorCorrectionUtil()

	void "Test converting simple account number to something that is valid"() {
		given:
		def invalidAccountNumber = '111111111'
		
		when:
		def result = errUtil.findAlternateValidNumber(invalidAccountNumber)
		
	   then:
	   assert '711111111' == result[0]

	}

	
	
	void "Test converting first digit to something that is valid"() {
		given:
		int index = 0
		def invalidAccountNumber = '111111111'
		
		when:
		def result = errUtil.findAlternateValidNumberAtIndex( index, invalidAccountNumber)
		
	   then:
	   assert '711111111' == result 

	}
	
	void "Test collecting alternative integers at all indices in an OCR number"() {
		given: " an illegible number for ONE "
		def illegibleNumber = '   ' +
	         	    		  '   ' +
	                          '  |'

        when: "convert OCR Digit to number"
		def numbers = errUtil.collectUsingAlternateCharacters(illegibleNumber)

        then: "should get back the number one"
        assert 18 == numbers.size()
		assert 2 == (numbers as Set).size()
		assert numbers.contains('1')
		assert numbers.contains('?')
	}


	void "Test Alternate Characters At Index"() {
		given:
		def illegibleNumber = '   ' +
     			    		  '   ' +
                		      '  |'
		def index = 0

		when:
		def numbers = errUtil.collectUsingAlternateCharactersAtIndex(index, illegibleNumber) 

		then:
		assert numbers[0] == '?'
		assert numbers[1] == '?'
	}

	void "Test changing alternate characters with missing space"() {
		given: "removing a char for OCR Number 1"
		def illegibleNumber = '__ ' +
	                    	  '| |' +
	                    	  '|_|'
		assert '?' == OcrDigit.ocrToDecimal(illegibleNumber)

        when: "adding a valid char back OCR Digit to number"
        int index = 0
        def numbers = errUtil.collectUsingAlternateCharactersAtIndex(index, illegibleNumber)
        
        then: "should get back the number one"
        assert numbers.contains('0')
		assert numbers.contains('?')
	}

	void "Test changing alternate characters with missing underscore"() {
		given: "removing a char for OCR Number 1"
		def illegibleNumber = '   ' +
	                    	  '| |' +
	                    	  '|_|'
		assert '?' == OcrDigit.ocrToDecimal(illegibleNumber)

        when: "adding a valid char back OCR Digit to number"
        int index = 1
        def numbers = errUtil.collectUsingAlternateCharactersAtIndex(index, illegibleNumber)
        
        then: "should get back the number one"
        assert numbers.contains('0')
		assert numbers.contains('?')
	}

	void "Test changing alternate character with missing pipe"() {
		given: "removing a char for OCR Number 1"
		String illegibleNumber = '   ' +
	            		 		 '   ' +
	                    	     '  |'
		assert '?' == OcrDigit.ocrToDecimal(illegibleNumber)

        when: "adding a valid char back OCR Digit to number"
        int index = 5
        def numbers = errUtil.collectUsingAlternateCharactersAtIndex(index, illegibleNumber)
        
        then: "should get back the number one"
        assert numbers.contains('1')
		assert numbers.contains('?')
	}

	void "Test changing one character in OCR Digit Number ONE finds a one"() {
		given: "removing a char for OCR Number 1"
		def illegibleNumber = '   ' +
	         	    		  '   ' +
	                          '  |'
		assert '?' == OcrDigit.ocrToDecimal(illegibleNumber)

        when: "adding a valid char back OCR Digit to number"
        int index = 5
        Character c = '|'
        def number = errUtil.replaceCharAndConvertToNumber(index, c, illegibleNumber)
        
        then: "should get back the number one"
        assert '1' == number
	}

	void "Test changing one character in OCR Digit Number ONE fails"() {
		given: " an illegible number for ONE "
		def illegibleNumber = '   ' +
	         	    		  '   ' +
	                          '  |'

        when: "convert OCR Digit to number"
        int index = 1
        Character c = '|'
        def number = errUtil.replaceCharAndConvertToNumber(index, c, illegibleNumber)
        
        then: "should get back the number one"
        assert '?' == number
	}


}
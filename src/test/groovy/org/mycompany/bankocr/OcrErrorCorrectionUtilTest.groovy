package org.mycompany.bankocr;

import org.mycompany.bankocr.OcrDigit;

import spock.lang.*

class OcrErrorCorrectionUtilTest extends Specification {
	// class under test
	OcrErrorCorrectionUtil errUtil = new OcrErrorCorrectionUtil()
	OcrFileReader fileReader = new OcrFileReader()
	
	@Unroll
	void "Test converting invalid account number, #invalidAccountNumber,  to something that is ambiguous, #validAccountNumbers"() {
		expect:
		def result = errUtil.findAlternateValidNumber(invalidAccountNumber)
	    assert validAccountNumbers.size() == result.size()
		result.each { assert validAccountNumbers.contains(it) }
		
	    where:
	    invalidAccountNumber | validAccountNumbers
	    '888888888'          | ['888886888','888888880','888888988']
		'555555555'          | ['555655555', '559555555']
		'666666666'          | ['666566666', '686666666']
		'999999999'          | ['899999999', '993999999', '999959999']
		'490067715'          | ['490067115', '490067719', '490867715']
	}
	
	@Unroll
	void "Test converting invalid account number, #invalidAccountNumber, to something that is valid, #validAccountNumber"() {
		
		expect:
		def result = errUtil.findAlternateValidNumber(invalidAccountNumber)
		assert validAccountNumber == result[0]
		
		where: 'The test account number is #accountNumber'
		invalidAccountNumber | validAccountNumber
		'111111111'          | '711111111'
		'777777777'          | '777777177'
		'200000000'          | '200800000'
		'333333333'          | '333393333'
	}
	

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
	
	void "Test collecting alternative integers for all Digits in an OCR number"() {
		given: " an illegilbe account number"
		def illegibleOcrNumber =["    _  _     _  _  _  _  _ ",
                                 " _| _| _||_||_ |_   ||_||_|",
							     "  ||_  _|  | _||_|  ||_| _|"]
                           
		when: "convert OCR Digit to number"
		def ocrDigitList = fileReader.collectSplitOcrDigitsLines(illegibleOcrNumber)
							.transpose()
							*.join()
		def accountNumber = ocrDigitList.collect { OcrDigit.ocrToDecimal(it) }.join()
		def illegibleDigit = ocrDigitList[accountNumber.indexOf('?')]
		def numbers = errUtil.collectUsingAlternateCharacters(illegibleDigit).findAll{ !it.contains('?') }
		def possibleAccountNumbers = numbers.collect { number -> accountNumber.replace('?', number) }
		
		then: "should get back the number one"
		assert 2 == possibleAccountNumbers.size()
		assert possibleAccountNumbers.contains('123456789')
		assert possibleAccountNumbers.contains('423456789')
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
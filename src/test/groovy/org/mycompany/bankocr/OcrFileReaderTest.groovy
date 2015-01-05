package org.mycompany.bankocr

import spock.lang.*


class OcrFileReaderTest extends Specification {
	// class under test
	def ocrAcctReader = new OcrFileReader()
	void "Test with 500 accounts - crate report with findings(valid,error,illegible) from five-hundred-accounts.txt"() {
		given: 'A test file with 500 accounts'
		def testFile = '/account-numbers-all-types.txt'

		when: 'Creating the final report with findings'
		def accountNumbers = ocrAcctReader.createAccountNumberReportFromFile(
			this.getClass().getResource('/five-hundred-accounts.txt').file as String)

		then: 'Final account list should have more than 500 entries'
		println "large report produced ${accountNumbers.size()} accounts with findings"
		assert 500 < accountNumbers.size()
		assert '711111111'     == accountNumbers[0]
		assert '000000051'     == accountNumbers[1]
		assert '49006771? ILL' == accountNumbers[2]
		assert '1234?678? ILL' == accountNumbers[3]
		assert '888888888 ERR' == accountNumbers[4]
	}


	void "Test creating an account number report with findings(valid,error,illegible) from account-numbers-all-types.txt"() {
		def testFile = '/account-numbers-all-types.txt'
		when:
		def accountNumbers = ocrAcctReader.createAccountNumberReportFromFile(
			this.getClass().getResource('/account-numbers-all-types.txt').file as String)

		then:
		assert 5 == accountNumbers.size()
		assert '711111111'     == accountNumbers[0]
		assert '000000051'     == accountNumbers[1]
		assert '49006771? ILL' == accountNumbers[2]
		assert '1234?678? ILL' == accountNumbers[3]
		assert '888888888 ERR' == accountNumbers[4]
	}

	void "Test valid checksum"() {
		given: 'A valid account number of 711111111'
		def accountNumber = '711111111'
		int expectedSum = (7 * 9) + 8 + 7 + 6 + 5 + 4 + 3 + 2 + 1

		when:'Computing the checksum'
		def checkSum = ocrAcctReader.computeCheckSum(accountNumber)

		then:'Expect checksum to be valid'
		assert expectedSum % 11 == checkSum

	}

    @Unroll
	void "Test verification using checksum for #accountNumber is #valid"() {
		expect: "validateAccountNumber() should return #valid"
		assert ocrAcctReader.validateAccountNumber(accountNumber) == valid

		where: 'The test account number is #accountNumber'
		accountNumber | valid
		'711111111'   | true
		'123456789'   | true
		'490867715'   | true
		'888888888'   | false
		'490067715'   | false
		'012345678'   | false
	}

	void "Test reading accounts from a file with one account"() {
		when:
		def accountNumbers = ocrAcctReader.collectAccountNumbersFromFile(
			this.getClass().getResource('/ocr-000000000.txt').file as String)

		then:
		assert 1 == accountNumbers.size()
		assert '000000000' == accountNumbers[0].getAt(0)
	}

	void "Test reading accounts from a file with many accounts"() {
		when:
		def accountNumbers = ocrAcctReader.collectAccountNumbersFromFile(
			this.getClass().getResource('/valid-account-numbers.txt').file as String)

		then:
		assert 5 == accountNumbers.size()
		assert '000000000' == accountNumbers[0].getAt(0)
		assert '111111111' == accountNumbers[1].getAt(0)
		assert '123456789' == accountNumbers[2].getAt(0)
		assert '222222222' == accountNumbers[3].getAt(0)
		assert '333333333' == accountNumbers[4].getAt(0)
	}


	
	@Unroll
	void "Convert OCR Digit version of #expectedAccountNumber"() {
		expect: "#expectedAccountNumber"
		List<String> lines = new File(this.getClass().getResource('/'+filename).file as String).readLines()
		def (acctNum,ocrDigit) = ocrAcctReader.convertOcrDigitsToAccountNumber(lines.take(3) as List<String>)
		assert expectedAccountNumber == acctNum

		where: 'The test file:#filename contains the OCR Digit version of #expectedAccountNumber'
		filename | expectedAccountNumber
		'ocr-000000000.txt' | '000000000'
		'ocr-111111111.txt' | '111111111'
		'ocr-222222222.txt' | '222222222'
		'ocr-333333333.txt' | '333333333'
		'ocr-444444444.txt' | '444444444'
		'ocr-555555555.txt' | '555555555'
		'ocr-666666666.txt' | '666666666'
		'ocr-777777777.txt' | '777777777'
		'ocr-888888888.txt' | '888888888'
		'ocr-999999999.txt' | '999999999'
		'ocr-123456789.txt' | '123456789'
	}



	void "Parse continuous line of OCR characters into collection of 9 distinct OCR segments "() {
		given: "String representing first line in a file with all OCR zeroes i.e. 9 copies of space-dash-space"
		String s = ' _  _  _  _  _  _  _  _  _ '
		
		when:  "Spliting the line into distinct OCR segments"
		def result = ocrAcctReader.splitOcrDigitLine(s)
		
		then:  "There should be a collection 9 space-dash-space Strings"
		assert 9 == result.size()
		assert result.every{ OcrFileReader.CHARS_PER_DIGIT == it.size() }
		assert result.every{ OcrDigit.ZERO.value[0..2] == it }
	}
}
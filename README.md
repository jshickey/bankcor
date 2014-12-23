This project can be built using Gradle Wrapper, which will download the correct version of gradle to build the project.

* git clone https://github.com/jshickey/bankcor.git
* cd bankcor

* run ./gradlew to build the project for mac and unix, or gradlew.bat on windows 

* run ./gradlew test to run the tests
 + JUnit report is in build/reports/tests/index.html
 + Spock test report is in build/spock-reports/index.html

* run ./gradlew jacocoTestReport to create the code coverage report
 + The Jacoco report is in build/jacocoHtml/index.html
 

Note: The Jacoco struggles with detecting execution branches in Groovy code.

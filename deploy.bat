echo off
start cmd /k mvn clean install
echo Wait for Maven to finish building before proceeding
pause
start cmd /k mvn exec:java -Dexec.mainClass="loanbroker.Aggregator"
start cmd /k mvn exec:java -Dexec.mainClass="loanbroker.GetBanks"
start cmd /k mvn exec:java -Dexec.mainClass="loanbroker.GetCreditScore"
start cmd /k mvn exec:java -Dexec.mainClass="loanbroker.Normalizer"
start cmd /k mvn exec:java -Dexec.mainClass="loanbroker.RecipientList"
exit
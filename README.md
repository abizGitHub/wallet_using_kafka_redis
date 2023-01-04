
### redis is used for distributed lock between multiple instances of application which are listening to different partitions of kafka-messages .



init-kafka

java -jar -Dserver.port=8080 -Dspring.kafka.consumer.partitions=0,1,2,3 wallet-1.jar
java -jar -Dserver.port=8081 -Dspring.kafka.consumer.partitions=4,5,6,7 wallet-1.jar
java -jar -Dserver.port=8082 -Dspring.kafka.consumer.partitions=8,9,10,11 wallet-1.jar

cat sampleData | http :8080/api/v1/wallet
for((i=0;i<1000;i++));do cat sampleData | http :8080/api/v1/wallet ; done;


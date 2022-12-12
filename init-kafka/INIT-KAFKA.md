
####START ZOOKEEPER
>bin/zookeeper-server-start.sh config/zookeeper.properties

####START KAFKA BROKER
>bin/kafka-server-start.sh config/server0.properties
bin/kafka-server-start.sh config/server1.properties
bin/kafka-server-start.sh config/server2.properties

####CREATE TOPIC
>bin/kafka-topics.sh \
--bootstrap-server  localhost:9092,localhost:9093,localhost:9094 \
--create \
--replication-factor 1 \
--partitions 10 \
--topic walletTopic

####LIST TOPICS
>bin/kafka-topics.sh \
--bootstrap-server localhost:9092 \
--list

####TOPIC DETAILS
>bin/kafka-topics.sh \
--bootstrap-server localhost:9092 \
--describe \
--topic walletTopic

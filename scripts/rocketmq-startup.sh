nohup sh /Library/ro/distribution/target/rocketmq-4.8.0/rocketmq-4.8.0/bin/mqnamesrv &

nohup sh /Library/ro/distribution/target/rocketmq-4.8.0/rocketmq-4.8.0/bin/mqbroker -n localhost:9876 autoCreateTopicEnable=true &
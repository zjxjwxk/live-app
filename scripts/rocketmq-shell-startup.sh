nohup sh /Library/rocketmq-all-4.8.0-source-release/distribution/target/rocketmq-4.8.0/rocketmq-4.8.0/bin/mqnamesrv &

nohup sh /Library/rocketmq-all-4.8.0-source-release/distribution/target/rocketmq-4.8.0/rocketmq-4.8.0/bin/mqbroker -n live.zjxjwxk.com:9876 autoCreateTopicEnable=true &
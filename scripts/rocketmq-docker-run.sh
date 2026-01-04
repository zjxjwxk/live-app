docker run -d \
  --name rmqnamesrv \
  -p 9876:9876 \
  --network rocketmq \
  apache/rocketmq:5.3.2 sh mqnamesrv

echo "brokerIP1=$HOST_IP" > broker.conf

docker run -d \
  --name rmqbroker \
  -p 10912:10912 -p 10911:10911 -p 10909:10909 \
  -p 8880:8080 -p 8881:8081 \
  --network rocketmq \
  -v "$(pwd)"/broker.conf:/home/rocketmq/rocketmq-5.3.2/conf/broker.conf \
  -e NAMESRV_ADDR=rmqnamesrv:9876 \
  -e JAVA_OPT_EXT="-server -Xms128m -Xmx128m -Xmn128m" \
  apache/rocketmq:5.3.2 sh mqbroker --enable-proxy \
  -c /home/rocketmq/rocketmq-5.3.2/conf/broker.conf

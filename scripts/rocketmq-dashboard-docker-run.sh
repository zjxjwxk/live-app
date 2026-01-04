docker run -d \
  --name rocketmq-dashboard \
  -p 8882:8082 \
  -e "JAVA_OPTS=-Drocketmq.namesrv.addr=$HOST_IP:9876" \
  -t apacherocketmq/rocketmq-dashboard:latest
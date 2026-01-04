docker run -d \
  --name live-user-provider \
  -v /tmp/logs/live-user-provider:/tmp/logs/live-user-provider \
  -p "$HOST_IP":9090:9090 \
  -e DUBBO_IP_TO_REGISTRY="$HOST_IP" \
  -e TZ=Asia/Shanghai \
  --add-host "live.zjxjwxk.com:$HOST_IP" \
  zjxjwxk/live-user-provider-docker:latest
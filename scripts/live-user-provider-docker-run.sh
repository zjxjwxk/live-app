docker run --name live-user-provider \
 -p "$HOST_IP":9090:9090 \
 -e DUBBO_IP_TO_REGISTRY="$HOST_IP" \
 --add-host "live.zjxjwxk.com:$HOST_IP" \
 -d live-user-provider-docker:latest
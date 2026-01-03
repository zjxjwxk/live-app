docker run --name live-user-provider-latest \
 -p 192.168.31.73:9090:9090 \
 -e DUBBO_IP_TO_REGISTRY=192.168.31.73 \
 --add-host 'live.zjxjwxk.com:192.168.31.73' \
 -d live-user-provider-docker:latest
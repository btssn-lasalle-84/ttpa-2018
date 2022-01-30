sudo chmod 777 /var/run/sdp
sudo sdptool add --channel=22 SP > /dev/null
sudo rfcomm watch /dev/rfcomm0 22 1>/dev/null 2>&1 &

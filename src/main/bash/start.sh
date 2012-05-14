#!/bin/bash
# IressMDS watchdog

cd /home/owner/IressMDS
        if grep -q -E 'shutting|Connection reset' log.log
        then ./mds.sh
        else touch PASS
        fi

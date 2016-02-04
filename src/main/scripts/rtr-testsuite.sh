#!/usr/bin/env bash
#
# The BSD License
#
# Copyright (c) 2010-2012 RIPE NCC
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#   - Redistributions of source code must retain the above copyright notice,
#     this list of conditions and the following disclaimer.
#   - Redistributions in binary form must reproduce the above copyright notice,
#     this list of conditions and the following disclaimer in the documentation
#     and/or other materials provided with the distribution.
#   - Neither the name of the RIPE NCC nor the names of its contributors may be
#     used to endorse or promote products derived from this software without
#     specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
#

EXECUTION_DIR=`dirname "$BASH_SOURCE"`
cd ${EXECUTION_DIR}

APP_NAME="rtr-testsuite"

function error_exit {
    echo -e "[ error ] $1"
    exit 1
}

function info {
    echo -e "[ info ] $1"
}

function warn {
    echo -e "[ warn ] $1"
}

function usage {
cat << EOF
Usage: $0 start  [-c /path/to/my-configuration.conf]
   or  $0 run    [-c /path/to/my-configuration.conf]
   or  $0 stop   [-c /path/to/my-configuration.conf]
   or  $0 status [-c /path/to/my-configuration.conf]
EOF
}


function check_java_version {
  JAVA_VERSION=`${JAVA_CMD} -version 2>&1 | grep version | sed 's/.* version //g'`
  MAJOR_VERSION=`echo ${JAVA_VERSION} | sed 's/"\([[:digit:]]\)\.\([[:digit:]]\).*"/\1\2/g'`
  if (( ${MAJOR_VERSION} < 17 )) ; then
    error_exit "rtr-testsuite requires Java 1.7 or greater, your version of java is ${JAVA_VERSION}";
  fi
}

#
# Specify the location of the Java home directory. If set then $JAVA_CMD will
# be defined to $JAVA_HOME/bin/java
#
if [ -d "${JAVA_HOME}"  ] ; then
    JAVA_CMD="${JAVA_HOME}/bin/java"
else
    warn "JAVA_HOME is not set, will try to find java on path."
    JAVA_CMD=`which java`
fi

if [ -z $JAVA_CMD ]; then
    error_exit "Cannot find java on path. Make sure java is installed and/or set JAVA_HOME"
fi

check_java_version

# See how we're called
RTR_PORT=8282
if (($# == 1)) ; then 
    RTR_PORT="$1"
    case $RTR_PORT in
        ''|*[!0-9]*) error_exit "Invalid port number";;
        *) RTR_PORT_OPT="-p ${RTR_PORT}";;
    esac
else
    RTR_PORT_OPT=""
fi
shift
if [[ -n $MODE ]]; then
   #usage
   exit
fi



info "Starting ${APP_NAME}..."
info "writing logs under log directory"
info "Routers can connect on port ${RTR_PORT}"

CMDLINE="${JAVA_CMD} ${JAVA_OPTS} \
         -Dapp.name=${APP_NAME} main.java.ScalaRunner ${RTR_PORT_OPT}"

${CMDLINE}
exit $?
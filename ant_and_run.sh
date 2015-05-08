# It will clean and the build the Code, so that its ready to be run
#
# 1. It will first move you to the same folder where this file is
# 2. "ant clean"
# 3. "ant"
# 4. "cd runtime/local"

cd /home/ravi/MTP2/apache-nutch-1.7-plain
ant clean
ant
cd runtime/local/.

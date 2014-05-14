#! /bin/sh

###########################################
# NeighborNote Startup script for OS-X
###########################################


eval $1

###########################################
# Location variables. Edit the variables #
# below to your specific installation. #
# The ones below are examples only. #
###########################################

NEIGHBORNOTE=$(cd `dirname $0` && cd ../../../ && pwd)
# NEIGHBORNOTE=/usr/share/neighbornote

########################################
# Memory settings. These can be tuned #
# to your specific needs. The greater #
# the memory allocated the better #
# your response may be, but the more #
# resources the program will consume. #
# Lower numbers may hurt performance #
# but will reduce resource held by #
# the program. If you get errors #
# that say "out of memory" you need #
# to increase these values. #
########################################
# Initial heap size
NN_XMS=256M
# Maximum heap size
NN_XMX=1024M

## The young generation
# the young generation will occupy 1/2 of total heap
NN_NEW_RATIO=1

## GC option
## recommend Incremental Low Pause GC for desktop apps 
NN_GC_OPT=-Xincgc
## recent multi-core CPU may show good performance
#NN_GC_OPT=-XX:+UseParNewGC
#NN_GC_OPT=-XX:+UseConcMarkSweepGC
## same as default
#NN_GC_OPT=-XX:+UseParallelGC

## debug
#NN_DEBUG=-agentlib:hprof=format=b
#NN_DEBUG=-agentlib:hprof=cpu=samples,format=a
#NN_DEBUG=-verbose:gc 

########################################
# This next variable is optional. It #
# is only needed if you want to run #
# multiple copies of NeighborNote under #
# the same Linux user id. Each #
# additional copy (after the first) #
# should have a unique name. This #
# permits the settings to be saved #
# properly. If you only want to run #
# one copy under a single userid, this #
# can be commented out. #
########################################
#NN_NAME="sandbox" 


#Do any parameter overrides
while [ -n "$*" ]
do
eval $1
shift
done


###################################################################
###################################################################
## You probably don't need to change anything below this line. ##
###################################################################
###################################################################


#####################
# Setup environment #
#####################
NN_CLASSPATH=$NEIGHBORNOTE/neighbornote.jar

NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/apache-mime4j-0.6.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/commons-codec-1.5.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/commons-compress-1.2.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/commons-lang3-3.0.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/commons-logging-1.1.1.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/evernote-api-1.25.0.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/h2-1.3.163.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/h2-lucene-ex-1.3.163.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/httpclient-4.1.1.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/httpcore-4.1.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/httpmime-4.1.1.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/jaxen-1.1.3.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/jazzy.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/jtidy-r938.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/libthrift.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/log4j-1.2.14.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNTOE/lib/lucene-analyzers-3.6.2.jar
NN_CLASSPATH=$NN_CLASSPATH;$NEIGHBORNOTE/lib/lucene-core-3.6.2.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/pdfbox-app-1.6.0.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/poi-3.7-20101029.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/poi-ooxml-3.7.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/poi-ooxml-schemas-3.7-20101029.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/poi-scratchpad-3.7-20101029.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/scribe-1.3.0.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/tika.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/xmlbeans-2.3.0.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/xsdlib-20060615.jar

NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/qtjambi-macosx-4.5.2_01.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEIGHBORNOTE/lib/qtjambi-macosx-gcc-4.5.2_01.jar

###################
# Run the program #
###################
cd $NEIGHBORNOTE

java -Xmx$NN_XMX -Xms$NN_XMS -XX:NewRatio=$NN_NEW_RATIO $NN_GC_OPT $NN_DEBUG -classpath $NN_CLASSPATH cx.fbn.nevernote.NeverNote --sync-only=$NN_SYNCONLY --home=$NN_HOME --name=$NN_NAME -XstartOnFirstThread -d32 -client

cd -


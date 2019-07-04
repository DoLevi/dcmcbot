APP_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"/..

cd $APP_DIR
pwd
java -jar -Djava.class.path=lib/* -Dlog4j.configurationFile=conf/log4j2.xml -Ddcmcbot.configurationFile=conf/dcmcbot-config.xml lib/dcmcbot-core-localbuild.jar
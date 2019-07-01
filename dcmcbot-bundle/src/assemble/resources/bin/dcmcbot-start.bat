@echo off
set APP_DIR=%~dp0..
cd %APP_DIR%
java -jar -Djava.class.path=lib/* -Dlog4j.configurationFile=conf/log4j2.xml -Ddcmcbot.configurationFile=conf\dcmcbot-config.xml lib\dcmcbot-core-localbuild.jar
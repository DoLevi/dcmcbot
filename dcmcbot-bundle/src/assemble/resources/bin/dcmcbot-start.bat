@echo off
set APP_DIR=%~dp0..
java -jar -DconfigurationFile=%APP_DIR%/conf/dcmcbot-config.xml -Djava.library.path=%APP_DIR%/lib %APP_DIR%/lib/dcmcbot-core-localbuild.jar
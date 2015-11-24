@echo off
set inputFile="../_default_files/BioServer/BioServerConfig.properties"

@echo on
java -jar dist\BioServer.jar %inputFile%

@echo off
pause
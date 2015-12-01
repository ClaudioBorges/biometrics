@echo off
set inputFile="../_default_files/BioServer/BioServerConfig.properties"

@echo on
java -cp dist\EncoderServer.jar bioserver.BioClient %inputFile%

@echo off
pause
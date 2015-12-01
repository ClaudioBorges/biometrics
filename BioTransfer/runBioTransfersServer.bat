@echo off
set inputFile="../_default_files/BioTransfer/BioTransfer.properties"

@echo on
java -cp dist\BioTransfer.jar biotransfer.server.BioTransferServer %inputFile%

@echo off
pause
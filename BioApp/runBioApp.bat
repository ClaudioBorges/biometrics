@echo off
set inputFile="../_default_files/BioApp/BioApp.properties"

@echo on
java -jar dist\BioApp.jar %inputFile%

@echo off
pause
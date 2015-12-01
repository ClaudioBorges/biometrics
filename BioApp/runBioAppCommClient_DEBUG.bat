@echo off
set inputFile="../_default_files/BioApp/BioApp.properties"

@echo on
java -cp dist\BioApp.jar bioapp.comm.BioAppCommClient %inputFile%

@echo off
pause
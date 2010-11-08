@setlocal
@echo off

REM Path to this script
set PWD=%~dp0

REM Paths to distributed files or source directories
set BXPATH=../lib/BaseX.jar
REM set BXPATH=%PWD%/../target/classes

REM Options for virtual machine
set VM=-Xmx1g

REM Run BaseX
java -cp "%BXPATH%" %VM% org.basex.BaseX %*

@endlocal
cd /D %~dp0
REM -Dsun.java2d.ddoffscreen=false
jdk\bin\java -Dsun.java2d.noddraw=true -Dsun.java2d.ddscale=true -jar quran.jar %*
pause
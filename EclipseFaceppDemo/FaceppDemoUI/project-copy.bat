@echo off
rename res res_path
rename src src_path

set /p res_path=<res_path
set /p src_path=<src_path

echo source code path:
echo %res_path%
echo %src_path%

echo remove old folders
del /Q/F res
del /Q/F src
echo ...

echo copy source code
xcopy "%res_path%" res\ /E/Y/K
xcopy "%src_path%" src\ /E/Y/K/

echo done...

pause
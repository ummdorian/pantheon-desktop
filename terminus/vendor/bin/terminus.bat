@ECHO OFF
setlocal DISABLEDELAYEDEXPANSION
SET BIN_TARGET=%~dp0/../pantheon-systems/terminus/bin/terminus
php "%BIN_TARGET%" %*

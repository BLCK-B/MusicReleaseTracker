; UI settings
!include "MUI2.nsh"
!define VERSION "8.1"
!define MUI_ABORTWARNING
!define MUI_ICON "MRTicon.ico"
!insertmacro MUI_PAGE_LICENSE "license.txt"
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES

Var JDKPath

Section "Uninstall"
    MessageBox MB_YESNO "Delete MRT AppData folder? This will delete its data." IDYES DeleteFolders IDNO SkipDeletion
	
    DeleteFolders:
        Delete "$SMPrograms\MusicReleaseTracker\MusicReleaseTracker.lnk"
        Delete "$INSTDIR\MusicReleaseTracker.exe"
        RMDir /r /REBOOTOK "$INSTDIR"
        RMDir /r /REBOOTOK "$APPDATA\MusicReleaseTracker"
        DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MusicReleaseTracker"
        goto End

    SkipDeletion:
        Delete "$SMPrograms\MusicReleaseTracker\MusicReleaseTracker.lnk"
        Delete "$INSTDIR\MusicReleaseTracker.exe"
		RMDir /r /REBOOTOK "$INSTDIR"
        DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MusicReleaseTracker"

    End:
SectionEnd

; Installer section
Section
    ; Uninstall previous version
	Delete "$SMPrograms\MusicReleaseTracker\MusicReleaseTracker.lnk"
	Delete "$INSTDIR\MusicReleaseTracker.exe"

    ; Create the installation directory and appdata folder (if not exist)
    SetOutPath "$INSTDIR"
	CreateDirectory "$APPDATA\MusicReleaseTracker"
    ; Install the new version and create Start Menu shortcut
    File "MusicReleaseTracker.exe"
    CreateDirectory "$SMPrograms\MusicReleaseTracker"
    CreateShortCut "$SMPrograms\MusicReleaseTracker\MusicReleaseTracker.lnk" "$INSTDIR\MusicReleaseTracker.exe"

    ; Create uninstaller
    WriteUninstaller "$INSTDIR\Uninstall.exe"
    
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MusicReleaseTracker" "DisplayName" "MusicReleaseTracker"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MusicReleaseTracker" "DisplayVersion" "${VERSION}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MusicReleaseTracker" "UninstallString" "$INSTDIR\Uninstall.exe"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MusicReleaseTracker" "Publisher" "BLCK"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MusicReleaseTracker" "DisplayIcon" "$INSTDIR\MusicReleaseTracker.exe,0"
SectionEnd

; Name in installer window
Name "MusicReleaseTracker installer"
; Name of output file
Outfile "MRT-${VERSION}-win.exe"
Icon "MRTicon.ico"
; Default installation directory
InstallDir $PROGRAMFILES\MusicReleaseTracker
ShowInstDetails hide

; Uninstaller details
UninstallCaption "Uninstall MusicReleaseTracker"
UninstallIcon "MRTicon.ico"
; Confirm uninstallation and progress
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
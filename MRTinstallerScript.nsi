; UI settings
!include "MUI2.nsh"
!define VERSION "8"
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
    ; Remove the old version if it's installed
    ReadRegStr $R0 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MusicReleaseTracker" "DisplayVersion"
    StrCmp $R0 "${VERSION}" NoUninstallOldVersion
    
    ; Uninstall old version
    ExecWait '"$INSTDIR\Uninstall.exe" /S' ; Silent uninstall
    
    NoUninstallOldVersion:

    ; Create the installation directory and appdata folder
    SetOutPath "$INSTDIR"
	CreateDirectory "$APPDATA\MusicReleaseTracker"

    ; Install the new version
    File "MusicReleaseTracker.exe"
    
    ; Create Start Menu shortcut
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

; Installer details
Name "MusicReleaseTracker"
Outfile "MRT-${VERSION}-win.exe"
Icon "MRTicon.ico"
InstallDir $PROGRAMFILES\MusicReleaseTracker
ShowInstDetails show

; Uninstaller details
UninstallCaption "Uninstall MusicReleaseTracker"
UninstallIcon "MRTicon.ico"

; This line is important to include for creating the uninstaller executable
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
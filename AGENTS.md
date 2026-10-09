# 🤖 LYWSD02 BLE Tool - Agent Build & Deployment Rules

이 프로젝트에서 작업하는 모든 AI 에이전트는 다음 빌드 및 배포 규칙을 반드시 준수해야 합니다.

## 📦 버전 관리 및 빌드 규칙 (Version & Build Rules)
1. **버전 자동 증가 규칙**:
   - 빌드를 수행할 때마다 `app/build.gradle.kts`의 `versionName`(`1.x.y`) 중 **`y`를 1씩 증가**시키고, `versionCode`를 1씩 올릴 것.
   - 예: `1.2.0` ➔ `1.2.1` ➔ `1.2.2`

2. **APK 배포 경로 자동 복사 규칙**:
   - 빌드가 완료되면 생성된 APK(`LYWSD02_BLE_Tool_v1.x.y.apk` 및 `LYWSD02_BLE_Tool.apk`)를 네트워크 공유 배포 경로인 `\\192.168.0.250\web\LYWSD02`에 반드시 복사할 것.
   - `local.properties`의 `apk.deploy.dir=\\\\192.168.0.250\\web\\LYWSD02` 설정을 통해 Gradle 태스크(`copyDebugApkToDeployDir` 및 `copyReleaseApkToDeployDir`)가 빌드 시 자동으로 복사를 수행하도록 보장할 것.

3. **빌드 검증 및 자가 루프 규칙**:
   - 절대 가정이나 추측으로 "작업이 완료되었습니다"라고 선언하지 말 것.
   - 코드 수정 후 반드시 터미널 명령으로 `./gradlew testDebugUnitTest` 및 `./gradlew assembleDebug` (또는 `assembleRelease`)를 실행하여 Exit Code 0 성공을 확인할 것.
   - 실패 시 스스로 오류를 분석하고 자가 수정 루프를 수행할 것.

4. **완료 후 작업**:
   - 최종 빌드 완료 및 멘트 종료 후 윈도우 딩동 멜로디 1회 재생 (`powershell -c "(New-Object Media.SoundPlayer 'C:\Windows\Media\chimes.wav').PlaySync()"`)
   - 연결된 GitHub 원격 저장소(`https://github.com/muro-dot/LYWSD02_BLE_Tool.git`)로 git commit & push 수행.

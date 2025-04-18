Procfile 실행 명령 (Heroku Dyno 기준)
web: java \

-Xss512k \ # 각 스레드에 할당할 스택 크기 설정 (기본보다 낮게: 메모리 절약 목적)

-Xmx300m \ # 최대 힙 메모리 사용량 제한 (Heroku 메모리 초과 방지를 위해 300MB로 제한)

-XX:+UseG1GC \ # G1 (Garbage First) GC 사용: 낮은 지연 시간 유지하며 효율적인 메모리 수집

-XX:+UseCompressedOops \ # 64bit JVM에서도 객체 포인터를 32bit로 압축해 힙 메모리 절약

-XX:NewRatio=2 \ # Old 영역 : Young 영역 = 2:1 → Old 영역이 Young보다 2배 크도록 비율 조정

-XX:SurvivorRatio=8 \ # Eden:Survivor = 8:1:1 로 설정 → GC 튜닝으로 Minor GC 효율 향상

-Xlog:gc*:file=gc.log:time,level,tags:filecount=5,filesize=1m \

GC 로그 출력 설정:

→ 시간/레벨/태그 포함

→ gc.log 파일에 기록

→ 최대 5개 파일, 각 1MB 로 순환 저장

-XX:+UseContainerSupport \ # Docker/LXC 등 컨테이너 리소스 제한을 JVM이 인식하도록 설정

-jar build/libs/knock_back-0.0.1-SNAPSHOT.jar # 실행할 JAR 파일 지정 (Spring Boot 앱 엔트리포인트)
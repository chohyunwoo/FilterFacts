# 1. Java 21 실행 환경 이미지 사용
FROM openjdk:21-jdk-slim

# 2. JAR 파일 복사 (build/libs 아래에 있다고 가정)
COPY build/libs/F_F-0.0.1-SNAPSHOT.jar app.jar

# 3. 앱 실행 명령
ENTRYPOINT ["java", "-jar", "/app.jar"]

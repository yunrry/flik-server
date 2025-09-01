# Spring Boot with JDK 21 - ARM64 최적화 Dockerfile
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Gradle Wrapper와 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 실행 권한 부여
RUN chmod +x gradlew

# 의존성 다운로드 (캐시 활용)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 빌드 실행 (테스트 제외)
RUN ./gradlew clean build -x test --no-daemon

# 빌드 결과 확인
RUN echo "=== JAR files created ===" && \
    ls -la build/libs/ && \
    find build/libs -name "*.jar" -type f

# 실행용 이미지
FROM eclipse-temurin:21-jre

WORKDIR /app

# 필수 패키지 설치
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# JAR 파일 복사 (app.jar로 고정)
COPY --from=builder /app/build/libs/*.jar app.jar

# 파일 확인
RUN ls -la app.jar

# 포트 노출
EXPOSE 8080

# 환경 변수 설정
ENV JAVA_OPTS="-Xms256m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication"

# JDK 21용 최적화된 JVM 옵션으로 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.jmx.enabled=false \
    -Dfile.encoding=UTF-8 \
    -Djava.net.preferIPv4Stack=true \
    -jar app.jar"]

# 헬스체크
HEALTHCHECK --interval=60s --timeout=30s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
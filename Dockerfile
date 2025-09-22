# 최적화된 Spring Boot Dockerfile - ARM64
FROM eclipse-temurin:21-jdk-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Alpine에 필요한 패키지 설치
RUN apk add --no-cache curl

# Gradle Wrapper와 설정 파일만 먼저 복사 (캐시 최적화)
COPY gradlew .
COPY gradle gradle/
COPY build.gradle settings.gradle ./

# 실행 권한 부여
RUN chmod +x gradlew

# 의존성만 먼저 다운로드 (이 레이어는 소스 변경시에도 캐시됨)
RUN ./gradlew dependencies --no-daemon --parallel --max-workers=4

# 소스 코드 복사 (의존성 다운로드 후)
COPY src src/

# 빌드 실행 (테스트 제외, 병렬 처리)
RUN ./gradlew clean build -x test --no-daemon --parallel --max-workers=4

# 최종 실행용 이미지 (더 가벼운 Alpine JRE 사용)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Alpine에 curl 설치 (헬스체크용)
RUN apk add --no-cache curl

# 비root 사용자 생성
RUN addgroup -g 1001 -S spring && adduser -u 1001 -S spring -G spring
USER spring

# JAR 파일 복사
COPY --from=builder --chown=spring:spring /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# JVM 최적화 옵션
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# 헬스체크
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
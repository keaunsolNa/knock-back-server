FROM openjdk:23-jdk-slim

# 필수 패키지 설치 및 업데이트
RUN apt-get update && apt-get install -y apt-utils

# 필요한 의존성 패키지 설치 (Google Chrome 설치 전에)
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    unzip \
    ca-certificates \
    gnupg \
    libx11-dev \
    libxcomposite1 \
    libxrandr2 \
    libxi6 \
    libgdk-pixbuf2.0-0 \
    libnss3 \
    libasound2 \
    libatk-bridge2.0-0 \
    libatk1.0-0 \
    libcups2 \
    libnspr4 \
    libnss3 \
    libxss1 \
    libappindicator3-1 \
    fonts-liberation \
    xdg-utils \
    libgbm1 \
    libvulkan1 \
    --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

RUN wget -q https://chromedriver.storage.googleapis.com/113.0.5672.0/chromedriver_linux64.zip
RUN apt-get install -y ./google-chrome-stable_current_amd64.deb
RUN rm ./google-chrome-stable_current_amd64.deb

RUN google-chrome --version

# 애플리케이션 디렉토리 생성
WORKDIR /app

# 빌드한 JAR 파일을 컨테이너로 복사
COPY build/libs/knock_back-0.0.1-SNAPSHOT.jar ./knock_back.jar

# 애플리케이션 포트 설정 (기본 8080)
EXPOSE 53287

# JAR 파일 실행
CMD ["java", "-jar", "knock_back.jar"]

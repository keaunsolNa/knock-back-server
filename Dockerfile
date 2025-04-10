FROM openjdk:23-jdk-slim

# 필수 패키지 설치
RUN apt-get update && apt-get install -y \
    apt-utils \
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
    libxss1 \
    libappindicator3-1 \
    fonts-liberation \
    xdg-utils \
    libgbm1 \
    libvulkan1 \
    --no-install-recommends \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Chrome 설치
RUN wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb \
    && apt install -y ./google-chrome-stable_current_amd64.deb \
    && rm ./google-chrome-stable_current_amd64.deb

# ChromeDriver 설치
RUN wget -q https://chromedriver.storage.googleapis.com/113.0.5672.0/chromedriver_linux64.zip \
    && unzip chromedriver_linux64.zip \
    && mv chromedriver /usr/local/bin/ \
    && chmod +x /usr/local/bin/chromedriver \
    && rm chromedriver_linux64.zip

RUN google-chrome --version && chromedriver --version

# 앱 디렉토리 설정
WORKDIR /app

# JAR 파일 복사
COPY build/libs/knock_back-0.0.1-SNAPSHOT.jar ./knock_back.jar

# 포트 개방
EXPOSE 53287

# 애플리케이션 실행
CMD ["java", "-jar", "knock_back.jar"]

# 第一阶段：构建阶段
FROM litongjava/maven:3.8.8-jdk8u391 AS builder

# 设置工作目录
WORKDIR /project

# 复制pom.xml并下载依赖
COPY pom.xml /project/
RUN mvn dependency:go-offline

# 复制源码到容器中
COPY src /project/src

# 运行maven打包命令
RUN mvn package -DskipTests -Pproduction


# 第二阶段：运行阶段
FROM litongjava/jdk:8u391-stable-slim

# 设置工作目录
WORKDIR /app

# 从构建阶段复制生成的jar文件到运行阶段
COPY --from=builder /project/target/tio-boot-admin-1.0.jar /app/

# Copy the jar file into the container
# COPY target/tio-boot-admin-1.0.jar /app/

# Command to run the jar file
CMD ["java", "-jar", "tio-boot-admin-1.0.jar", "--app.env=prod"]
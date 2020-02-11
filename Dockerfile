FROM openjdk:11-jdk as builder

COPY . /srv
WORKDIR /srv
RUN ./mvnw clean package -DskipTests

###############################################################################################################
FROM openjdk:11-jre-stretch

ENV APP_HOME      /srv
ENV APP_DIST_DIR  /srv-dist
ENV PATH /usr/local/openjdk-11/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:$APP_HOME/bin

COPY --from=builder /srv/target/s3-bucket-tester-*-dist.tar.gz /s3-bucket-tester.tar.gz

RUN mkdir /srv/temp \
	&& tar zxvf /s3-bucket-tester.tar.gz -C /srv/temp \
	&& rm -rf /s3-bucket-tester.tar.gz \
	&& mv /srv/temp/s3-bucket-tester* /srv/temp/something \
	&& mv /srv/temp/something/* /srv \
	&& rm -rf /srv/temp

ENV APP_USER app
ENV APP_UID 9999
ENV APP_GID 9999
RUN groupadd -r -g $APP_GID $APP_USER  \
    && useradd -r -u $APP_UID -g $APP_GID $APP_USER  \
    && mkdir $APP_HOME \
    && chown -R $APP_UID:$APP_GID $APP_HOME

USER $APP_USER

# Set working directory for convenience with interactive usage
WORKDIR $APP_HOME

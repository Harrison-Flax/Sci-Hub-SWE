# The base image is ubuntu
FROM ubuntu:latest
RUN apt-get update \
	&& DEBIAN_FRONTEND=noninteractive apt-get install -y sudo \
	&& apt-get update \
	&& DEBIAN_FRONTEND=noninteractive apt-get install -y wget \
	&& apt-get update \
	&& DEBIAN_FRONTEND=noninteractive apt-get install -y gnupg2 \
	&& apt-get update \
	&& DEBIAN_FRONTEND=noninteractive apt-get install -y software-properties-common \
	&& apt-get update \
	&& DEBIAN_FRONTEND=noninteractive apt-get install -y tmux \
	&& apt-get update \
	&& apt-get install debconf-utils \
	&& apt-get update \
 	&& add-apt-repository -y ppa:webupd8team/java \
	&& apt-get update \
	&& echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | debconf-set-selections \
	&& apt-get install -y oracle-java8-installer
ENTRYPOINT find /var/lib/mysql/mysql -exec touch -c -a {} + && service mysql start \
	&& /bin/bash

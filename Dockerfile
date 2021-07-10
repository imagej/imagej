# Development Dockerfile for ImageJ
# ---------------------------------
# This dockerfile can be used to build an
# ImageJ.app directory which can then be run
# within a number of different Docker images.

# By default, building this dockerfile will use
# the IMAGE argument below for the runtime image.
ARG IMAGE=openjdk:8-jre-alpine

# To install the built ImageJ.app into other runtimes
# pass a build argument, e.g.:
#
#   docker build --build-arg IMAGE=openjdk:9 ...
#

# Similarly, the MAVEN_IMAGE argument can be overwritten
# but this is generally not needed.
ARG MAVEN_IMAGE=maven:3.5-jdk-8

#
# Build phase: Use the maven image for building.
#
FROM ${MAVEN_IMAGE} as maven
RUN adduser ij
COPY . /src
RUN chown -R ij /src
USER ij
WORKDIR /src
RUN bin/populate-app.sh

#
# Install phase: Copy the build ImageJ.app into a
# clean container to minimize size.
#
FROM ${IMAGE}
COPY --from=maven /src/ImageJ.app /ImageJ.app
ENV PATH $PATH:/ImageJ.app
ENTRYPOINT ["ImageJ-linux64"]

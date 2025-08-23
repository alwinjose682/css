FROM docker.io/ubuntu:22.04 AS builder

# Set the environment, should NOT have a trailing '/' due to several reasons! (like: using it in COPY command etc. Check the docker reference)
ARG ONLY_REQ_FILES_DIR=/opt/req_files \
    BUILD_TMP_DIR=/opt/tmp \
    JDK_FULL_HOME=/opt/tmp/jdk_full \
    IGNITE_HOME=/opt/req_files/ignite/apache-ignite
# NOTE: Only the files required in the next stage are placed in 'ONLY_REQ_FILES_DIR'. This whole directory is copied in the next stage

# Pull apache-ignite image and prepare the distribution
COPY --from=docker.io/apacheignite/ignite:2.16.0 /opt/ignite/apache-ignite ${IGNITE_HOME}
WORKDIR ${IGNITE_HOME}
## Use the custom run.sh from build context
## Use ignite rest module
## Remove files that are not required
COPY build_items/custom_ignite_run.sh custom_ignite_run.sh
RUN rm -f run.sh && \
    mv custom_ignite_run.sh run.sh && \
    chmod 555 run.sh && \
    mv ./libs/optional/ignite-rest-http ./libs/ && \
    rm -rf ./libs/optional/; \
    rm -rf ./benchmarks/; \
    rm -rf ./examples/; \
    rm -rf ./platforms; \
    rm -rf ./docs;

# Download JDK and Create a custom JDK runtime for above ignite distribution
# https://download.oracle.com/java/21/archive/jdk-21.0.4_linux-x64_bin.tar.gz
WORKDIR ${BUILD_TMP_DIR}
ADD https://download.java.net/java/GA/jdk19.0.1/afdd2e245b014143b62ccb916125e3ce/10/GPL/openjdk-19.0.1_linux-x64_bin.tar.gz \
	openjdk_bin
RUN tar -xzf openjdk_bin && \
	jdkExtractedDir=$(find . -maxdepth 1 -type d -name "jdk*" | cut -c 3-) && \
	mv "${jdkExtractedDir}" ${JDK_FULL_HOME}; \
## Get all required modules for ignite
    ${JDK_FULL_HOME}/bin/jdeps \
        -q \
        -R \
        --print-module-deps \
        --ignore-missing-deps \
        --multi-release=17 \
        $(find ${IGNITE_HOME} -type f -name "*.jar") > /tmp/req_modules && \
# Create custom jdk runtime, in directory named 'jdk'
    $JDK_FULL_HOME/bin/jlink \
          --verbose \
          --module-path ${JDK_FULL_HOME}/jmods \
          --add-modules="$(cat /tmp/req_modules),jdk.internal.jvmstat" \
          --bind-services \
          --compress=2 \
          --strip-java-debug-attributes \
          --no-man-pages \
          --no-header-files \
          --save-opts jlink.opts \
          --output ${ONLY_REQ_FILES_DIR}/jdk && \
      #      --strip-debug \ # requires objcopy[binutils of OS]. Does not have any size saving even after stripping debug. Maybe debug info is already stripped off in openjdk distributions
# Move jlink.opts to the custom jdk runtime directory
    mv jlink.opts ${ONLY_REQ_FILES_DIR}/jdk/

#--------------------------------------#
# Create a fresh image with the custom JDK runtime and apache-ignite
FROM docker.io/ubuntu:22.04
# Set the environment, should NOT have a trailing '/' due to several reasons!
ENV JAVA_HOME=/opt/jdk \
    IGNITE_HOME=/opt/ignite/apache-ignite
COPY --from=builder /opt/req_files /opt
WORKDIR ${IGNITE_HOME}
ENTRYPOINT [ "./run.sh" ]
EXPOSE 11211 47100 47500 49112 10800 8080

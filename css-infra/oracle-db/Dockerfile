FROM container-registry.oracle.com/database/express:21.3.0-xe
# TODO: This does not work as of now. Create the DB and run the init sqls manually

# Place the CSS DB setup sql scripts in startup directory
COPY install__1_0 /etc/opt/css_db/install__1_0
COPY start_css_db.sh /opt/oracle/scripts/startup/start_css_db.sh
#RUN chmod 555 /opt/oracle/scripts/startup/start_css_db.sh; \
#    chmod 555 /etc/opt/css_db; \
#    chmod -R 555 /etc/opt/css_db/install__1_0;

# All of the below is copied from https://github.com/oracle/docker-images/blob/main/OracleDatabase/SingleInstance/dockerfiles/21.3.0/Dockerfile.xe
USER oracle
WORKDIR /home/oracle

CMD [ "/bin/bash", "-c", "$ORACLE_BASE/$RUN_FILE" ]

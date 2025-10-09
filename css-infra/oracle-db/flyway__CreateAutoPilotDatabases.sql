DECLARE
   environments  SYS.ODCIVARCHAR2LIST := SYS.ODCIVARCHAR2LIST('Dev', 'Test', 'Prod', 'Check', 'Shadow', 'Build');
   tablespace_prefix  VARCHAR2(50) := 'ap_';
   user_password      VARCHAR2(50) := 'Redgate';
BEGIN
   FOR i IN 1..environments.COUNT LOOP
      BEGIN
         -- Create tablespace
         EXECUTE IMMEDIATE 'CREATE TABLESPACE ' || tablespace_prefix || environments(i) || '_data ' ||
                           'DATAFILE ''' || tablespace_prefix || environments(i) || '_tabspace.dat'' ' ||
                           'SIZE 10M AUTOEXTEND ON';

         -- Create temporary tablespace
         EXECUTE IMMEDIATE 'CREATE TEMPORARY TABLESPACE ' || tablespace_prefix || environments(i) || '_tabspace_temp ' ||
                           'TEMPFILE ''' || tablespace_prefix || environments(i) || '_tabspace_temp.dat'' ' ||
                           'SIZE 5M AUTOEXTEND ON';

         -- Create user and assign tablespaces
         EXECUTE IMMEDIATE 'CREATE USER ' || tablespace_prefix || environments(i) || ' ' ||
                           'IDENTIFIED BY ' || user_password || ' ' ||
                           'DEFAULT TABLESPACE ' || tablespace_prefix || environments(i) || '_data ' ||
                           'TEMPORARY TABLESPACE ' || tablespace_prefix || environments(i) || '_tabspace_temp';

         -- Grant necessary permissions
         EXECUTE IMMEDIATE 'GRANT CREATE SESSION TO ' || tablespace_prefix || environments(i);

         DBMS_OUTPUT.PUT_LINE('Successfully created environment: ' || environments(i));
      EXCEPTION
         WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Failed to create environment: ' || environments(i) || ' - Error: ' || SQLERRM);
      END;
   END LOOP;
END;

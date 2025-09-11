set serveroutput on size 10000;

define DATA_TS=&1
define INDEX_TS=&2

begin
    execute immediate 'CREATE bigfile TABLESPACE &DATA_TS ' ||
    'DATAFILE ''&DATA_TS' || '_tabspace.dbf'' SIZE 10M AUTOEXTEND ON';

    execute immediate 'CREATE bigfile TABLESPACE &INDEX_TS ' ||
    'DATAFILE ''&INDEX_TS' || '_tabspace.dbf'' SIZE 10M AUTOEXTEND ON';

    dbms_output.put_line('Successfully created tablespaces: &DATA_TS and &INDEX_TS');
EXCEPTION WHEN OTHERS THEN
    dbms_output.put_line('Failed to create one or both tablespaces: &DATA_TS and &INDEX_TS; ' || 'Error: ' || SQLERRM);
end;
/
set serveroutput off;
exit

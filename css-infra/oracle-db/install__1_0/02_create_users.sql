define DATA_TS=&1
define INDEX_TS=&2

--css
create user css
identified by freepass
default tablespace &DATA_TS;

alter user css quota unlimited on &DATA_TS;
alter user css quota unlimited on &INDEX_TS;

create role css_role not identified;
grant create table to css_role;
grant create session to css_role;
grant create sequence to css_role;
grant css_role to css;
grant css_role to sys with admin option;
alter user css default role all; --to re-grant the roles at login

--css_refdata
create user css_refdata
identified by freepass
default tablespace &DATA_TS;

alter user css_refdata quota unlimited on &DATA_TS;
alter user css_refdata quota unlimited on &INDEX_TS;

grant css_role to css_refdata;
grant css_role to sys with admin option;
prompt *** S-1 ***
--Seems like Oracle express:21.3.0-xe gets stuck at this step
--alter user css_refdata default role all; --to re-grant the roles at login
prompt *** S-2 ***
exit

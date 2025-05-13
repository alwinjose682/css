--grant select on counterparty to css, css_refdata;
--Note: above format is not used as h2 does not support providing grants to multiple users in a single statement

grant select on counterparty to css;
grant select on counterparty_netting_profile to css;
grant select on counterparty_sla_mapping to css;
grant select on ssi to css;
grant select on country to css;
grant select on currency to css;
grant select on entity to css;
grant select on book to css;
grant select on nostro to css;
grant select on data_load_status to css;

grant select on counterparty to css_refdata;
grant select on counterparty_netting_profile to css_refdata;
grant select on counterparty_sla_mapping to css_refdata;
grant select on ssi to css_refdata;
grant select on country to css_refdata;
grant select on currency to css_refdata;
grant select on entity to css_refdata;
grant select on book to css_refdata;
grant select on nostro to css_refdata;
grant select on data_load_status to css_refdata;

grant update, delete, insert on counterparty to css_refdata;
grant update, delete, insert on counterparty_netting_profile to css_refdata;
grant update, delete, insert on counterparty_sla_mapping to css_refdata;
grant update, delete, insert on ssi to css_refdata;
grant update, delete, insert on country to css_refdata;
grant update, delete, insert on currency to css_refdata;
grant update, delete, insert on entity to css_refdata;
grant update, delete, insert on book to css_refdata;
grant update, delete, insert on nostro to css_refdata;
grant update, delete, insert on data_load_status to css_refdata;

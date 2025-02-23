INSERT INTO USERS (ID,USERNAME,EMAIL,FIRST_NAME,LAST_NAME, PASSWORD, ACTIVE) VALUES (1, 'dquaid@acme.com','dquaid@acme.com','Douglas','Quaid','changeit', true);
INSERT INTO USERS (ID,USERNAME,EMAIL,FIRST_NAME,LAST_NAME, PASSWORD, ACTIVE) VALUES (2, 'jrambo@acme.com','jrambo@acme.com','John','Rambo','changeit', true);
ALTER TABLE USERS ALTER COLUMN ID RESTART WITH (SELECT MAX(ID) FROM USERS) + 1;

INSERT INTO GROUPS (ID,NAME) VALUES (1,'Group 1');
INSERT INTO GROUPS (ID,NAME) VALUES (2,'Group 2');
ALTER TABLE GROUPS ALTER COLUMN ID RESTART WITH (SELECT MAX(ID) FROM GROUPS) + 1;

INSERT INTO GROUP_MEMBERS (GROUP_ID,USER_ID) VALUES (1,1);
INSERT INTO GROUP_MEMBERS (GROUP_ID,USER_ID) VALUES (2,2);

INSERT INTO LICENSES (ID,NAME) VALUES (1,'License 1');
INSERT INTO LICENSES (ID,NAME) VALUES (2,'License 2');
ALTER TABLE LICENSES ALTER COLUMN ID RESTART WITH (SELECT MAX(ID) FROM LICENSES) + 1;

INSERT INTO LICENSE_ASSIGNMENTS (LICENSE_ID,USER_ID) VALUES (1,1);
INSERT INTO LICENSE_ASSIGNMENTS (LICENSE_ID,USER_ID) VALUES (2,2);

INSERT INTO RESPONSIBILITIES (ID,NAME) VALUES (1,'Responsibility 1');
INSERT INTO RESPONSIBILITIES (ID,NAME) VALUES (2,'Responsibility 2');
INSERT INTO RESPONSIBILITIES (ID,NAME) VALUES (3,'Responsibility 3');
INSERT INTO RESPONSIBILITIES (ID,NAME) VALUES (4,'Responsibility 4');
ALTER TABLE RESPONSIBILITIES ALTER COLUMN ID RESTART WITH (SELECT MAX(ID) FROM RESPONSIBILITIES) + 1;

INSERT INTO RESPONSIBILITY_ASSIGNMENTS (RESPONSIBILITY_ID,USER_ID) VALUES (1,1);
INSERT INTO RESPONSIBILITY_ASSIGNMENTS (RESPONSIBILITY_ID,USER_ID) VALUES (1,2);
INSERT INTO RESPONSIBILITY_ASSIGNMENTS (RESPONSIBILITY_ID,USER_ID) VALUES (2,3);
INSERT INTO RESPONSIBILITY_ASSIGNMENTS (RESPONSIBILITY_ID,USER_ID) VALUES (2,4);


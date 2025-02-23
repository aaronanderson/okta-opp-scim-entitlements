create table USERS(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  USERNAME varchar(100) not null,
  EMAIL varchar(100) not null,
  FIRST_NAME varchar(100) not null,
  LAST_NAME varchar(100) not null,
  -- password is clear text for demo purposes only
  PASSWORD varchar(100) not null, 
  ACTIVE boolean,
  LAST_MODIFIED timestamp on update CURRENT_TIMESTAMP
);

create table GROUPS(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  NAME varchar(100) not null,
  LAST_MODIFIED timestamp on update CURRENT_TIMESTAMP
);

create table GROUP_MEMBERS(
  GROUP_ID int not null,
  USER_ID int not null,
  LAST_MODIFIED timestamp on update CURRENT_TIMESTAMP,
  CONSTRAINT GROUP_MEMBERS_KEY PRIMARY KEY (GROUP_ID, USER_ID)
);

create table LICENSES(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  NAME varchar(100) not null,
  DESCRIPTION varchar(100),
  LAST_MODIFIED timestamp on update CURRENT_TIMESTAMP
);

create table LICENSE_ASSIGNMENTS(
  LICENSE_ID int not null,
  USER_ID int not null,
  LAST_MODIFIED timestamp on update CURRENT_TIMESTAMP,
  CONSTRAINT LICENSE_ASSIGNMENT_KEY PRIMARY KEY (LICENSE_ID, USER_ID)
);

create table RESPONSIBILITIES(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  NAME varchar(100) not null,
  DESCRIPTION varchar(100),
  LAST_MODIFIED timestamp on update CURRENT_TIMESTAMP
);

create table RESPONSIBILITY_ASSIGNMENTS(
  RESPONSIBILITY_ID int not null,
  USER_ID int not null,
  LAST_MODIFIED timestamp on update CURRENT_TIMESTAMP,
  CONSTRAINT RESPONSIBILITY_ASSIGNMENT_KEY PRIMARY KEY (RESPONSIBILITY_ID, USER_ID)
);
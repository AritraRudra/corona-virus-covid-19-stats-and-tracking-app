/*

CREATE TABLE PatientsStats(
	ID int,
	Latest_Count int,
	Count_History tinytext,
	CONSTRAINT PatientsStats PRIMARY KEY (ID),
	CONSTRAINT FK_LocationStats FOREIGN KEY (ID)
    REFERENCES LocationStats(ID)
);

CREATE TABLE LocationStats (
	ID int,
    State varchar(255),
    Region varchar(255),
    CONSTRAINT PK_LocationStats PRIMARY KEY (ID)
);
*/
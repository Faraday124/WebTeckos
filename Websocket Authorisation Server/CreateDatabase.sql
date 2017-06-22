use websocketdatabase; /** Selecting Database **/



CREATE TABLE users (
 ID INT PRIMARY KEY auto_increment,
 Login VARCHAR(20) NOT NULL,
 Password VARCHAR(80) NOT NULL,
 Salt VARCHAR(255) NOT NULL
 )ENGINE=InnoDB CHARACTER SET=utf8 COLLATE=utf8_polish_ci ;


